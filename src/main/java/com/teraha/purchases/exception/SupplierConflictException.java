package com.teraha.purchases.exception;

public class SupplierConflictException extends RuntimeException {
	public SupplierConflictException(String document){
		super("The supplier with document " + document + " already exists");
	}
}

