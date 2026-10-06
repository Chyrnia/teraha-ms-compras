package com.teraha.purchases.exception;

public class SupplierNotFoundException extends RuntimeException {
	public SupplierNotFoundException(Long id){
		super("Supplier with id "+ id +" was not found");
	}
}

