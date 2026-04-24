package com.healthanalyzer.service;

import com.healthanalyzer.dto.*;
import com.healthanalyzer.dto.AnalysisResponse.ParameterResult;
import com.healthanalyzer.model.*;
import com.healthanalyzer.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AnalysisService {
    private final AnalysisRecordRepository analysisRepo;
    private final UserRepository userRepo;
    private final ObjectMapper objectMapper;

    private record Range(double min, double max, String unit) {}

    private static final Map<String, Range> NORMAL_RANGES = Map.ofEntries(
        Map.entry("Hemoglobin", new Range(12.0, 17.5, "g/dL")),
        Map.entry("RBC", new Range(4.5, 5.5, "million/µL")),
        Map.entry("WBC", new Range(4000, 11000, "cells/µL")),
        Map.entry("Platelets", new Range(150000, 400000, "cells/µL")),
        Map.entry("Blood Sugar", new Range(70, 100, "mg/dL")),
        Map.entry("Vitamin D", new Range(30, 100, "ng/mL")),
        Map.entry("Vitamin B12", new Range(200, 900, "pg/mL")),
        Map.entry("Iron", new Range(60, 170, "µg/dL")),
        Map.entry("Calcium", new Range(8.5, 10.5, "mg/dL")),
        Map.entry("Cholesterol", new Range(0, 200, "mg/dL")),
        Map.entry("TSH", new Range(0.4, 4.0, "mIU/L"))
    );

    public AnalysisResponse analyze(LabInputRequest req, String email) {
        User user = userRepo.findByEmail(email).orElseThrow();
        List<ParameterResult> params = new ArrayList<>();

        addParam(params, "Hemoglobin", req.getHemoglobin());
        addParam(params, "RBC", req.getRbc());
        addParam(params, "WBC", req.getWbc());
        addParam(params, "Platelets", req.getPlatelets());
        addParam(params, "Blood Sugar", req.getBloodSugar());
        addParam(params, "Vitamin D", req.getVitaminD());
        addParam(params, "Vitamin B12", req.getVitaminB12());
        addParam(params, "Iron", req.getIron());
        addParam(params, "Calcium", req.getCalcium());
        addParam(params, "Cholesterol", req.getCholesterol());
        addParam(params, "TSH", req.getTsh());

        double bmi = 0;
        String bmiCat = "N/A";
        if (req.getHeight() != null && req.getWeight() != null && req.getHeight() > 0) {
            double hm = req.getHeight() / 100.0;
            bmi = Math.round(req.getWeight() / (hm * hm) * 10.0) / 10.0;
            if (bmi < 18.5) bmiCat = "Underweight";
            else if (bmi < 25) bmiCat = "Normal";
            else if (bmi < 30) bmiCat = "Overweight";
            else bmiCat = "Obese";
        }

        int score = calculateHealthScore(params, bmi);
        String risk = score >= 80 ? "Low" : score >= 50 ? "Medium" : "High";

        AnalysisResponse response = AnalysisResponse.builder()
                .bmi(bmi).bmiCategory(bmiCat).healthScore(score).overallRisk(risk)
                .parameters(params)
                .dietRecommendations(getDietRecs(params))
                .exerciseRecommendations(getExerciseRecs(bmi, bmiCat))
                .lifestyleRecommendations(getLifestyleRecs(params))
                .build();

        try {
            AnalysisRecord record = AnalysisRecord.builder()
                    .user(user).hemoglobin(req.getHemoglobin()).rbc(req.getRbc())
                    .wbc(req.getWbc()).platelets(req.getPlatelets()).bloodSugar(req.getBloodSugar())
                    .vitaminD(req.getVitaminD()).vitaminB12(req.getVitaminB12()).iron(req.getIron())
                    .calcium(req.getCalcium()).cholesterol(req.getCholesterol()).tsh(req.getTsh())
                    .height(req.getHeight()).weight(req.getWeight()).bmi(bmi).healthScore(score)
                    .analysisJson(objectMapper.writeValueAsString(response))
                    .build();
            record = analysisRepo.save(record);
            response.setId(record.getId());
            response.setAnalyzedAt(record.getAnalyzedAt());
        } catch (Exception e) { throw new RuntimeException("Failed to save analysis", e); }

        return response;
    }

    public List<AnalysisResponse> getHistory(String email) {
        User user = userRepo.findByEmail(email).orElseThrow();
        List<AnalysisRecord> records = analysisRepo.findByUserIdOrderByAnalyzedAtDesc(user.getId());
        List<AnalysisResponse> result = new ArrayList<>();
        for (AnalysisRecord r : records) {
            try {
                AnalysisResponse resp = objectMapper.readValue(r.getAnalysisJson(), AnalysisResponse.class);
                resp.setId(r.getId());
                resp.setAnalyzedAt(r.getAnalyzedAt());
                result.add(resp);
            } catch (Exception ignored) {}
        }
        return result;
    }

    private void addParam(List<ParameterResult> list, String name, Double value) {
        if (value == null) return;
        Range range = NORMAL_RANGES.get(name);
        String status; String riskLevel; double pctDiff;
        double mid = (range.min + range.max) / 2.0;
        if (value < range.min) {
            status = "Low"; pctDiff = Math.round((range.min - value) / range.min * 1000.0) / 10.0;
            riskLevel = pctDiff > 30 ? "High" : pctDiff > 15 ? "Medium" : "Low";
        } else if (value > range.max) {
            status = "High"; pctDiff = Math.round((value - range.max) / range.max * 1000.0) / 10.0;
            riskLevel = pctDiff > 30 ? "High" : pctDiff > 15 ? "Medium" : "Low";
        } else {
            status = "Normal"; pctDiff = 0; riskLevel = "Low";
        }
        list.add(ParameterResult.builder().name(name).value(value).unit(range.unit)
                .status(status).riskLevel(riskLevel).percentDifference(pctDiff)
                .normalMin(range.min).normalMax(range.max).build());
    }

    private int calculateHealthScore(List<ParameterResult> params, double bmi) {
        if (params.isEmpty()) return 50;
        int score = 100;
        for (ParameterResult p : params) {
            if ("High".equals(p.getRiskLevel())) score -= 12;
            else if ("Medium".equals(p.getRiskLevel())) score -= 6;
            else if (!"Normal".equals(p.getStatus())) score -= 2;
        }
        if (bmi > 0 && (bmi < 18.5 || bmi >= 30)) score -= 10;
        else if (bmi >= 25) score -= 5;
        return Math.max(0, Math.min(100, score));
    }

    private List<String> getDietRecs(List<ParameterResult> params) {
        List<String> recs = new ArrayList<>();
        for (ParameterResult p : params) {
            switch (p.getName()) {
                case "Hemoglobin" -> { if ("Low".equals(p.getStatus())) recs.add("Include iron-rich foods: spinach (palak), beetroot, pomegranate, jaggery (gur), and dates (khajoor)."); }
                case "Vitamin D" -> { if ("Low".equals(p.getStatus())) recs.add("Get 15-20 min morning sunlight. Add fortified milk, eggs, and mushrooms to your diet."); }
                case "Vitamin B12" -> { if ("Low".equals(p.getStatus())) recs.add("Include curd (dahi), paneer, eggs, and fortified cereals. Consider B12 supplements."); }
                case "Calcium" -> { if ("Low".equals(p.getStatus())) recs.add("Consume ragi (finger millet), sesame seeds (til), milk, curd, and green leafy vegetables."); }
                case "Cholesterol" -> { if ("High".equals(p.getStatus())) recs.add("Reduce fried foods, ghee, and red meat. Increase oats, walnuts, flaxseeds, and garlic."); }
                case "Blood Sugar" -> { if ("High".equals(p.getStatus())) recs.add("Reduce refined carbs and sugar. Prefer whole grains, bitter gourd (karela), fenugreek (methi), and cinnamon (dalchini)."); }
                case "Iron" -> { if ("Low".equals(p.getStatus())) recs.add("Eat more green leafy veggies, lentils (dal), tofu, and amla for vitamin C to boost iron absorption."); }
                case "TSH" -> { if ("High".equals(p.getStatus())) recs.add("Avoid goitrogenic foods like raw cabbage and soy. Include selenium-rich foods: Brazil nuts, sunflower seeds."); }
            }
        }
        if (recs.isEmpty()) recs.add("Your diet seems balanced! Continue eating a variety of fruits, vegetables, whole grains, and proteins.");
        return recs;
    }

    private List<String> getExerciseRecs(double bmi, String bmiCat) {
        List<String> recs = new ArrayList<>();
        recs.add("Aim for at least 30 minutes of moderate exercise 5 days a week.");
        switch (bmiCat) {
            case "Underweight" -> recs.add("Focus on strength training and muscle-building exercises. Include yoga asanas like Surya Namaskar.");
            case "Overweight", "Obese" -> { recs.add("Start with brisk walking, cycling, or swimming. Gradually increase intensity."); recs.add("Practice Pranayama and Kapalbhati for metabolic health."); }
            default -> recs.add("Maintain current activity. Mix cardio, strength training, and flexibility exercises.");
        }
        return recs;
    }

    private List<String> getLifestyleRecs(List<ParameterResult> params) {
        List<String> recs = new ArrayList<>(List.of(
            "Sleep 7-8 hours daily. Maintain a consistent sleep schedule.",
            "Stay hydrated — drink at least 8-10 glasses of water daily.",
            "Manage stress through meditation, deep breathing, or hobbies.",
            "Get regular health check-ups every 6 months."
        ));
        boolean hasHighRisk = params.stream().anyMatch(p -> "High".equals(p.getRiskLevel()));
        if (hasHighRisk) recs.add("⚠️ Some parameters are significantly abnormal. Please consult a healthcare professional promptly.");
        return recs;
    }
}
