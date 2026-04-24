package com.healthanalyzer.dto;

import lombok.*;
import java.time.LocalDateTime;
import java.util.*;

@Data @Builder @AllArgsConstructor @NoArgsConstructor
public class AnalysisResponse {
    private Long id;
    private Double bmi;
    private String bmiCategory;
    private Integer healthScore;
    private String overallRisk;
    private List<ParameterResult> parameters;
    private List<String> dietRecommendations;
    private List<String> exerciseRecommendations;
    private List<String> lifestyleRecommendations;
    private LocalDateTime analyzedAt;

    @Data @Builder @AllArgsConstructor @NoArgsConstructor
    public static class ParameterResult {
        private String name;
        private Double value;
        private String unit;
        private String status; // Low, Normal, High
        private String riskLevel; // Low, Medium, High
        private Double percentDifference;
        private Double normalMin;
        private Double normalMax;
    }
}
