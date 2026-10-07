package com.teraha.purchases.controller;

import com.teraha.commons.dtos.PurchaseInvoiceDTO;
import com.teraha.purchases.service.PurchaseInvoiceService;

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
import org.springframework.data.domain.Page;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j 
@RestController
@RequestMapping(path="/purchases", produces="application/json")
@CrossOrigin
@RequiredArgsConstructor
public class PurchaseInvoiceController {
	private final PurchaseInvoiceService purchaseService;

	@PostMapping(consumes="application/json")
	@ResponseStatus(HttpStatus.CREATED)
	public PurchaseInvoiceDTO postPurchase(@RequestBody PurchaseInvoiceDTO dto){

		log.info("Received a POST request at /purchases");

		return purchaseService.createPurchaseInvoice(dto);
	}

	@GetMapping
	public Page<PurchaseInvoiceDTO> listPurchaseInvoices(@RequestParam(name="page", defaultValue="0") int page, @RequestParam(name="search", required=false) String search){
		
		log.info("Received a GET at /purchases");

		return purchaseService.listPurchases(page);
	}

	@GetMapping("/{id}")
	public PurchaseInvoiceDTO getPurchaseInvoice(@PathVariable("id") Long id){

		log.info("Received GET at /purchases/{}", id);

	 	return purchaseService.getPurchaseInvoice(id);
	}

}
