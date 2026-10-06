package com.teraha.purchases.repository;

import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;

import com.teraha.commons.entities.Supplier;

public interface SupplierRepository extends PersonRepository<Supplier> {

	Page<Supplier> findAllByActiveTrue(Pageable page);

	Optional<Supplier> findByIdAndActiveTrue(Long id);

	boolean existsByDocument(String document);

}
