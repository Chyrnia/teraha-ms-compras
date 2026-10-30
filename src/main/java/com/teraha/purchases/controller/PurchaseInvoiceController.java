package com.teraha.purchases.controller;

import com.teraha.commons.dtos.PurchaseInvoiceDTO;
import com.teraha.commons.dtos.ErrorResponse;
import com.teraha.purchases.service.PurchaseInvoiceService;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.http.HttpStatus;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Tag(
	name = "Purchase Invoices",
	description = "Operations for creating and consulting purchase invoices"
	)
@Slf4j 
@RestController
@RequestMapping(path="/purchases", produces="application/json")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class PurchaseInvoiceController {
	private final PurchaseInvoiceService purchaseService;

	@Operation(
		summary = "Creates a purchase invoice",
		description = "Creates a purchase invoice, persists its details, calculates the total and updates product stock"
	)
	@ApiResponses({
		@ApiResponse(
			responseCode = "201",
			description = "Purchase invoice created succesfully"
			),
		@ApiResponse(
			responseCode = "400",
			description = "Invalid purchase invoice data",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = ErrorResponse.class )
				)
		),
		@ApiResponse(
			responseCode = "404",
			description = "Supplier or Product not found",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = ErrorResponse.class )
				)
		)
	})
	@PreAuthorize("hasAnyRole('SELLER','MANAGER','SYSADMIN')")
	@PostMapping(consumes="application/json")
	@ResponseStatus(HttpStatus.CREATED)
	public PurchaseInvoiceDTO postPurchase(
		@io.swagger.v3.oas.annotations.parameters.RequestBody(
			description = "Purchase invoice to create",
			required = true
		) 
		@RequestBody PurchaseInvoiceDTO dto){

		log.info("Received a POST request at /purchases");

		return purchaseService.createPurchaseInvoice(dto);
	}

	@Operation(
		summary = "List purchase invoices",
		description = "Returns purchase invoices using pagination and an optional search term"
	)
	@ApiResponses({
		@ApiResponse(
			responseCode = "200",
			description = "Purchase invoice retrieved succesfully"
		)
	})
	@PreAuthorize("hasAnyRole('SELLER','MANAGER','SYSADMIN')")
	@GetMapping
	public Page<PurchaseInvoiceDTO> listPurchaseInvoices(
			@Parameter(
				description = "Zero based page number",
				example = "0"
			)
			@RequestParam(name="page", defaultValue="0") int page, 
			@Parameter(
				description = "Searches by invoice number, supplier, or product information",
				example = "Pants"
			)
			@RequestParam(name="search", required=false) String search
			){
		
		log.info("Received a GET at /purchases");

		return purchaseService.listPurchases(page, search);
	}

	@Operation(
		summary = "Get a purchase invoice by ID",
		description = "Returns a purchase invoice identified by its database ID"
	)
	@ApiResponses({
		@ApiResponse(
			responseCode = "200",
			description = "Purchase invoice found"
		),
		@ApiResponse(
			responseCode = "404",
			description = "Purchase invoice not found",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = ErrorResponse.class)
				)
		)
	})
	@PreAuthorize("hasAnyRole('SELLER','MANAGER','SYSADMIN')")
	@GetMapping("/{id}")
	public PurchaseInvoiceDTO getPurchaseInvoice(
		@Parameter(
			description = "Purchase invoice ID",
			example = "3",
			required = true
		)
		@PathVariable("id") Long id){

		log.info("Received GET at /purchases/{}", id);

	 	return purchaseService.getPurchaseInvoice(id);
	}

}
