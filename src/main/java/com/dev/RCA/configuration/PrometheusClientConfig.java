package com.dev.RCA.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class PrometheusClientConfig {

    @Value("${prometheus.query.base.url}")
    private String prometheusBaseURL;

    @Bean
    public WebClient prometheusWebClient(WebClient.Builder builder) {
        return builder
                .baseUrl(prometheusBaseURL)
                .build();
    }
}
