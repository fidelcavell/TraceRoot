package com.dev.RCA.service;

import com.dev.RCA.helper.PrometheusClient;
import com.dev.RCA.helper.PrometheusMetricParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@Service
public class MetricService {

    @Autowired
    private PrometheusClient prometheusClient;

    @Autowired
    private PrometheusMetricParser metricParser;

    public double getCpuUsageMetrics() {
        String json = prometheusClient
                .instantQuery("100 - avg(rate(windows_cpu_time_total[1m])) * 100")
                .block();

        return metricParser
                .parseSingleValue(json)
                .orElse(0.0);
    }

    public double getNetworkTransmitterMetrics() {
        String json = prometheusClient
                .instantQuery("rate(process_network_transmit_bytes_total[1m]) / 1024")
                .block();

        return metricParser
                .parseSingleValue(json)
                .orElse(0.0);
    }

    public double getMemoryAvailableMetrics() {
        String json = prometheusClient
                .instantQuery("windows_memory_available_bytes / 1024 / 1024 / 1024")
                .block();

        return metricParser
                .parseSingleValue(json)
                .orElse(0.0);
    }

    public Map<String, Double> getDiskReadLatencyMetrics() {
        String json = prometheusClient
                .instantQuery("rate(windows_logical_disk_read_latency_seconds_total[1m]) " +
                        "/ rate(windows_logical_disk_reads_total[1m])")
                .block();

        return metricParser.parseLabeledValues(json, "volume");
    }

    public Map<String, Double> getDiskWriteLatencyMetrics() {
        String json = prometheusClient
                .instantQuery("rate(windows_logical_disk_write_latency_seconds_total[1m]) " +
                        "/ rate(windows_logical_disk_writes_total[1m])")
                .block();

        return metricParser.parseLabeledValues(json, "volume");
    }
}
