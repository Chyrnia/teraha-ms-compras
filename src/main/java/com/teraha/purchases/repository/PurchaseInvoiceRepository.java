package com.teraha.purchases.repository;

import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Lock;

import jakarta.persistence.LockModeType;

import com.teraha.commons.entities.PurchaseInvoice;

public interface PurchaseInvoiceRepository extends InvoiceRepository<PurchaseInvoice> {
	@Query(
		value = """
			SELECT DISTINCT p
			FROM PurchaseInvoice p
			LEFT JOIN p.thirdParty s
			LEFT JOIN p.details d
			LEFT JOIN d.product pr
			WHERE
				LOWER(p.invoiceNumber) LIKE LOWER(CONCAT('%', :query, '%'))
				OR LOWER(s.name) LIKE LOWER(CONCAT('%', :query, '%'))
				OR LOWER(s.document) LIKE LOWER(CONCAT('%', :query, '%'))
				OR LOWER(pr.code) LIKE LOWER(CONCAT('%', :query, '%'))
				OR LOWER(pr.description) LIKE LOWER(CONCAT('%', :query, '%'))
			""",
		countQuery = """
			SELECT COUNT(DISTINCT p.id)
			FROM PurchaseInvoice p
			LEFT JOIN p.thirdParty s
			LEFT JOIN p.details d
			LEFT JOIN d.product pr
			WHERE
				LOWER(p.invoiceNumber) LIKE LOWER(CONCAT('%', :query, '%'))
				OR LOWER(s.name) LIKE LOWER(CONCAT('%', :query, '%'))
				OR LOWER(s.document) LIKE LOWER(CONCAT('%', :query, '%'))
				OR LOWER(pr.code) LIKE LOWER(CONCAT('%', :query, '%'))
				OR LOWER(pr.description) LIKE LOWER(CONCAT('%', :query, '%'))
			"""
	)
	Page<PurchaseInvoice> searchPurchases(
		@Param("query") String query,
		Pageable pageable
	);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("SELECT pi FROM PurchaseInvoice pi WHERE pi.id = :id")
	Optional<PurchaseInvoice> findByIdForUpdate(@Param("id")Long id);

}
