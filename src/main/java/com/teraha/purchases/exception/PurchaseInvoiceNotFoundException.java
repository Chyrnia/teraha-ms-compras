package com.teraha.purchases.exception;

public class PurchaseInvoiceNotFoundException extends RuntimeException {
	public PurchaseInvoiceNotFoundException(Long id){
		super("Invoice with id "+ id +" was not found");
	}
}

