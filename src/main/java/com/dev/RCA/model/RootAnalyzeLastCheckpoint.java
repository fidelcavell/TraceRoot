package com.dev.RCA.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class RootAnalyzeLastCheckpoint {

    @Id
    private String name;

    private Instant lastCheckedAt;

    private Instant lastRcaTriggeredAt;
}
