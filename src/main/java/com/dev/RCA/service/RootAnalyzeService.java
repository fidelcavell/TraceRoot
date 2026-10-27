package com.dev.RCA.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import com.dev.RCA.model.RootAnalyzeItem;
import com.dev.RCA.model.RootAnalyzeOutput;
import com.dev.RCA.model.SystemMetric;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.*;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class RootAnalyzeService {

    @Autowired
    private OllamaService ollamaService;

    @Autowired
    private ElasticsearchClient elasticsearchClient;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // To generate Root Analyze based on given logs from ollama AI model.
    public RootAnalyzeOutput generateRootAnalyze(List<Map<String, Object>> logs, SystemMetric metric) throws Exception {
        String prompt = buildPromptContext(logs, metric);
        return ollamaService.analyzeLogs(prompt);
    }

    public RootAnalyzeOutput generateRootAnalyzeWithTimestamp(RootAnalyzeOutput rootAnalyzeOutput) {
        String currentTime = OffsetDateTime
                .now(ZoneId.of("Asia/Jakarta"))
                .toString();

        if (rootAnalyzeOutput.getTimestamp() == null) {
            rootAnalyzeOutput.setTimestamp(currentTime);
        }

        if (rootAnalyzeOutput.getRootCauseList() != null) {
            for (RootAnalyzeItem rc : rootAnalyzeOutput.getRootCauseList()) {
                if (rc.getTimestamp() == null) {
                    rc.setTimestamp(currentTime);
                }
            }
        }
        return rootAnalyzeOutput;
    }

    // To add new indices that contains Root Analyze JSON result to elasticsearch index.
    public void addIndexRootAnalyzeOutput(RootAnalyzeOutput rootAnalyzeOutput) throws IOException {

        if (rootAnalyzeOutput == null) {
            return;
        }

        Map<String, Object> document = objectMapper.convertValue(rootAnalyzeOutput, Map.class);
        document.put("@timestamp", OffsetDateTime
                .now(ZoneId.of("Asia/Jakarta"))
                .toInstant()
                .toString());

        IndexRequest<Map<String, Object>> request = IndexRequest.of(i -> i
                .id(UUID.randomUUID().toString())
                .index("traceroot-log-rca-result-" + LocalDate.now(ZoneOffset.UTC))
                .document(document)
        );

        elasticsearchClient.index(request);
    }

    public void addIndexRootAnalyzeOutputWithTimeFrame(RootAnalyzeOutput rootAnalyzeOutput) throws IOException {

        if (rootAnalyzeOutput == null) {
            return;
        }

        Map<String, Object> document = objectMapper.convertValue(rootAnalyzeOutput, Map.class);
        document.put("@timestamp", OffsetDateTime
                .now(ZoneId.of("Asia/Jakarta"))
                .toInstant()
                .toString());

        IndexRequest<Map<String, Object>> request = IndexRequest.of(i -> i
                .id(UUID.randomUUID().toString())
                .index("traceroot-log-rca-time-frame-" + LocalDate.now(ZoneOffset.UTC))
                .document(document)
        );

        elasticsearchClient.index(request);
    }

    public String buildPromptContext(List<Map<String, Object>> logs, SystemMetric metric) {
        StringBuilder promptContext = new StringBuilder();
        promptContext.append("""
                You are a Senior Site Reliability Engineer (SRE).
                     
                     Analyze the following Spring Boot application logs and produce a step-by-step Root Cause Analysis (RCA).
                     
                     STRICT RULES:
                     - Return ONLY valid JSON.
                     - Output MUST be a COMPLETE and NON-TRUNCATED JSON object.
                     - NEVER output partial JSON.
                     - NEVER omit required fields.
                     - NEVER shorten, summarize, or compress output to save space.
                     - ALL strings MUST be single-line strings.
                     - Newlines MUST be escaped as \\\\n.
                     - Tabs MUST be escaped as \\\\t.
                     - Do NOT include raw stack traces.
                     - Do NOT include comments (// or /* */).
                     - Do NOT include markdown or explanations outside JSON.
                     - The response MUST NOT contain comments, placeholders, ellipsis, ranges, or references to omitted content in any form.
                     - Forbidden patterns include (but are not limited to): "//", "/*", "*/", "...", "entries", "would continue", "omitted", "for brevity", "same pattern", "remaining logs"
                     - Response MUST DO LOG #1 maps to root_cause_list[0], LOG #2 maps to root_cause_list[1], etc.
                     - Response MUST NOT include line starting with "//" or "/*" (FORBIDDEN, IF STILL THEN DO NOT INCLUDE THAT LINE AT ANY COST).
                     - Do NOT include line of comment in the response output.
                     
                     ANALYSIS RULES:
                     - Do NOT invent facts not explicitly supported by log evidence.
                     - Logical inference is allowed ONLY when directly supported by log content.
                     - If the root cause cannot be determined with high certainty, explicitly state the assumption.
                     - If a log contains no error or warning indicators:
                       - Set root_cause to "No incident detected"
                       - Set confidence to 0.9
                     - severity_score is derived from log level:
                       - ERROR = 3
                       - WARN = 2
                       - INFO = 1
                     - Fix steps MUST be concrete and verifiable actions, NOT generic advice.
                     - Add in "summary" a comprehensive description of the detected root causes.
                     - Add in "summary" as the LAST sentence whether the system metric caused the error or not.
                     
                     ANALYSIS REQUIREMENTS:
                     - Identify the most likely root cause
                     - Explain WHY it happened based on log evidence
                     - Suggest concrete, actionable fixes
                     - Mention whether the issue is related to:
                       - configuration
                       - application code
                       - database
                       - infrastructure
                     - Use concise, technical language
                     - Confidence score range:
                       - 0.0 – 0.3 : speculative
                       - 0.4 – 0.6 : plausible
                       - 0.7 – 1.0 : high confidence
                       
                     - Do NOT mention:
                       - memory leak
                       - network issue
                       - CPU spike
                       - disk full
                     unless explicitly present in logs.
                     
                     ALLOWED ROOT CAUSE CATEGORIES:
                     - Configuration error
                     - Application code bug
                     - Database constraint or connectivity issue
                     - Dependency version mismatch
                     - Invalid input handling
                     - If no category fits exactly, select the closest applicable category and explain why in incident_summary
                     
                     CONTEXT:
                     - Tech stack: Spring Boot, PostgreSQL, React JS
                     - Environment: Development
                     
                     OUTPUT FORMAT (STRICT):
                     {
                       "summary": "string",
                       "root_cause_list": [
                         {
                           "root_cause": "string",
                           "affected_component": "string",
                           "fix_steps": [
                             "step 1",
                             "step 2"
                           ],
                           "incident_summary": "string",
                           "prevention_recommendation": "string",
                           "contributing_factors": "string",
                           "level": "string",
                           "confidence": 0.0
                         }
                       ]
                     }
                     
                     LOGS:
                """);

        int i = 1;
        for (Map<String, Object> log : logs) {
            promptContext.append("LOG #").append(i).append(":\n")
                    .append("stack_trace: ").append(log.get("stack_trace")).append("\n")
                    .append("logger_name: ").append(log.get("logger_name")).append("\n")
                    .append("message: ").append(log.get("message")).append("\n\n");
            i++;
        }
        promptContext.append("SYSTEM METRIC DETAIL DATA (GLOBAL CONTEXT, DO NOT USE TO MERGE LOGS):\n").append(metric);

        return promptContext.toString();
    }
}
