package com.dev.RCA.configuration;

import com.dev.RCA.model.RootAnalyzeLastCheckpoint;
import com.dev.RCA.repository.RootAnalyzeLastCheckpointRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@RequiredArgsConstructor
public class RootAnalyzeLastCheckpointInitializer implements ApplicationRunner {

    @Autowired
    private RootAnalyzeLastCheckpointRepository rootAnalyzeLastCheckpointRepository;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        rootAnalyzeLastCheckpointRepository.findById("elasticsearch")
                .orElseGet(() -> rootAnalyzeLastCheckpointRepository.save(new RootAnalyzeLastCheckpoint("elasticsearch", Instant.EPOCH, Instant.EPOCH)));
    }
}
