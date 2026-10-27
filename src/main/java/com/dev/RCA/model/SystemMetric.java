package com.dev.RCA.model;

import lombok.Data;

import java.util.Map;

@Data
public class SystemMetric {

    private double memoryAvailableInGB;

    private double cpuTotalUsageInPercent;

    private double networkTransmitterInKBps;

    private Map<String, Double> diskReadLatencyInMs;

    private Map<String, Double> diskWriteLatencyInMs;
}
