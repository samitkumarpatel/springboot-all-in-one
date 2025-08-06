package com.example.all_in_one.services;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MetadataService {
    final WebClient.Builder webClientBuilder;

    @Value("${spring.application.metadata.url:https://jsonplaceholder.typicode.com}")
    private String metadataUrl;

    @Value("${spring.application.metadata.uri:/users}")
    private String metadataUri;

    private Mono<ClientResponse> clientResponse() {
        return webClientBuilder
                .baseUrl(metadataUrl)
                .build()
                .get()
                .uri(metadataUri)
                .exchangeToMono(Mono::just);
    }

    public Mono<Map<String,String>> getMetadata() {
        return clientResponse()
                //Find cookie and header from response object and prepare a map
                .map(clientResponse -> {
                    Map<String, String> metadata = clientResponse.headers().asHttpHeaders().entrySet().stream()
                            .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().getFirst()));

                    //Map<String, String> cookieMap = clientResponse.cookies().entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().stream().map(ResponseCookie::getValue).collect(Collectors.joining(","))));


                    MultiValueMap<String, String> cookieMap = new LinkedMultiValueMap<>();

                    System.out.println("clientResponse.cookies() = " + clientResponse.cookies());

                    clientResponse.cookies().forEach((s, httpCookies) -> httpCookies
                            .forEach(httpCookie -> cookieMap.add(httpCookie.getName(), httpCookie.getValue())));
                    metadata.putAll(cookieMap.asSingleValueMap());
                    return metadata;
                });
    }
}
