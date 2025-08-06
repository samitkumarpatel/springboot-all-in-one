package com.example.all_in_one.models;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class CustomerJsonTest {
    @Autowired
    JacksonTester<Customer> json;

    @Test
    @SneakyThrows
    void customerSerializeTest() {
       Customer customer = Customer.builder()
               .id(1L)
               .firstName("John")
               .lastName("Doe")
               .age(25)
               //.dateOfBirth(LocalDate.of(1996, 1, 1))
               .active(true)
               .build();
        assertThat(json.write(customer))
                .isEqualToJson("""
                        {
                          "id": 1,
                          "firstName": "John",
                          "lastName": "Doe",
                          "age": 25,
                          "isActive": true
                        }
                        """);
    }

}
