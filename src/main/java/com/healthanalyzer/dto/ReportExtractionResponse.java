package com.healthanalyzer.dto;

import java.util.Map;

public class ReportExtractionResponse {
    private String fileName;
    private String extractedText;
    private Map<String, Double> extractedValues;
    private boolean success;
    private String message;

    public ReportExtractionResponse() {
    }

    public ReportExtractionResponse(String fileName, String extractedText, Map<String, Double> extractedValues, boolean success, String message) {
        this.fileName = fileName;
        this.extractedText = extractedText;
        this.extractedValues = extractedValues;
        this.success = success;
        this.message = message;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getExtractedText() {
        return extractedText;
    }

    public void setExtractedText(String extractedText) {
        this.extractedText = extractedText;
    }

    public Map<String, Double> getExtractedValues() {
        return extractedValues;
    }

    public void setExtractedValues(Map<String, Double> extractedValues) {
        this.extractedValues = extractedValues;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}