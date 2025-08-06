package com.example.all_in_one.models;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class MessageJsonTest {
    @Autowired
    JacksonTester<Message> json;

    @Test
    @SneakyThrows
    void messageSerializeTest() {
        Message details = new Message("Hello", false);
        assertThat(json.write(details))
                .isEqualToJson("""
                        {
                          "message": "Hello",
                          "isActive": false
                        }
                        """);
    }
}
