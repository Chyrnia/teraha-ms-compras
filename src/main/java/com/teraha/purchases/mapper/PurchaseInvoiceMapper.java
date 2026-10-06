package com.teraha.purchases.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.teraha.commons.entities.PurchaseInvoice;
import com.teraha.commons.entities.PurchaseInvoiceDetail;
import com.teraha.commons.dtos.PurchaseInvoiceDTO;
import com.teraha.commons.dtos.PurchaseInvoiceDetailDTO;

@Mapper(componentModel = "spring")
public interface PurchaseInvoiceMapper{

	@Mapping(target="supplierId", source="thirdParty.id")
	PurchaseInvoiceDTO toDto(PurchaseInvoice invoice);

	@Mapping(target="productId", source="product.id")
	PurchaseInvoiceDetailDTO toDto(PurchaseInvoiceDetail detail);
}
