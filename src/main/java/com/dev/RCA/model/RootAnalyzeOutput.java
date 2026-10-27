package com.dev.RCA.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class RootAnalyzeOutput {

    @JsonProperty("@timestamp")
    private String timestamp;

    @JsonProperty("summary")
    private String summary;

    @JsonProperty("root_cause_list")
    private List<RootAnalyzeItem> rootCauseList;
}
