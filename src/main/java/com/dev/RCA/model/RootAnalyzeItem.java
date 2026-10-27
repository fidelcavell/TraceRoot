package com.dev.RCA.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class RootAnalyzeItem {
    @JsonProperty("@timestamp")
    private String timestamp;

    @JsonProperty("root_cause")
    private String rootCause;

    @JsonProperty("affected_component")
    private String affectedComponent;

    @JsonProperty("fix_steps")
    private List<String> fixSteps;

    @JsonProperty("incident_summary")
    private String incidentSummary;

    @JsonProperty("prevention_recommendation")
    private String preventionRecommendation;

    @JsonProperty("contributing_factors")
    private String contributingFactors;

    @JsonProperty("level")
    private String level;

    @JsonProperty("confidence")
    private double confidence;
}
