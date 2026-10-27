package com.dev.RCA.controller;

import com.dev.RCA.helper.PrometheusClient;
import com.dev.RCA.model.RootAnalyzeOutput;
import com.dev.RCA.model.SystemMetric;
import com.dev.RCA.repository.RootAnalyzeLastCheckpointRepository;
import com.dev.RCA.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

// ONLY for data testing purpose:
@RestController
@RequestMapping("/api/testing")
public class RootTestingController {

    @Autowired
    private LogQueryService logQueryService;

    @Autowired
    private RootAnalyzeService rootAnalyzeService;

    @Autowired
    private RootAnalyzeLastCheckpointRepository rootAnalyzeLastCheckpointRepository;

    @Autowired
    private DummyLogService dummyLogService;

    @Autowired
    private PrometheusClient prometheusClient;

    @Autowired
    private MetricService metricService;

    @Autowired
    private LogReaderService logReaderService;

    @GetMapping("/all-critical-log")
    public ResponseEntity<?> getCriticalLogs() throws IOException {
        return ResponseEntity.ok(logQueryService.getAllCriticalLogs(1));
    }

    @GetMapping("/get-root-analyze-output")
    public ResponseEntity<?> getRootAnalyzeOutputs() throws IOException {
        return ResponseEntity.ok(logQueryService.getAllRCAResult());
    }

    @GetMapping("/generate-root-analyze-output")
    public ResponseEntity<?> generateRootAnalyze() throws Exception {
        SystemMetric systemMetrics = new SystemMetric();
        systemMetrics.setMemoryAvailableInGB(metricService.getMemoryAvailableMetrics());
        systemMetrics.setCpuTotalUsageInPercent(metricService.getCpuUsageMetrics());
        systemMetrics.setNetworkTransmitterInKBps(metricService.getNetworkTransmitterMetrics());
        systemMetrics.setDiskReadLatencyInMs(metricService.getDiskReadLatencyMetrics());
        systemMetrics.setDiskWriteLatencyInMs(metricService.getDiskWriteLatencyMetrics());

        List<Map<String, Object>> selectedLogs = logQueryService.getAllCriticalLogs(2);
        RootAnalyzeOutput rootAnalyzeOutput = rootAnalyzeService.generateRootAnalyze(selectedLogs, systemMetrics);

        return ResponseEntity.ok(rootAnalyzeService.generateRootAnalyzeWithTimestamp(rootAnalyzeOutput));
    }

    @GetMapping("/generate-root-analyze-with-info")
    public ResponseEntity<?> generateRootAnalyzeWithInfo() throws Exception {
        SystemMetric systemMetrics = new SystemMetric();
        systemMetrics.setMemoryAvailableInGB(metricService.getMemoryAvailableMetrics());
        systemMetrics.setCpuTotalUsageInPercent(metricService.getCpuUsageMetrics());
        systemMetrics.setNetworkTransmitterInKBps(metricService.getNetworkTransmitterMetrics());
        systemMetrics.setDiskReadLatencyInMs(metricService.getDiskReadLatencyMetrics());
        systemMetrics.setDiskWriteLatencyInMs(metricService.getDiskWriteLatencyMetrics());

        List<Map<String, Object>> selectedLogs = logQueryService.getAllCriticalLogs(1);
        RootAnalyzeOutput rootAnalyzeOutput = rootAnalyzeService.generateRootAnalyze(selectedLogs, systemMetrics);

        return ResponseEntity.ok(rootAnalyzeService.generateRootAnalyzeWithTimestamp(rootAnalyzeOutput));
    }

    @GetMapping("/generate-root-analyze-with-max-metrics")
    public ResponseEntity<?> generateRootAnalyzeWithMaxMetrics() throws Exception {
        SystemMetric systemMetrics = new SystemMetric();
        systemMetrics.setMemoryAvailableInGB(0.2);
        systemMetrics.setCpuTotalUsageInPercent(99.0);
        systemMetrics.setNetworkTransmitterInKBps(10.0);
        systemMetrics.setDiskReadLatencyInMs(Map.of("E:", 100.0, "D:", 100.0, "C:", 100.0));
        systemMetrics.setDiskWriteLatencyInMs(Map.of("E:", 100.0, "D:", 100.0, "C:", 100.0));

        List<Map<String, Object>> selectedLogs = logQueryService.getAllCriticalLogs(2);
        RootAnalyzeOutput rootAnalyzeOutput = rootAnalyzeService.generateRootAnalyze(selectedLogs, systemMetrics);

        return ResponseEntity.ok(rootAnalyzeService.generateRootAnalyzeWithTimestamp(rootAnalyzeOutput));
    }

