package com.dev.RCA.repository;

import com.dev.RCA.model.RootAnalyzeLastCheckpoint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RootAnalyzeLastCheckpointRepository extends JpaRepository<RootAnalyzeLastCheckpoint, String> {
}
