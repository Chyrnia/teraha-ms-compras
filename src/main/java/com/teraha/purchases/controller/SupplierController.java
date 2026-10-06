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
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.data.domain.Page;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j 
@RestController
@RequestMapping(path="/suppliers", produces="application/json")
@CrossOrigin
@RequiredArgsConstructor
public class SupplierController {
	private final SupplierService supplierService;

	@GetMapping
	public Page<SupplierDTO> getSuppliers(@RequestParam(name="page", defaultValue="0") int page, @RequestParam(name="search", required="false") String search){
		//TODO: have the search param in case I have time later. Wont hurt noone.
		log.info("Received a GET request at /suppliers");
		return supplierService.listSuppliers(page);
	}

	@GetMapping("/{id}")
	public ResponseEntity<SupplierDTO> findSupplierById(@PathVariable("id") Long id){

		log.info("Received a GET request at /suppliers/{}", id);

		SupplierDTO supplier = supplierService.findSupplierById(id);
		return new ResponseEntity<>(supplier, HttpStatus.OK);
	}

	@PostMapping(consumes="application/json")
	@ResponseStatus(HttpStatus.CREATED)
	public SupplierDTO postSupplier(@RequestBody SupplierDTO dto){

		log.info("Received a POST request at /suppliers");

		return supplierService.createSupplier(dto);
	}
}
