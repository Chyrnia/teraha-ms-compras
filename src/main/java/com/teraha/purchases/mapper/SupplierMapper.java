package com.teraha.purchases.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.BeanMapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;


import com.teraha.commons.entities.Supplier;
import com.teraha.commons.dtos.SupplierDTO;

@Mapper(componentModel = "spring")
public interface SupplierMapper{

	SupplierDTO toDto(Supplier supplier);

	@Mapping(target="products", ignore=true)
	Supplier toEntity(SupplierDTO supplierDto);

	@BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
	@Mapping(target="products", ignore=true)
	void updateEntity(
			SupplierDTO supplierDto,
			@MappingTarget Supplier supplier);
}
