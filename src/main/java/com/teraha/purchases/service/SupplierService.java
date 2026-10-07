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
import com.teraha.purchases.configs.PagingConfigs;

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
	private final PagingConfigs pageConfigs;

	public SupplierDTO createSupplier(SupplierDTO dto){

		validateSupplierCreationInfo(dto);

		Supplier supplier = supplierMapper.toEntity(dto);

		log.info("Creating supplier with document {}", dto.getDocument());

		Supplier saved = supplierRepository.save(supplier);

		return supplierMapper.toDto(saved);
	}

	public Page<SupplierDTO> listSuppliers(int page){
		Pageable pageReq = PageRequest.of(page, pageConfigs.getPageSize());

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

		if(
				dto.getDocument() == null || dto.getDocument().isBlank() ||
				dto.getName() == null || dto.getName().isBlank() ||
				dto.getTimbrado() == null || dto.getTimbrado().isBlank()
		  ){
			throw new SupplierBadRequestException("Los campos Nombre, Documento y Timbrado son obligatorios");
		  }

	}

}

