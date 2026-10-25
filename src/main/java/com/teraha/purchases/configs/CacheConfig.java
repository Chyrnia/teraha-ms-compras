package com.teraha.purchases.config;

import java.time.Duration;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair;

import com.teraha.commons.dtos.SupplierDTO;
import com.teraha.commons.dtos.PurchaseInvoiceDTO;

import tools.jackson.databind.ObjectMapper;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
	public CacheManager cacheManager(
			RedisConnectionFactory connectionFactory,
			ObjectMapper objectMapper,
			@Value("${app.cache.ttl.default-seconds}") long defaultTtl,
			@Value("${app.cache.ttl.suppliers-seconds}") long suppliersTtl,
			@Value("${app.cache.ttl.purchase-invoices-seconds}") long invoicesTtl) {

			RedisCacheConfiguration defaults =
				RedisCacheConfiguration.defaultCacheConfig()
				.entryTtl(Duration.ofSeconds(defaultTtl))
				.disableCachingNullValues();

			RedisCacheConfiguration suppliersCache =
				defaults
				.entryTtl(Duration.ofSeconds(suppliersTtl))
				.serializeValuesWith(
						SerializationPair.fromSerializer(
							new JacksonJsonRedisSerializer<>(
								objectMapper, SupplierDTO.class)));

			RedisCacheConfiguration invoicesCache =
				defaults
				.entryTtl(Duration.ofSeconds(invoicesTtl))
				.serializeValuesWith(
						SerializationPair.fromSerializer(
							new JacksonJsonRedisSerializer<>(
								objectMapper, PurchaseInvoiceDTO.class)));

			RedisCacheManager redisManager =
				RedisCacheManager.builder(connectionFactory)
				.cacheDefaults(defaults)
				.withInitialCacheConfigurations(Map.of(
							"purchases:suppliers", suppliersCache,
							"purchases:invoices", invoicesCache))
				.transactionAware()
				.build();

			return redisManager;
			}
}
