package com.example.all_in_one.routers;

import com.example.all_in_one.config.KafkaPubSubConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest
//@SpringBootTest(properties = {"spring.application.messages.basicGreetMessage=from-springboot-test"})
@AutoConfigureWebTestClient
@WithMockUser
public class MessageRouterTest {
    @MockitoBean
    KafkaPubSubConfiguration kafkaPubSubConfiguration;

    @Autowired
    // https://docs.spring.io/spring-framework/reference/testing/webtestclient.html
    WebTestClient webTestClient;

    @DynamicPropertySource
    static void setDynamicProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.application.messages.basicGreetMessage", () -> "from-dynamic-property");
    }

    @Test
    void getRouterTest() {
        webTestClient
                .get()
                .uri("/message")
                .exchange()
                .expectStatus().isOk()
//                .expectAll(
//                        r -> r.expectBody().json(""),
//                        r -> r.expectBody().jsonPath("$.isActive").isEqualTo(true)
//                )

                .expectBody()
                .json("""
                    {
                        "isActive":true,
                        "message":"from-dynamic-property"
                    }
                """);
//                .expectBody(Message.class)
//                .isEqualTo(new Message("Hello, There!"));
    }
}
