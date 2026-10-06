package com.teraha.purchases.service;

import org.springframework.stereotype.Service;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;

import com.teraha.commons.entities.Supplier;
import com.teraha.commons.dtos.SupplierDTO;

import com.teraha.purchases.repository.SupplierRepository;
import com.teraha.purchases.mapper.SupplierMapper;
import com.teraha.purchases.exception.SupplierConflictException;
import com.teraha.purchases.exception.SupplierNotFoundException;
import com.teraha.purchases.exception.SupplierBadRequestException;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Transactional
@Service
@RequiredArgsConstructor
public class SupplierService {

	private final SupplierRepository supplierRepository;
	private final SupplierMapper supplierMapper;

	public SupplierDTO createSupplier(SupplierDTO dto){

		validateSupplierCreationInfo(dto);

		Supplier supplier = supplierMapper.toEntity(dto);

		log.info("Creating supplier with document {}", dto.getDocument());

		Supplier saved = supplierRepository.save(supplier);

		return supplierMapper.toDto(saved);
	}

	public Page<SupplierDTO> listSuppliers(int page){
		//TODO: still have to create PageConfigs for this
		Pageable pageReq = PageRequest.of(page, 10);

		return supplierRepository.findAllByActiveTrue(pageReq)
			.map(supplierMapper::toDto);
	}

	public SupplierDTO findSupplierById(Long id){
		return supplierRepository.findByIdAndActiveTrue(id)
			.map(supplierMapper::toDto)
			.orElseThrow(() -> new SupplierNotFoundException(id));
	}


	private void  validateSupplierCreationInfo(SupplierDTO dto){

		validateRequiredCreationFields(dto);
		
		if(supplierRepository.existsByDocument(dto.getDocument())){
			throw new SupplierConflictException(dto.getDocument());
		}

	}

	private void validateRequiredCreationFields(SupplierDTO dto){

		//TODO: check for blank strings too
		if(dto.getDocument() == null || dto.getName() == null || dto.getTimbrado() == null){
			throw new SupplierBadRequestException("Los campos Nombre, Documento y Timbrado son obligatorios");
		}
		
	}

}

