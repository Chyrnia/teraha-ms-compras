package com.teraha.purchases.repository;

import org.springframework.data.repository.NoRepositoryBean;
import com.teraha.commons.entities.Invoice;

@NoRepositoryBean
public interface InvoiceRepository<E extends Invoice> extends BaseRepository<E> {}
