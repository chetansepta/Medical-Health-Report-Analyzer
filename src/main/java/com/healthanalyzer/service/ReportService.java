package com.healthanalyzer.service;

import com.healthanalyzer.dto.ReportExtractionResponse;
import com.healthanalyzer.util.HealthParameterParser;
import net.sourceforge.tess4j.TesseractException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@Service
public class ReportService {

    @Value("${file.upload-dir}")
    private String uploadDir;

    private final OcrService ocrService;

    public ReportService(OcrService ocrService) {
        this.ocrService = ocrService;
    }

    public ReportExtractionResponse uploadAndExtract(MultipartFile multipartFile) {
        if (multipartFile == null || multipartFile.isEmpty()) {
            return new ReportExtractionResponse(null, null, null, false, "File is empty");
        }

        try {
            File dir = new File(uploadDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            String originalName = multipartFile.getOriginalFilename();
            String savedName = UUID.randomUUID() + "_" + originalName;
            File savedFile = new File(dir, savedName);
            savedFile = savedFile.getAbsoluteFile();

            multipartFile.transferTo(savedFile);

            String extractedText = ocrService.extractTextFromFile(savedFile);
            Map<String, Double> extractedValues = HealthParameterParser.extractHealthParameters(extractedText);

            return new ReportExtractionResponse(
                    originalName,
                    extractedText,
                    extractedValues,
                    true,
                    "Report uploaded and processed successfully"
            );

        } catch (IOException | TesseractException | IllegalArgumentException e) {
            return new ReportExtractionResponse(null, null, null, false, e.getMessage());
        }
    }
}