package com.teraha.purchases.service;

import java.util.Optional;
import java.util.List;
import java.math.BigDecimal;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.teraha.commons.entities.PurchaseInvoice;
import com.teraha.commons.entities.PurchaseInvoiceDetail;
import com.teraha.commons.entities.Product;
import com.teraha.commons.entities.Supplier;

import com.teraha.commons.dtos.PurchaseInvoiceDTO;
import com.teraha.commons.dtos.PurchaseInvoiceDetailDTO;

import com.teraha.purchases.repository.ProductRepository;
import com.teraha.purchases.repository.SupplierRepository;
import com.teraha.purchases.repository.PurchaseInvoiceRepository;

import com.teraha.purchases.mapper.PurchaseInvoiceMapper;

import com.teraha.purchases.exception.SupplierNotFoundException;
import com.teraha.purchases.exception.ProductNotFoundException;
import com.teraha.purchases.exception.PurchaseInvoiceBadRequestException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Transactional
@Service
@RequiredArgsConstructor
public class PurchaseInvoiceService {

	private final SupplierRepository supplierRepository;
	private final ProductRepository productRepository;
	private final PurchaseInvoiceRepository purchaseInvoiceRepository;

	private final PurchaseInvoiceMapper purchaseInvoiceMapper;

	public PurchaseInvoiceDTO createPurchaseInvoice(PurchaseInvoiceDTO dto){

		validatePurchaseInvoiceCreation(dto);

		//retriev and validate supplier 
		Supplier supplier = supplierRepository
			.findByIdAndActiveTrue(dto.getSupplierId())
			.orElseThrow(() -> new SupplierNotFoundException(dto.getSupplierId()));

		PurchaseInvoice invoice = new PurchaseInvoice(dto.getInvoiceNumber(), dto.getTransactionDate(), supplier);

		//fill details and compute total
		BigDecimal total = fillInvoiceWithDetails(invoice, dto.getDetails());
		invoice.setTotal(total);

		log.info("Creating purchase invoice with number {}", invoice.getInvoiceNumber());

		PurchaseInvoice saved = purchaseInvoiceRepository.save(invoice);

		return purchaseInvoiceMapper.toDto(saved);
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

}

