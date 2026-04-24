package com.healthanalyzer.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "analysis_records")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class AnalysisRecord {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Lab values
    private Double hemoglobin;
    private Double rbc;
    private Double wbc;
    private Double platelets;
    private Double bloodSugar;
    private Double vitaminD;
    private Double vitaminB12;
    private Double iron;
    private Double calcium;
    private Double cholesterol;
    private Double tsh;
    private Double height; // cm
    private Double weight; // kg

    // Results
    private Double bmi;
    private Integer healthScore;

    @Column(columnDefinition = "TEXT")
    private String analysisJson; // full analysis result stored as JSON

    private LocalDateTime analyzedAt;

    @PrePersist
    protected void onCreate() { this.analyzedAt = LocalDateTime.now(); }
}
