package com.dev.RCA.helper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.OptionalDouble;

@Component
public class PrometheusMetricParser {

    private final ObjectMapper mapper = new ObjectMapper();

    public OptionalDouble parseSingleValue(String json) {
        try {
            JsonNode result = mapper.readTree(json)
                    .path("data")
                    .path("result");

            if (!result.isArray() || result.isEmpty()) {
                return OptionalDouble.empty();
            }
            return OptionalDouble.of(
                    result.get(0)
                            .path("value")
                            .get(1)
                            .asDouble()
            );

        } catch (Exception e) {
            return OptionalDouble.empty();
        }
    }

    public Map<String, Double> parseLabeledValues(String json, String labelName) {
        Map<String, Double> values = new HashMap<>();

        try {
            JsonNode results = mapper.readTree(json)
                    .path("data")
                    .path("result");

            for (JsonNode item : results) {
                String label = item.path("metric")
                        .path(labelName)
                        .asText(null);

                if (label != null) {
                    double value = item.path("value")
                            .get(1)
                            .asDouble();
                    values.put(label, value);
                }
            }

        } catch (Exception ignored) {}
        return values;
    }
}
