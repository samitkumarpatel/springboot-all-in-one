package com.example.all_in_one.services;

import com.example.all_in_one.clients.orderservice.OrderHttpClient;
import com.example.all_in_one.clients.orderservice.Order;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {
    final OrderHttpClient orderHttpClient;

    public Mono<Order> fetchCustomerOrder(Long id) {
        log.info("OrderService::fetchCustomerOrder: {}", id);
        return orderHttpClient
                .getOrder(id)
                .retry(3);
    }

}
