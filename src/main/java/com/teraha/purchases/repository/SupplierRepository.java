package com.teraha.purchases.repository;

import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.teraha.commons.entities.Supplier;

public interface SupplierRepository extends PersonRepository<Supplier> {

	Page<Supplier> findAllByActiveTrue(Pageable page);

	Optional<Supplier> findByIdAndActiveTrue(Long id);

	boolean existsByDocument(String document);

	@Query("""
		SELECT s
		FROM Supplier s
		WHERE s.active = true
		  AND (
			LOWER(s.name) LIKE LOWER(CONCAT('%', :query, '%'))
			OR LOWER(s.document) LIKE LOWER(CONCAT('%', :query, '%'))
			OR LOWER(s.timbrado) LIKE LOWER(CONCAT('%', :query, '%'))
		  )
		  """)
	Page<Supplier> searchActiveSuppliers(@Param("query") String query,
			   	Pageable pageable);
}
