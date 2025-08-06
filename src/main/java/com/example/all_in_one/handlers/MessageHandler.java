package com.example.all_in_one.handlers;

import com.example.all_in_one.services.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;


@Component
@RequiredArgsConstructor
public class MessageHandler {
    final MessageService messageServices;

    public Mono<ServerResponse> greet(ServerRequest request) {
        return messageServices
                .greet()
                .flatMap(ServerResponse.ok()::bodyValue);
    }
}
