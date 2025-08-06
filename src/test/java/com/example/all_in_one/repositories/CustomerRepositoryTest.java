package com.example.all_in_one.repositories;

import com.example.all_in_one.models.Customer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import reactor.test.StepVerifier;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataR2dbcTest
@Testcontainers
public class CustomerRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(DockerImageName.parse("postgres:latest"))
            .withInitScript("db/schema.sql")
            .waitingFor(Wait.forListeningPort());

    @Autowired
    private CustomerRepository customerRepository;

    @Test
    void customerCrudTest() {
        assertAll(
                //CREATE
                () -> customerRepository
                        .save(Customer.builder().firstName("John").lastName("Doe").dateOfBirth(LocalDate.now()).build())
                        .as(StepVerifier::create)
                        .consumeNextWith(customer -> {
                            assertNotNull(customer.getId());
                            System.out.println(customer);
                        })
                        .verifyComplete(),
                //READ
                () -> customerRepository
                        .findById(1L)
                        .as(StepVerifier::create)
                        .consumeNextWith(customer -> {
                            assertNotNull(customer.getId());
                            assertEquals("John", customer.getFirstName());
                            assertEquals("Doe", customer.getLastName());
                            System.out.println(customer);
                        })
                        .verifyComplete(),
                //UPDATE
                () -> customerRepository
                        .save(Customer.builder().id(1L).firstName("Jane").lastName("Doe").dateOfBirth(LocalDate.now()).build())
                        .as(StepVerifier::create)
                        .consumeNextWith(customer -> {
                            assertNotNull(customer.getId());
                            assertEquals("Jane", customer.getFirstName());
                            assertEquals("Doe", customer.getLastName());
                            System.out.println(customer);
                        })
                        .verifyComplete(),
                //READ
                () -> customerRepository
                        .findById(1L)
                        .as(StepVerifier::create)
                        .consumeNextWith(customer -> {
                            assertNotNull(customer.getId());
                            assertEquals("Jane", customer.getFirstName());
                            assertEquals("Doe", customer.getLastName());
                            System.out.println(customer);
                        })
                        .verifyComplete(),
                //FIND ALL
                () -> customerRepository
                        .findAll()
                        .as(StepVerifier::create)
                        .expectNextCount(1)
                        .verifyComplete(),
                //DELETE
                () -> customerRepository
                        .deleteById(1L)
                        .as(StepVerifier::create)
                        .verifyComplete()

        );
    }
}
