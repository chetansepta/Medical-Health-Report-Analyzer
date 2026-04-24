package com.healthanalyzer.dto;

import lombok.Data;

@Data
public class LabInputRequest {
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
    private Double height;
    private Double weight;
}
