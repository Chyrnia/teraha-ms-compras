package com.teraha.purchases.controller;

import com.teraha.commons.dtos.SupplierDTO;
import com.teraha.purchases.service.SupplierService;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Tag(
	name = "Suppliers",
	description = "Operations for managing suppliers"
)
@Slf4j 
@RestController
@RequestMapping(path="/suppliers", produces="application/json")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class SupplierController {
	private final SupplierService supplierService;

	@Operation(
		summary = "List suppliers",
		description = "Returns a paginated list of suppliers, optionally filtered by name, document or timbrado"
	)
	@ApiResponse(
		responseCode = "200",
		description = "Suppliers retrieved succesfully"
	)
	@PreAuthorize("hasAnyRole('SELLER','MANAGER','SYSADMIN')")
	@GetMapping
	public Page<SupplierDTO> getSuppliers(
			@Parameter(
				description = "Zero based page-number",
				example = "0"
			)
			@RequestParam(name="page", defaultValue="0") int page, 
			@Parameter(
				description = "Optional search term for supplier name, document or timbrado",
				example = "ACME"
			)
			@RequestParam(name="search", required=false) String search){
		log.info("Received a GET request at /suppliers");
		return supplierService.listSuppliers(page, search);
	}

	@Operation(
		summary = "Get supplier by ID",
		description = "Returns a supplier identified by its database ID"
	)
	@ApiResponses({
		@ApiResponse(
			responseCode = "200",
			description = "Supplier found"
		),
		@ApiResponse(
			responseCode = "404",
			description = "Supplier not found"
		)
	})
	@PreAuthorize("hasAnyRole('SELLER','MANAGER','SYSADMIN')")
	@GetMapping("/{id}")
	public ResponseEntity<SupplierDTO> findSupplierById(
			@Parameter(
				description = "Supplier ID",
				example = "2",
				required = true
			)
			@PathVariable("id") Long id){

		log.info("Received a GET request at /suppliers/{}", id);

		SupplierDTO supplier = supplierService.findSupplierById(id);
		return new ResponseEntity<>(supplier, HttpStatus.OK);
	}

	@Operation(
		summary = "Create a new supplier",
		description = "Creates a new supplier"
	)
	@ApiResponses({
		@ApiResponse(
			responseCode = "201",
			description = "Supplier created succesfully"
		),
		@ApiResponse(
			responseCode = "400",
			description = "Invalid supplier data"
		),
		@ApiResponse(
			responseCode = "409",
			description = "A supplier with the specified document already exists"
		)
	})
	@PreAuthorize("hasAnyRole('MANAGER','SYSADMIN')")
	@PostMapping(consumes="application/json")
	@ResponseStatus(HttpStatus.CREATED)
	public SupplierDTO postSupplier(
			@RequestBody SupplierDTO dto){

		log.info("Received a POST request at /suppliers");

		return supplierService.createSupplier(dto);
	}

	@Operation(
        summary = "Deactivate supplier",
        description = "Logically deactivates a supplier. The supplier remains stored for historical purposes."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "204",
            description = "Supplier deactivated successfully"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Supplier not found"
        )
    })
	@PreAuthorize("hasAnyRole('MANAGER','SYSADMIN')")
	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deleteSupplier(
			@Parameter(
				description = "Supplier ID",
				example = "1",
				required = true
			)
			@PathVariable("id") Long id){

		log.info("Received DELETE request at /suppliers/{}", id);

		supplierService.deactivateSupplier(id);
	}
}
