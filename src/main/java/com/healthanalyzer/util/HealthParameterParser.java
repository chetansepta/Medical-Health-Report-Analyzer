package com.healthanalyzer.util;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HealthParameterParser {

    public static Map<String, Double> extractHealthParameters(String text) {
        Map<String, Double> values = new HashMap<>();

        extractValue(text, values, "hemoglobin", "(?i)hemoglobin\\s*[:\\-]?\\s*(\\d+(\\.\\d+)?)");
        extractValue(text, values, "rbc", "(?i)rbc\\s*[:\\-]?\\s*(\\d+(\\.\\d+)?)");
        extractValue(text, values, "wbc", "(?i)wbc\\s*[:\\-]?\\s*(\\d+(\\.\\d+)?)");
        extractValue(text, values, "platelets", "(?i)platelets\\s*[:\\-]?\\s*(\\d+(\\.\\d+)?)");
        extractValue(text, values, "bloodSugar", "(?i)(blood\\s*sugar|glucose)\\s*[:\\-]?\\s*(\\d+(\\.\\d+)?)", 2);
        extractValue(text, values, "vitaminD", "(?i)vitamin\\s*d\\s*[:\\-]?\\s*(\\d+(\\.\\d+)?)");
        extractValue(text, values, "vitaminB12", "(?i)vitamin\\s*b12\\s*[:\\-]?\\s*(\\d+(\\.\\d+)?)");
        extractValue(text, values, "iron", "(?i)iron\\s*[:\\-]?\\s*(\\d+(\\.\\d+)?)");
        extractValue(text, values, "calcium", "(?i)calcium\\s*[:\\-]?\\s*(\\d+(\\.\\d+)?)");
        extractValue(text, values, "cholesterol", "(?i)cholesterol\\s*[:\\-]?\\s*(\\d+(\\.\\d+)?)");
        extractValue(text, values, "tsh", "(?i)tsh\\s*[:\\-]?\\s*(\\d+(\\.\\d+)?)");

        return values;
    }

    private static void extractValue(String text, Map<String, Double> values, String key, String regex) {
        extractValue(text, values, key, regex, 1);
    }

    private static void extractValue(String text, Map<String, Double> values, String key, String regex, int groupIndex) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            try {
                values.put(key, Double.parseDouble(matcher.group(groupIndex)));
            } catch (Exception ignored) {
            }
        }
    }
}