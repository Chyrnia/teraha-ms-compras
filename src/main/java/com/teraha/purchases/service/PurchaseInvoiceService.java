package com.teraha.purchases.service;

import java.util.List;
import java.math.BigDecimal;

import org.springframework.stereotype.Service;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import com.teraha.commons.entities.PurchaseInvoice;
import com.teraha.commons.entities.PurchaseInvoiceDetail;
import com.teraha.commons.entities.Product;
import com.teraha.commons.entities.Supplier;

import com.teraha.commons.dtos.PurchaseInvoiceDTO;
import com.teraha.commons.dtos.PurchaseInvoiceDetailDTO;
import com.teraha.commons.dtos.InventoryUpdate;
import com.teraha.commons.dtos.ProductDTO;

import com.teraha.purchases.repository.ProductRepository;
import com.teraha.purchases.repository.SupplierRepository;
import com.teraha.purchases.repository.PurchaseInvoiceRepository;

import com.teraha.purchases.mapper.PurchaseInvoiceMapper;

import com.teraha.purchases.exception.SupplierNotFoundException;
import com.teraha.purchases.exception.ProductNotFoundException;
import com.teraha.purchases.exception.PurchaseInvoiceBadRequestException;
import com.teraha.purchases.exception.PurchaseInvoiceNotFoundException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Transactional
@Service
@RequiredArgsConstructor
public class PurchaseInvoiceService {

	private final RestTemplate rest;

	private final SupplierRepository supplierRepository;
	private final ProductRepository productRepository;
	private final PurchaseInvoiceRepository purchaseInvoiceRepository;

	private final PurchaseInvoiceMapper purchaseInvoiceMapper;

	public PurchaseInvoiceDTO createPurchaseInvoice(PurchaseInvoiceDTO dto){

		validatePurchaseInvoiceCreation(dto);

		//retrieve and validate supplier 
		Supplier supplier = supplierRepository
			.findByIdAndActiveTrue(dto.getSupplierId())
			.orElseThrow(() -> new SupplierNotFoundException(dto.getSupplierId()));

		PurchaseInvoice invoice = new PurchaseInvoice(dto.getInvoiceNumber(), dto.getTransactionDate(), supplier);

		//fill details and compute total
		BigDecimal total = fillInvoiceWithDetails(invoice, dto.getDetails());
		invoice.setTotal(total);

		log.info("Creating purchase invoice with number {}", invoice.getInvoiceNumber());

		PurchaseInvoice saved = purchaseInvoiceRepository.save(invoice);

		//call products and tell him that we got more stock!
		for(PurchaseInvoiceDetailDTO d : dto.getDetails()){
			updateProductStock(d.getProductId(), d.getQuantity(), d.getUnitCost());
		}

		return purchaseInvoiceMapper.toDto(saved);
	}

	public Page<PurchaseInvoiceDTO> listPurchases(int page){
		//TODO: create paging configurations
		Pageable pageReq = PageRequest.of(page, 10);
		return purchaseInvoiceRepository.findAll(pageReq)
			.map(purchaseInvoiceMapper::toDto);
	}

	public PurchaseInvoiceDTO getPurchaseInvoice(Long id){
		return purchaseInvoiceRepository.findById(id)
			.map(purchaseInvoiceMapper::toDto)
			.orElseThrow(() -> new PurchaseInvoiceNotFoundException(id));
	}

	private void  validatePurchaseInvoiceCreation(PurchaseInvoiceDTO dto){

		if(dto.getInvoiceNumber() == null || dto.getInvoiceNumber().isBlank()){
			log.error("No invoice number provided");
			throw new PurchaseInvoiceBadRequestException("Invoice Number invalid");
		}

		if(dto.getTransactionDate() == null){
			log.error("No invoice date provided");
			throw new PurchaseInvoiceBadRequestException("Date invalid");
		}

		if(dto.getSupplierId() == null){
			log.error("No supplier provided");
			throw new PurchaseInvoiceBadRequestException("Supplier invalid");
		}

		if(dto.getDetails() == null || dto.getDetails().isEmpty()){
			log.error("No details provided");
			throw new PurchaseInvoiceBadRequestException("Empty details");
		}

	}

	private BigDecimal fillInvoiceWithDetails(PurchaseInvoice invoice, List<PurchaseInvoiceDetailDTO> details){

		BigDecimal total = BigDecimal.ZERO;

		for(PurchaseInvoiceDetailDTO d : details){

			//validate productId
			if(d.getProductId() == null){
				throw new PurchaseInvoiceBadRequestException("Required productId");
			}

			//retrieve and validate product
			Product product = productRepository
				.findByIdAndActiveTrue(d.getProductId())
				.orElseThrow(() -> new ProductNotFoundException(d.getProductId()));

			//validate quantity
			if(d.getQuantity() == null || d.getQuantity() <= 0){
				throw new PurchaseInvoiceBadRequestException("Required quantity > 0");
			}
			BigDecimal quantity = BigDecimal.valueOf(d.getQuantity());

			//validate unit cost
			if(d.getUnitCost() == null || d.getUnitCost() <= 0){
				throw new PurchaseInvoiceBadRequestException("Required unit cost > 0");
			}
			BigDecimal unitCost = BigDecimal.valueOf(d.getUnitCost());

			PurchaseInvoiceDetail detail = new PurchaseInvoiceDetail(product, d.getQuantity(), unitCost);
			invoice.addDetail(detail);


			total = total.add(unitCost.multiply(quantity));
		}

		return total;
	}

	private void updateProductStock(Long productId, int quantity, double unitCost){

		InventoryUpdate inv = new InventoryUpdate();

		inv.stockDelta(quantity)
			.unitCost(unitCost);

		//TODO: make URL configurable
		rest.patchForObject("http://localhost:30000/products/{id}/inventory", inv, ProductDTO.class, productId);     
	}

}

