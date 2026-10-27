package com.dev.RCA.controller;

import com.dev.RCA.model.RootAnalyzeOutput;
import com.dev.RCA.model.SystemMetric;
import com.dev.RCA.service.LogQueryService;
import com.dev.RCA.service.MetricService;
import com.dev.RCA.service.RootAnalyzeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/root-analyze")
public class RootAnalyzeController {

    @Autowired
    private LogQueryService logQueryService;

    @Autowired
    private RootAnalyzeService rootAnalyzeService;

    @Autowired
    private MetricService metricService;

    @PostMapping("/trigger-time-frame")
    public ResponseEntity<?> triggerRootAnalyzeTimeFrame(@RequestParam long from, @RequestParam long to) throws Exception {
        Instant fromTimestamp = Instant.ofEpochMilli(from);
        Instant toTimeStamp = Instant.ofEpochMilli(to);

        SystemMetric systemMetrics = new SystemMetric();
        systemMetrics.setMemoryAvailableInGB(metricService.getMemoryAvailableMetrics());
        systemMetrics.setCpuTotalUsageInPercent(metricService.getCpuUsageMetrics());
        systemMetrics.setNetworkTransmitterInKBps(metricService.getNetworkTransmitterMetrics());
        systemMetrics.setDiskReadLatencyInMs(metricService.getDiskReadLatencyMetrics());
        systemMetrics.setDiskWriteLatencyInMs(metricService.getDiskWriteLatencyMetrics());

        // Get Critical log in given timeframe
        List<Map<String, Object>> selectedLogs = logQueryService.getCriticalLogsWithTimeFrame(fromTimestamp, toTimeStamp, 2);

        System.out.println(fromTimestamp + " and " + toTimeStamp);

        // Generate RootAnalyze Output
        RootAnalyzeOutput rootAnalyzeOutputWithTimeFrame = rootAnalyzeService.generateRootAnalyze(selectedLogs, systemMetrics);

        // Add it to index "traceroot-log-rca-time-frame-*"
        rootAnalyzeService.addIndexRootAnalyzeOutputWithTimeFrame(rootAnalyzeOutputWithTimeFrame);

        System.out.println("GENERATE ROOT ANALYZE SUMMARY BASED ON TIMEFRAME EXECUTE SUCCESSFULLY + ADDED ELK TOO!");
        return ResponseEntity.ok("Triggered Root Analyze and add new index with time-frame!");
    }
}
