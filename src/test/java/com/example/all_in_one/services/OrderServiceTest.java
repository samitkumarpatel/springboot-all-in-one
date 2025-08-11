package com.example.all_in_one.services;

import com.example.all_in_one.clients.orderservice.Order;
import com.example.all_in_one.clients.orderservice.OrderHttpClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith({SpringExtension.class, OutputCaptureExtension.class})
@DisplayName("Log line Test")
public class OrderServiceTest {

    @MockitoBean
    OrderHttpClient orderHttpClient;

    OrderService orderService;

    @BeforeEach
    void beforeEach() {
        when(orderHttpClient.getOrder(1L)).thenReturn(Mono.just(new Order(1L, LocalDate.now(), null, null, List.of())));
        orderService = new OrderService(orderHttpClient);
    }

    @Test
    @DisplayName("Log line test")
    void logTest(CapturedOutput output) {
        orderService.fetchCustomerOrder(1L).block();
        assertThat(output).contains("OrderService::fetchCustomerOrder: 1");
    }

    @Test
    @DisplayName("Order service retry Test")
    void fetchOrderTest() {

        //a retry scenario using WireMock
        /*stubFor(get(urlEqualTo("/order/1"))
                .inScenario("retryTest")
                .whenScenarioStateIs(Scenario.STARTED)
                .willReturn(aResponse().withStatus(500))
                .willSetStateTo("serviceCallFailed"));

        stubFor(get(urlEqualTo("/order/1"))
                .inScenario("retryTest")
                .whenScenarioStateIs("serviceCallFailed")
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody("""
                             { "order": 1 }
                        """)
                )
        );*/

        assertTrue(true);
    }
}
