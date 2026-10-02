package com.teraha.purchases.repository;

import org.springframework.stereotypes.Repository;

import com.teraha.commons.entities.PurchaseInvoice;

@Repository
public interface PurchaseInvoiceRepository extends InvoiceRepository<PurchaseInvoice> {}
