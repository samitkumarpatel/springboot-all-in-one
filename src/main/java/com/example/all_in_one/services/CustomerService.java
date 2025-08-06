package com.example.all_in_one.services;

import com.example.all_in_one.models.Customer;
import com.example.all_in_one.repositories.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerService {

    final CustomerRepository customerRepository;
    final OrderService orderService;

    public Mono<Customer> fetchCustomer(Long id) {
        log.info("CustomerService::fetchCustomer: {}", id);
        return customerRepository.findById(id)
                .flatMap(customer -> orderService.fetchCustomerOrder(id)
                        .map(order -> {
                            customer.setOrder(order);
                            log.info("CustomerService::fetchCustomer: {}", customer);
                            return customer;
                        })
                        .onErrorResume(e -> Mono.just(customer))
                );
    }

    public Mono<Customer> saveCustomer(Customer customer) {
        log.info("CustomerService::saveCustomer: {}", customer);
        return customerRepository.save(customer);
    }
}
