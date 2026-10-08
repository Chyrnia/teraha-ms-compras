package com.teraha.purchases.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
}
