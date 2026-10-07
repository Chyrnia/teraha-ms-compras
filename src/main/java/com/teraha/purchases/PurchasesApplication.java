package com.teraha.purchases;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
impotr org.springframework.web.client.RestTemplate;
impotr org.springframework.context.annotation.Bean;


@SpringBootApplication
@EntityScan(basePackages = "com.teraha.commons.entities")
public class PurchasesApplication {

	public static void main(String[] args) {
		SpringApplication.run(PurchasesApplication.class, args);
	}

	@Bean
	public RestTemplate restTemplate(){
		return new RestTemplate();
	}

}
