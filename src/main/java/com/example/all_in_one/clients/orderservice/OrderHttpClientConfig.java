package com.example.all_in_one.clients.orderservice;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
@Slf4j
public class OrderHttpClientConfig {

    @Value("${spring.application.order-service.host}")
    private String orderServiceHost;

    //Rest HTTP Interface
    // https://docs.spring.io/spring-framework/reference/integration/rest-clients.html#rest-http-interface
    @Bean
    OrderHttpClient orderHttpClient(WebClient.Builder webClientBuilder) {
        var webClient = webClientBuilder
                .baseUrl(orderServiceHost)
                .filter((request, next) -> {
                    log.info("WebClient: {} {}", request.method() , request.url());
                    request.headers().forEach((name, values) -> values.forEach(value -> log.info("{} : {}", name , value)));
                    return next.exchange(request);
                })
                .build();
        WebClientAdapter adapter = WebClientAdapter.create(webClient);
        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(adapter).build();
        return factory.createClient(OrderHttpClient.class);
    }

}
