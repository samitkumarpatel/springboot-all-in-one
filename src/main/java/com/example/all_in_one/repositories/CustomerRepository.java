package com.example.all_in_one.repositories;

import com.example.all_in_one.models.Customer;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface CustomerRepository extends ReactiveCrudRepository<Customer, Long>{
}
