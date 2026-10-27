package com.dev.RCA.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class DummyLogService {

    private static final Logger logger = LoggerFactory.getLogger(LogMonitorService.class);

    public void generateDummyLog(String level, String message) {
        switch (level.toUpperCase()) {
            case "ERROR" -> {
                Exception exception = new RuntimeException("Dummy exception");
                logger.error(message, exception);
            }
            case "WARN" -> logger.warn(message);
            default -> logger.info(message);
        }
    }
}
