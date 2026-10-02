package com.teraha.purchases.repository;

import org.springframework.data.repository.NoRepositoryBean;
import com.teraha.commons.entities.Person;

@NoRepositoryBean
public interface PersonRepository<E extends Person> extends BaseRepository<E> {}
