package com.dev.RCA.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@Service
public class LogReaderService {

    private final ObjectMapper mapper = new ObjectMapper();

    @Value("${LOG_FILE_PATH}")
    private String logFilePath;

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // Translate durationOption, filter log output and return a response contain list of filtered log
    public List<Map<String, Object>> readLocalLogs(String durationOption) throws IOException {
        List<JsonNode> result = new ArrayList<>();
        Instant to = Instant.now();
        Instant from = switch (durationOption) {
            case "5 minutes" -> to.minus(5, ChronoUnit.MINUTES);
            case "15 minutes" -> to.minus(15, ChronoUnit.MINUTES);
            case "30 minutes" -> to.minus(30, ChronoUnit.MINUTES);
            case "1 hour" -> to.minus(1, ChronoUnit.HOURS);
            case "3 hours" -> to.minus(3, ChronoUnit.HOURS);
            case "6 hours" -> to.minus(6, ChronoUnit.HOURS);
            case "12 hours" -> to.minus(12, ChronoUnit.HOURS);
            case "24 hours" -> to.minus(1, ChronoUnit.DAYS);
            case "2 days" -> to.minus(2, ChronoUnit.DAYS);
            case "7 days" -> to.minus(7, ChronoUnit.DAYS);
            default -> to.minus(30, ChronoUnit.DAYS);
        };

        LocalDate fromDate = LocalDate.ofInstant(from, ZoneId.systemDefault());
        LocalDate toDate = LocalDate.ofInstant(to, ZoneId.systemDefault());

        System.out.println("START DATE: " + fromDate);
        System.out.println("END DATE: " + toDate);

        Path logDir = Paths.get(logFilePath);
        try (Stream<Path> paths = Files.walk(logDir, 2)) {
            paths.filter(Files::isRegularFile)
                    .filter(path -> isLogFileInTimeRange(path.getFileName().toString(), fromDate, toDate))
                    .forEach(path -> result.addAll(readSingleLog(path)));
        }

        return result.stream()
                .filter(log -> isSingleLogWithinTimeRange(log, from, to))
                .map(node -> mapper.convertValue(
                        node,
                        new TypeReference<Map<String, Object>>() {
                        }
                ))
                .filter(log -> {
                    Object logLevel = log.get("level");
                    return "WARN".equals(logLevel) || "ERROR".equals(logLevel);
                })
                .limit(250)
                .toList();
    }

    // Check: is the log file is in a requested time range
    private boolean isLogFileInTimeRange(String fileName, LocalDate from, LocalDate to) {
        LocalDate today = LocalDate.now();

        if (fileName.equals("traceroot-logger.log")) {
            return !to.isBefore(today);
        }

        Pattern pattern = Pattern.compile("traceroot-logger\\.(\\d{4}-\\d{2}-\\d{2})(?:\\.\\d+)?\\.log");
        Matcher matcher = pattern.matcher(fileName);

        System.out.println("Detected PATTERN: " + pattern);
        System.out.println("Detected FileName: " + fileName);

        if (!matcher.matches()) return false;
        LocalDate fileDate = LocalDate.parse(matcher.group(1), DATE_FORMAT);

        System.out.println("Detected FileDate: " + fileDate);
        return !fileDate.isBefore(from) && !fileDate.isAfter(to);
    }

    // Read a single log file
    private List<JsonNode> readSingleLog(Path path) {
        System.out.println("FileName Read: " + path);
        try (Stream<String> lines = Files.lines(path)) {
            return lines
                    .map(line -> {
                        try {
                            return mapper.readTree(line);
                        } catch (Exception e) {
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .toList();
        } catch (Exception e) {
            return List.of();
        }
    }

    // Filter log by @timestamp
    private boolean isSingleLogWithinTimeRange(JsonNode node, Instant from, Instant to) {
        JsonNode fetchedNode = node.get("@timestamp");
        if (fetchedNode == null) return false;

        try {
            Instant fetchedLogTime = OffsetDateTime
                    .parse(fetchedNode.asText())
                    .toInstant();
            return !fetchedLogTime.isBefore(from) && !fetchedLogTime.isAfter(to);
        } catch (Exception e) {
            return false;
        }
    }
}
