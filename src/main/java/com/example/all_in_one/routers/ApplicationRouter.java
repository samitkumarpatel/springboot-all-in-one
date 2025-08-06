package com.example.all_in_one.routers;

import com.example.all_in_one.handlers.CustomerHandler;
import com.example.all_in_one.handlers.MessageHandler;
import com.example.all_in_one.handlers.MetadataHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
public class ApplicationRouter {

    @Bean
    RouterFunction<ServerResponse> routerFunction(MessageHandler messageHandlers, CustomerHandler customerHandler, MetadataHandler metadataHandler) {
        return RouterFunctions
                .route()
                .GET("/metadata", metadataHandler::apiMetadata)
                .path("/message", messageBuilder -> messageBuilder
                        .GET("", messageHandlers::greet)
                )
                .path("/customer", customerBuilder -> customerBuilder
                        .GET("", request -> ServerResponse.noContent().build())
                        .POST("", customerHandler::saveCustomer)
                        .GET("/{id}", customerHandler::customerById)
                        .PUT("/{id}", request -> ServerResponse.noContent().build())
                        .DELETE("/{id}", request -> ServerResponse.noContent().build())
                )
                .build();
    }
}
