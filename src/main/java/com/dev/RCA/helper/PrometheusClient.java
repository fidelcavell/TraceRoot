package com.dev.RCA.helper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class PrometheusClient {

    @Autowired
    private WebClient prometheusClient;

    public Mono<String> instantQuery(String promQl) {
        return prometheusClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/query")
                        .queryParam("query", promQl)
                        .build())
                .retrieve()
                .bodyToMono(String.class);
    }
}
