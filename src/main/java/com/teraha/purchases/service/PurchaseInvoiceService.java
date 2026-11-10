package com.teraha.purchases.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import com.teraha.commons.dtos.InventoryUpdate;
import com.teraha.commons.dtos.InventoryUpdateLine;
import com.teraha.commons.dtos.PurchaseInvoiceDTO;
import com.teraha.commons.dtos.PurchaseInvoiceDetailDTO;
import com.teraha.commons.entities.Product;
import com.teraha.commons.entities.PurchaseInvoice;
import com.teraha.commons.entities.PurchaseInvoiceDetail;
import com.teraha.commons.entities.Supplier;

import com.teraha.purchases.configs.PagingConfigs;
import com.teraha.purchases.exception.ProductNotFoundException;
import com.teraha.purchases.exception.PurchaseInvoiceBadRequestException;
import com.teraha.purchases.exception.PurchaseInvoiceNotFoundException;
import com.teraha.purchases.exception.SupplierNotFoundException;
import com.teraha.purchases.mapper.PurchaseInvoiceMapper;
import com.teraha.purchases.repository.ProductRepository;
import com.teraha.purchases.repository.PurchaseInvoiceRepository;
import com.teraha.purchases.repository.SupplierRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class PurchaseInvoiceService {

    @Value("${products.service.url}")
    private String productServiceUrl;

    private final RestTemplate rest;

    private final SupplierRepository supplierRepository;
    private final ProductRepository productRepository;
    private final PurchaseInvoiceRepository purchaseInvoiceRepository;

    private final PurchaseInvoiceMapper purchaseInvoiceMapper;
    private final PagingConfigs pageConfigs;

    @CachePut(cacheNames = "purchases:invoices", key = "#result.id")
    public PurchaseInvoiceDTO createPurchaseInvoice(PurchaseInvoiceDTO dto) {

        validatePurchaseInvoiceCreation(dto);

        Supplier supplier = supplierRepository
                .findByIdAndActiveTrue(dto.getSupplierId())
                .orElseThrow(() -> new SupplierNotFoundException(dto.getSupplierId()));

        PurchaseInvoice invoice = new PurchaseInvoice(
                dto.getInvoiceNumber(),
                dto.getTransactionDate(),
                supplier
        );

        BigDecimal total = fillInvoiceWithDetails(invoice, dto.getDetails());
        invoice.setTotal(total);

        log.info("Creating purchase invoice with number {}", invoice.getInvoiceNumber());

        // Persist the invoice first.
        PurchaseInvoice saved = purchaseInvoiceRepository.save(invoice);

        // Increase inventory 
        updateProductStocks(saved.getDetails(), 1);

        return purchaseInvoiceMapper.toDto(saved);
    }

    @CacheEvict(cacheNames = "purchases:invoices", key = "#id")
    public void deactivatePurchaseInvoice(Long id) {

        PurchaseInvoice invoice = purchaseInvoiceRepository
                .findByIdForUpdate(id)
                .orElseThrow(() -> new PurchaseInvoiceNotFoundException(id));

        if (invoice.isCancelled()) {
            throw new PurchaseInvoiceBadRequestException(
                    "Purchase invoice is already cancelled"
            );
        }

        log.info("Cancelling purchase invoice {}", id);

        // Reverse the inventory increase caused by this purchase.
        reverseProductStocks(invoice.getDetails());

        // Only mark it cancelled if Products accepted the reversal.
        invoice.setCancelled(true);
    }

    @Transactional(readOnly = true)
    public Page<PurchaseInvoiceDTO> listPurchases(int page, String search) {

        Pageable pageReq = PageRequest.of(page, pageConfigs.getPageSize());

        Page<PurchaseInvoice> purchases;

        if (search == null || search.isBlank()) {
            log.info("Listing purchases, page {}", page);
            purchases = purchaseInvoiceRepository.findAll(pageReq);
        } else {
            log.info("Listing purchases, page {}, search term: {}", page, search);
            purchases = purchaseInvoiceRepository.searchPurchases(
                    search.trim(),
                    pageReq
            );
        }

        return purchases.map(purchaseInvoiceMapper::toDto);
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "purchases:invoices", key = "#id")
    public PurchaseInvoiceDTO getPurchaseInvoice(Long id) {

        log.info("Fetching invoice with id: {}", id);

        return purchaseInvoiceRepository.findById(id)
                .map(purchaseInvoiceMapper::toDto)
                .orElseThrow(() -> new PurchaseInvoiceNotFoundException(id));
    }

    private void validatePurchaseInvoiceCreation(PurchaseInvoiceDTO dto) {

        if (dto.getInvoiceNumber() == null || dto.getInvoiceNumber().isBlank()) {
            throw new PurchaseInvoiceBadRequestException("Invoice number invalid");
        }

        if (dto.getTransactionDate() == null) {
            throw new PurchaseInvoiceBadRequestException("Date invalid");
        }

        if (dto.getSupplierId() == null) {
            throw new PurchaseInvoiceBadRequestException("Supplier invalid");
        }

        if (dto.getDetails() == null || dto.getDetails().isEmpty()) {
            throw new PurchaseInvoiceBadRequestException("Empty details");
        }
    }

    private BigDecimal fillInvoiceWithDetails(
            PurchaseInvoice invoice,
            List<PurchaseInvoiceDetailDTO> details
    ) {

        BigDecimal total = BigDecimal.ZERO;

        for (PurchaseInvoiceDetailDTO d : details) {

            if (d.getProductId() == null) {
                throw new PurchaseInvoiceBadRequestException("Required productId");
            }

            Product product = productRepository
                    .findByIdAndActiveTrue(d.getProductId())
                    .orElseThrow(() -> new ProductNotFoundException(d.getProductId()));

            if (d.getQuantity() == null || d.getQuantity() <= 0) {
                throw new PurchaseInvoiceBadRequestException("Required quantity > 0");
            }

            if (d.getUnitCost() == null || d.getUnitCost() <= 0) {
                throw new PurchaseInvoiceBadRequestException("Required unit cost > 0");
            }

            BigDecimal quantity = BigDecimal.valueOf(d.getQuantity());
            BigDecimal unitCost = BigDecimal.valueOf(d.getUnitCost());

            PurchaseInvoiceDetail detail = new PurchaseInvoiceDetail(
                    product,
                    d.getQuantity(),
                    unitCost
            );

            invoice.addDetail(detail);

            total = total.add(unitCost.multiply(quantity));
        }

        return total;
    }

    private void updateProductStocks(
            List<PurchaseInvoiceDetail> details,
            int multiplier
    ) {

        List<InventoryUpdateLine> lines = new ArrayList<>();

        for (PurchaseInvoiceDetail d : details) {

            InventoryUpdateLine line = new InventoryUpdateLine();

            line.setProductId(d.getProduct().getId());
            line.setStockDelta(d.getQuantity() * multiplier);
            line.setUnitCost(d.getUnitCost().doubleValue());

            lines.add(line);
        }

        InventoryUpdate update = new InventoryUpdate();
        update.setDetails(lines);

        rest.postForObject(
                productServiceUrl + "/products/inventory",
                update,
                Void.class
        );

        log.info(
                "Applied inventory batch with {} movements (multiplier: {})",
                lines.size(),
                multiplier
        );
    }

	private void reverseProductStocks(List<PurchaseInvoiceDetail> details) {

    List<InventoryUpdateLine> lines = new ArrayList<>();

    for (PurchaseInvoiceDetail d : details) {

        InventoryUpdateLine line = new InventoryUpdateLine();

        line.setProductId(d.getProduct().getId());
        line.setStockDelta(-d.getQuantity());

        // Deliberately omit unitCost: this is a reversal,
        // not a new purchase.

        lines.add(line);
    }

    InventoryUpdate update = new InventoryUpdate();
    update.setDetails(lines);

    rest.postForObject(
            productServiceUrl + "/products/inventory",
            update,
            Void.class
    );

    log.info(
            "Reversed inventory batch with {} movements",
            lines.size()
    );
}
}
