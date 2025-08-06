package com.example.all_in_one.routers;

import com.example.all_in_one.config.KafkaPubSubConfiguration;
import com.github.tomakehurst.wiremock.stubbing.Scenario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.HttpHeaders;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@SpringBootTest
@Testcontainers
@AutoConfigureWebTestClient
@AutoConfigureWireMock(port = 0)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CustomerRouterTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(DockerImageName.parse("postgres:latest"))
            .withInitScript("db/schema-n-data.sql")
            .waitingFor(Wait.forListeningPort());

    @DynamicPropertySource
    static void applicationProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.application.order-service.host", () -> "http://localhost:${wiremock.server.port}");
        registry.add("spring.application.metadata.url", () -> "http://localhost:${wiremock.server.port}");
        registry.add("spring.application.metadata.uri", () -> "/metadata");
    }

    @MockitoBean
    KafkaPubSubConfiguration kafkaPubSubConfiguration;

    @BeforeEach
    void setUp() {
        //wiremock
        stubFor(get(urlPathEqualTo("/order/1"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody("""
                                    {
                                        "id": 1,
                                        "placedAt": "2021-08-01",
                                        "expectedDeliveryDate": "2021-08-04",
                                        "deliveredDate": null,
                                        "items": ["item1", "item2"]
                                    }
                                """)
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)));
    }


    @Test
    @DisplayName("Order service retry test")
    @Order(1)
    @WithMockUser
    void orderServiceRetryTest(@Autowired WebTestClient webTestClient) {
        //https://wiremock.org/docs/stateful-behaviour/

        // 1st attempt
        stubFor(get(urlPathEqualTo("/order/1"))
                .inScenario("retryTest")
                .whenScenarioStateIs(Scenario.STARTED)
                .willReturn(aResponse().withStatus(500))
                .willSetStateTo("serviceCallFailed"));

        // 2nd attempt
        stubFor(get(urlPathEqualTo("/order/1"))
                .inScenario("retryTest")
                .whenScenarioStateIs("serviceCallFailed")
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody("""
                                    {
                                        "id": 1,
                                        "placedAt": "2021-08-01",
                                        "expectedDeliveryDate": "2021-08-04",
                                        "deliveredDate": null,
                                        "items": ["item1"]
                                    }
                                """)
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)));

        webTestClient
                .get()
                .uri("/customer/{id}", 1)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(1)
                .jsonPath("$.order.id").isEqualTo(1)
                .json("""
                        {
                            "id":1,
                            "firstName":"John",
                            "lastName":"Doe",
                            "age":30,
                            "dateOfBirth":"1990-01-01",
                            "order":{
                                "id":1,
                                "placedAt":"2021-08-01",
                                "expectedDeliveryDate":"2021-08-04",
                                "deliveredDate":null,
                                "items":["item1"]
                            },
                            "isActive":true
                        }
                """);

        verify(2, getRequestedFor(urlEqualTo("/order/1")));
    }

    @Test
    @Order(2)
    @WithMockUser
    void employeeRouterTest(@Autowired WebTestClient webTestClient) {
        webTestClient
                .get()
                .uri("/customer/{id}", 1)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(1)
                .jsonPath("$.order.id").isEqualTo(1)
                .json("""
                        {
                            "id":1,
                            "firstName":"John",
                            "lastName":"Doe",
                            "age":30,
                            "dateOfBirth":"1990-01-01",
                            "order":{
                                "id":1,
                                "placedAt":"2021-08-01",
                                "expectedDeliveryDate":"2021-08-04",
                                "deliveredDate":null,
                                "items":["item1","item2"]
                            },
                            "isActive":true
                        }
                """);

    }

    @Test
    @Order(3)
    @WithMockUser(roles = "ADMIN")
    //or
    //webTestClient.mutateWith(SecurityMockServerConfigurers.mockJwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))).post()...
    void saveCustomerRouterTest(@Autowired WebTestClient webTestClient) {
        webTestClient
                //.mutateWith(SecurityMockServerConfigurers.mockJwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                .post()
                .uri("/customer")
                .header(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .bodyValue("""
                        {
                            "firstName":"John",
                            "lastName":"Doe",
                            "age":30,
                            "dateOfBirth":"1990-01-01",
                            "isActive":true
                        }
                """)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.id").isNotEmpty()
                .jsonPath("$.firstName").isEqualTo("John")
                .jsonPath("$.lastName").isEqualTo("Doe")
                .jsonPath("$.age").isEqualTo(30)
                .jsonPath("$.dateOfBirth").isEqualTo("1990-01-01")
                .jsonPath("$.isActive").isEqualTo(true);
    }

    @Test
    @Order(3)
    @WithMockUser(roles = "USER")
    void saveCustomerRouterTest01(@Autowired WebTestClient webTestClient) {
        webTestClient
                //.mutateWith(SecurityMockServerConfigurers.mockJwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                .post()
                .uri("/customer")
                .header(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .bodyValue("""
                        {
                            "firstName":"John",
                            "lastName":"Doe",
                            "age":30,
                            "dateOfBirth":"1990-01-01",
                            "isActive":true
                        }
                """)
                .exchange()
                .expectStatus()
                .isForbidden();
    }

    @Test
    @Order(4)
    @WithMockUser
    void metadataRouterTest(@Autowired WebTestClient webTestClient) {
        stubFor(get(urlPathEqualTo("/metadata"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Custom-Header", "Custom-Value")
                        .withHeader(HttpHeaders.SET_COOKIE, "cookie1=value1;Path=/;HttpOnly","cookie2=value2;Path=/;HttpOnly")
                )
        );

        webTestClient
                .get()
                .uri("/metadata")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.Transfer-Encoding").isEqualTo("chunked")
                .jsonPath("$.Custom-Header").isEqualTo("Custom-Value")
                .jsonPath("$.Set-Cookie").isEqualTo("cookie1=value1;Path=/;HttpOnly")
                .jsonPath("$.Vary").isEqualTo("Accept-Encoding, User-Agent")
                .jsonPath("$.cookie1").isEqualTo("value1")
                .jsonPath("$.cookie2").isEqualTo("value2");
    }
}
