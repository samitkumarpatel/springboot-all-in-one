package com.example.all_in_one.services;

import com.example.all_in_one.clients.orderservice.Order;
import com.example.all_in_one.clients.orderservice.OrderHttpClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith({SpringExtension.class, OutputCaptureExtension.class})
@DisplayName("Log line Test")
public class OrderServiceTest {

    @MockBean
    OrderHttpClient orderHttpClient;

    OrderService orderService;

    @BeforeEach
    void beforeEach() {
        Mockito.when(orderHttpClient.getOrder(1L)).thenReturn(Mono.just(new Order(1L, LocalDate.now(), null, null, List.of())));
        orderService = new OrderService(orderHttpClient);
    }
    @Test
    void logTest(CapturedOutput output) {
        orderService.fetchCustomerOrder(1L).block();
        assertThat(output).contains("OrderService::fetchCustomerOrder: 1");
    }
}
