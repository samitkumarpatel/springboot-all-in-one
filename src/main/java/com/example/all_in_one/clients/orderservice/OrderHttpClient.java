package com.example.all_in_one.clients.orderservice;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import reactor.core.publisher.Mono;

@HttpExchange(url = "/order", contentType = MediaType.APPLICATION_JSON_VALUE)
public interface OrderHttpClient {
    @GetExchange("/{id}")
    Mono<Order> getOrder(@PathVariable Long id);

}
