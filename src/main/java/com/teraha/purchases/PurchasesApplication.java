package com.teraha.purchases;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.boot.restclient.RestTemplateBuilder;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;

import org.springframework.web.client.RestTemplate;
import org.springframework.context.annotation.Bean;


@SpringBootApplication
@EntityScan(basePackages = "com.teraha.commons.entities")
public class PurchasesApplication {

	public static void main(String[] args) {
		SpringApplication.run(PurchasesApplication.class, args);
	}

	@Bean
	@LoadBalanced
	public RestTemplate restTemplate(RestTemplateBuilder builder){
		return builder.build();
	}

}
