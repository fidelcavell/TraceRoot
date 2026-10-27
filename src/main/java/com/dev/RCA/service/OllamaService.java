package com.dev.RCA.service;

import com.dev.RCA.model.RootAnalyzeOutput;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class OllamaService {

    @Value("${spring.ai.ollama.base-url}")
    private String ollamaBaseURL;

    @Value("${ollama.api.key}")
    private String apiKey;

    @Value("${spring.ai.ollama.chat.options.model}")
    private String ollamaModel;

    private final RestTemplate restTemplate = new RestTemplate();

    public RootAnalyzeOutput analyzeLogs(String prompt) throws Exception {
        Map<String, Object> requestBody = Map.of(
                "model", ollamaModel,
                "messages", List.of(
                        Map.of(
                                "role", "user",
                                "content", prompt
                        )
                ),
                "stream", false
        );
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setContentType(MediaType.APPLICATION_JSON);
        requestHeaders.setBearerAuth(apiKey);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, requestHeaders);
        ResponseEntity<Map> response = restTemplate.postForEntity(ollamaBaseURL, entity, Map.class);

        Map<String, Object> rawResponse = (Map<String, Object>) response.getBody().get("message");
        ObjectMapper objectMapper = new ObjectMapper();

        String cleanedResponse = Arrays.stream(rawResponse.get("content").toString().split("\\R"))
                .filter(line -> !line.trim().startsWith("//"))
                .collect(Collectors.joining("\n"));

        // For LLM's response checking purpose:
        System.out.println(rawResponse.get("content").toString());

        return objectMapper.readValue(cleanedResponse, new TypeReference<>() {
        });
    }
}