    @GetMapping("/generate-h2")
    public ResponseEntity<?> generateH2() {
        return ResponseEntity.ok(rootAnalyzeLastCheckpointRepository.findAll());
    }

    @GetMapping("/count-critical-log")
    public ResponseEntity<?> getCountLogs(@RequestParam String lastTriggered, @RequestParam int minScore) throws IOException {
        Instant lastRcaTriggeredAt = OffsetDateTime.parse(lastTriggered.replace(" ", "+")).toInstant();
        return ResponseEntity.ok(logQueryService.countNewCriticalLogs(lastRcaTriggeredAt, minScore));
    }

    @GetMapping("/get-new-log")
    public ResponseEntity<?> getNewLogs(@RequestParam String lastTriggered, @RequestParam int minScore) throws IOException {
        Instant lastRcaTriggeredAt = OffsetDateTime.parse(lastTriggered.replace(" ", "+")).toInstant();
        return ResponseEntity.ok(logQueryService.getNewCriticalLogs(lastRcaTriggeredAt, minScore));
    }

    @GetMapping("/generate-dummy-log")
    public ResponseEntity<?> generateDummyLog(@RequestParam String level, @RequestParam String message) {
        dummyLogService.generateDummyLog(level, message);
        return ResponseEntity.ok("Log generated with level: " + level);
    }

    @GetMapping("/instant-metrics")
    public ResponseEntity<?> getInstantMetricsUsage(@RequestParam String promQl) {
        return ResponseEntity.ok(prometheusClient.instantQuery(promQl));
    }

    @GetMapping("/get-metrics")
    public ResponseEntity<?> getMetrics() {
        SystemMetric systemMetrics = new SystemMetric();
        systemMetrics.setMemoryAvailableInGB(metricService.getMemoryAvailableMetrics());
        systemMetrics.setCpuTotalUsageInPercent(metricService.getCpuUsageMetrics());
        systemMetrics.setNetworkTransmitterInKBps(metricService.getNetworkTransmitterMetrics());
        systemMetrics.setDiskReadLatencyInMs(metricService.getDiskReadLatencyMetrics());
        systemMetrics.setDiskWriteLatencyInMs(metricService.getDiskWriteLatencyMetrics());

        return ResponseEntity.ok(systemMetrics);
    }

    @GetMapping("/read-local-logs")
    public ResponseEntity<?> getReadLogsFile(@RequestParam(required = false, defaultValue = "6 hours") String durationOption) throws IOException {
        return ResponseEntity.ok(logReaderService.readLocalLogs(durationOption));
    }

    @GetMapping("/generate-rca-with-local-logs")
    public ResponseEntity<?> generateRcaWithLocalLogs(@RequestParam(required = false, defaultValue = "6 hours") String durationOption) throws Exception {
        SystemMetric systemMetrics = new SystemMetric();
        systemMetrics.setMemoryAvailableInGB(metricService.getMemoryAvailableMetrics());
        systemMetrics.setCpuTotalUsageInPercent(metricService.getCpuUsageMetrics());
        systemMetrics.setNetworkTransmitterInKBps(metricService.getNetworkTransmitterMetrics());
        systemMetrics.setDiskReadLatencyInMs(metricService.getDiskReadLatencyMetrics());
        systemMetrics.setDiskWriteLatencyInMs(metricService.getDiskWriteLatencyMetrics());

        List<Map<String, Object>> result = logReaderService.readLocalLogs(durationOption);
        RootAnalyzeOutput rootAnalyzeOutput = rootAnalyzeService.generateRootAnalyze(result, systemMetrics);

        return ResponseEntity.ok(rootAnalyzeService.generateRootAnalyzeWithTimestamp(rootAnalyzeOutput));
    }
}
