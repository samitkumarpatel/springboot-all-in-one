package com.example.all_in_one.services;

import com.example.all_in_one.models.Message;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class MessageService {

    @Value("${spring.application.messages.basicGreetMessage}")
    private String basicGreetingMessage;

    public Mono<Message> greet() {
        return Mono.fromCallable(() -> new Message(basicGreetingMessage, true));
    }
}
