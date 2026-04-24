package com.healthanalyzer.controller;

import com.healthanalyzer.dto.*;
import com.healthanalyzer.service.AnalysisService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AnalysisController {
    private final AnalysisService analysisService;

    @PostMapping("/analyze")
    public ResponseEntity<AnalysisResponse> analyze(@RequestBody LabInputRequest request, Authentication auth) {
        return ResponseEntity.ok(analysisService.analyze(request, auth.getName()));
    }

    @GetMapping("/history")
    public ResponseEntity<List<AnalysisResponse>> history(Authentication auth) {
        return ResponseEntity.ok(analysisService.getHistory(auth.getName()));
    }
}
