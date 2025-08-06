package com.example.all_in_one.handlers;

import com.example.all_in_one.models.Customer;
import com.example.all_in_one.services.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class CustomerHandler {
    final CustomerService customerService;

    public Mono<ServerResponse> customerById(ServerRequest request) {
        Long id = Long.valueOf(request.pathVariable("id"));

        return customerService.fetchCustomer(id)
                .flatMap(ServerResponse.ok()::bodyValue)
                .switchIfEmpty(ServerResponse.notFound().build());
    }

    @PreAuthorize("hasRole('ADMIN')")
    public Mono<ServerResponse> saveCustomer(ServerRequest request) {
        return request.bodyToMono(Customer.class)
                .flatMap(customerService::saveCustomer)
                .flatMap(ServerResponse.ok()::bodyValue);
    }
}
