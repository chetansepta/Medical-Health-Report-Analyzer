package com.healthanalyzer.controller;

import com.healthanalyzer.dto.ReportExtractionResponse;
import com.healthanalyzer.service.ReportService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/report")
@CrossOrigin(origins = "*")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ReportExtractionResponse> uploadReport(@RequestParam("file") MultipartFile file) {
        ReportExtractionResponse response = reportService.uploadAndExtract(file);
        return ResponseEntity.ok(response);
    }
}