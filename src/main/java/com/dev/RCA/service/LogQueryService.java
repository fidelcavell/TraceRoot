package com.dev.RCA.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch.core.CountResponse;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.json.JsonData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class LogQueryService {

    @Autowired
    private ElasticsearchClient elasticsearchClient;

    public List getAllRCAResult() throws IOException {
        SearchResponse<Map> response =
                elasticsearchClient.search(search -> search
                                .index("traceroot-log-rca-result-*")
                                .sort(sort -> sort
                                        .field(f -> f
                                                .field("@timestamp")
                                                .order(SortOrder.Asc)
                                        )
                                )
                                .size(500),
                        Map.class
                );

        return response.hits().hits().stream()
                .map(Hit::source)
                .collect(Collectors.toList());
    }

    public List getCriticalLogsWithTimeFrame(Instant fromTimestamp, Instant toTimestamp, int minSeverityScore) throws IOException {
        SearchResponse<Map> response = elasticsearchClient.search(search -> search
                        .index("traceroot-log-rca-assistant-*")
                        .query(q -> q
                                .bool(b -> b
                                        .filter(m -> m.range(r -> r
                                                .field("@timestamp")
                                                .gte(JsonData.of(fromTimestamp.toString()))
                                                .lte(JsonData.of(toTimestamp.toString()))
                                        ))
                                        .filter(m -> m.range(r -> r
                                                .field("severity_score")
                                                .gte(JsonData.of(minSeverityScore))
                                        ))
                                )
                        )
                        .sort(sort -> sort
                                .field(f -> f
                                        .field("@timestamp")
                                        .order(SortOrder.Asc)
                                )
                        )
                        .size(250),
                Map.class
        );

        return response.hits().hits().stream()
                .map(Hit::source)
                .collect(Collectors.toList());
    }

    public List getAllCriticalLogs(int minSeverityScore) throws IOException {
        SearchResponse<Map> response =
                elasticsearchClient.search(search -> search
                                .index("traceroot-log-rca-assistant-*")
                                .query(q -> q
                                        .range(r -> r
                                                .field("severity_score")
                                                .gte(JsonData.of(minSeverityScore))
                                        )
                                )
                                .sort(sort -> sort
                                        .field(f -> f
                                                .field("@timestamp")
                                                .order(SortOrder.Asc)
                                        )
                                )
                                .size(150),
                        Map.class
                );

        return response.hits().hits().stream()
                .map(Hit::source)
                .collect(Collectors.toList());
    }

    public long countNewCriticalLogs(Instant lastRcaTriggeredAt, int minSeverityScore) throws IOException {
        CountResponse response = elasticsearchClient.count(count -> count
                .index("traceroot-log-rca-assistant-*")
                .query(q -> q
                        .bool(b -> b
                                .filter(m -> m.range(r -> r
                                        .field("@timestamp")
                                        .gte(JsonData.of(lastRcaTriggeredAt.toString()))
                                ))
                                .filter(m -> m.range(r -> r
                                        .field("severity_score")
                                        .gte(JsonData.of(minSeverityScore))
                                ))
                        )
                )
        );

        return response.count();
    }

    public List getNewCriticalLogs(Instant lastRcaTriggeredAt, int minSeverityScore) throws IOException {
        SearchResponse<Map> response =
                elasticsearchClient.search(search -> search
                                .index("traceroot-log-rca-assistant-*")
                                .query(q -> q
                                        .bool(b -> b
                                                .filter(m -> m.range(r -> r
                                                        .field("@timestamp")
                                                        .gte(JsonData.of(lastRcaTriggeredAt.toString()))
                                                ))
                                                .filter(m -> m.range(r -> r
                                                        .field("severity_score")
                                                        .gte(JsonData.of(minSeverityScore))
                                                ))
                                        )
                                )
                                .sort(sort -> sort
                                        .field(f -> f
                                                .field("@timestamp")
                                                .order(SortOrder.Asc)
                                        )
                                )
                                .size(500),
                        Map.class
                );

        return response.hits().hits()
                .stream()
                .map(Hit::source)
                .toList();
    }
}
