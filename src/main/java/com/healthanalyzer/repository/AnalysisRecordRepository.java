package com.healthanalyzer.repository;

import com.healthanalyzer.model.AnalysisRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AnalysisRecordRepository extends JpaRepository<AnalysisRecord, Long> {
    List<AnalysisRecord> findByUserIdOrderByAnalyzedAtDesc(Long userId);
}
