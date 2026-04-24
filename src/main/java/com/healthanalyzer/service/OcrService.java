package com.healthanalyzer.service;

import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

@Service
public class OcrService {

    @Value("${tesseract.data-path}")
    private String tesseractDataPath;

    public String extractTextFromFile(File file) throws IOException, TesseractException {
        String fileName = file.getName().toLowerCase();

        if (fileName.endsWith(".png") || fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) {
            return extractTextFromImage(file);
        } else if (fileName.endsWith(".pdf")) {
            return extractTextFromPdf(file);
        } else {
            throw new IllegalArgumentException("Unsupported file type");
        }
    }

    private String extractTextFromImage(File file) throws IOException, TesseractException {
        BufferedImage image = ImageIO.read(file);
        ITesseract tesseract = new Tesseract();
        tesseract.setDatapath(tesseractDataPath);
        tesseract.setLanguage("eng");
        return tesseract.doOCR(image);
    }

    private String extractTextFromPdf(File file) throws IOException, TesseractException {
        StringBuilder result = new StringBuilder();

        try (PDDocument document = PDDocument.load(file)) {
            PDFRenderer pdfRenderer = new PDFRenderer(document);
            ITesseract tesseract = new Tesseract();
            tesseract.setDatapath(tesseractDataPath);
            tesseract.setLanguage("eng");

            for (int i = 0; i < document.getNumberOfPages(); i++) {
                BufferedImage image = pdfRenderer.renderImageWithDPI(i, 300);
                result.append(tesseract.doOCR(image)).append("\n");
            }
        }

        return result.toString();
    }
}