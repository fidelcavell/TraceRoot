package com.dev.RCA.service;

import com.dev.RCA.model.RootAnalyzeLastCheckpoint;
import com.dev.RCA.model.RootAnalyzeOutput;
import com.dev.RCA.model.SystemMetric;
import com.dev.RCA.repository.RootAnalyzeLastCheckpointRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.List;
import java.util.Map;

@Service
public class LogMonitorService {

    @Autowired
    private LogQueryService logQueryService;

    @Autowired
    private RootAnalyzeService rootAnalyzeService;

    @Autowired
    private RootAnalyzeLastCheckpointRepository rootAnalyzeLastCheckpointRepository;

    @Autowired
    private MetricService metricService;

    @Scheduled(fixedDelay = 60000) // Execute in every 1 minute
    public void logMonitor() throws Exception {
        OffsetDateTime currentTime = OffsetDateTime.now(ZoneId.of("Asia/Jakarta"));

        RootAnalyzeLastCheckpoint storedRootAnalyzeLastCheckpoint = rootAnalyzeLastCheckpointRepository.findById("elasticsearch").orElseGet(() -> {
            RootAnalyzeLastCheckpoint selectedRootAnalyzeLastCheckpoint = new RootAnalyzeLastCheckpoint();
            selectedRootAnalyzeLastCheckpoint.setName("elasticsearch");
            selectedRootAnalyzeLastCheckpoint.setLastCheckedAt(currentTime.toInstant().minus(Duration.ofMinutes(60)));
            selectedRootAnalyzeLastCheckpoint.setLastRcaTriggeredAt(currentTime.toInstant().minus(Duration.ofMinutes(60)));
            return selectedRootAnalyzeLastCheckpoint;
        });

        if (storedRootAnalyzeLastCheckpoint.getLastRcaTriggeredAt() == null) {
            storedRootAnalyzeLastCheckpoint.setLastRcaTriggeredAt(Instant.EPOCH);
        }

        Instant storedLastRcaTriggered = storedRootAnalyzeLastCheckpoint.getLastRcaTriggeredAt();
        long count = logQueryService.countNewCriticalLogs(storedLastRcaTriggered, 1);
        storedRootAnalyzeLastCheckpoint.setLastCheckedAt(currentTime.toInstant());

        System.out.println("SCHEDULED PART HERE!");
        System.out.println("LastRcaTriggeredAt: " + storedLastRcaTriggered.atOffset(ZoneOffset.ofHours(7)));
        System.out.println("NewCriticalLogs count: " +  count);

        // as Threshold, if log with ERROR or WARN are more than equal 5, do generate Root Analyze based on those new log.
        if (count >= 5) {
            SystemMetric systemMetrics = new SystemMetric();
            systemMetrics.setMemoryAvailableInGB(metricService.getMemoryAvailableMetrics());
            systemMetrics.setCpuTotalUsageInPercent(metricService.getCpuUsageMetrics());
            systemMetrics.setNetworkTransmitterInKBps(metricService.getNetworkTransmitterMetrics());
            systemMetrics.setDiskReadLatencyInMs(metricService.getDiskReadLatencyMetrics());
            systemMetrics.setDiskWriteLatencyInMs(metricService.getDiskWriteLatencyMetrics());

            List<Map<String, Object>> selectedLogs = logQueryService.getNewCriticalLogs(storedLastRcaTriggered, 1);

            // Generate List of Root Analyze outputs.
            RootAnalyzeOutput rcaResults = rootAnalyzeService.generateRootAnalyze(selectedLogs, systemMetrics);

            // Add new indices for Root Analyze in Elasticsearch index.
            rootAnalyzeService.addIndexRootAnalyzeOutput(rcaResults);

            storedRootAnalyzeLastCheckpoint.setLastRcaTriggeredAt(currentTime.toInstant());
            System.out.println("GENERATE ROOT ANALYZE PART HERE!");
        }

        System.out.println("LastCheckAt: " + storedRootAnalyzeLastCheckpoint.getLastCheckedAt().atOffset(ZoneOffset.ofHours(7)));

        // Save new checkpoint data.
        rootAnalyzeLastCheckpointRepository.save(storedRootAnalyzeLastCheckpoint);
    }
}
