package com.example.pdfsummarizer.controller;

import com.example.pdfsummarizer.model.SummaryResponse;
import com.example.pdfsummarizer.service.PdfSummarizerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.io.File;

@RestController
@RequestMapping("/pdf-summarizer")
@CrossOrigin(origins = "*")
public class PdfSummarizerController {

    private static final Logger logger = LoggerFactory.getLogger(PdfSummarizerController.class);

    @Autowired
    private PdfSummarizerService pdfSummarizerService;

    @PostMapping("/summarize")
    public ResponseEntity<SummaryResponse> summarizePdf(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "summaryType", defaultValue = "basic") String summaryType,
            @RequestParam(value = "maxWords", defaultValue = "300") @Min(50) @Max(1000) int maxWords) {

        logger.info("Received PDF summarization request: filename={}, summaryType={}, maxWords={}", 
                   file.getOriginalFilename(), summaryType, maxWords);

        // Validate file
        if (file.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(new SummaryResponse(null, null, null, false, "No file provided"));
        }

        if (!isPdfFile(file)) {
            return ResponseEntity.badRequest()
                    .body(new SummaryResponse(file.getOriginalFilename(), null, null, false, 
                                            "Only PDF files are supported"));
        }

        SummaryResponse response = pdfSummarizerService.processPdfSummary(file, summaryType, maxWords);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/summarize-text-only")
    public ResponseEntity<SummaryResponse> summarizePdfTextOnly(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "maxWords", defaultValue = "300") @Min(50) @Max(1000) int maxWords) {

        logger.info("Received text-only PDF summarization request: filename={}, maxWords={}", 
                   file.getOriginalFilename(), maxWords);

        // Validate file
        if (file.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(new SummaryResponse(null, null, null, false, "No file provided"));
        }

        if (!isPdfFile(file)) {
            return ResponseEntity.badRequest()
                    .body(new SummaryResponse(file.getOriginalFilename(), null, null, false, 
                                            "Only PDF files are supported"));
        }

        SummaryResponse response = pdfSummarizerService.generateTextOnlySummary(file, maxWords);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/download/{filename}")
    public ResponseEntity<Resource> downloadSummaryPdf(@PathVariable String filename) {
        logger.info("Received download request for file: {}", filename);

        File file = pdfSummarizerService.getOutputFile(filename);
        
        if (file == null || !file.exists()) {
            return ResponseEntity.notFound().build();
        }

        Resource resource = new FileSystemResource(file);
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(resource);
    }

    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("PDF Summarizer Service is running");
    }

    @GetMapping("/info")
    public ResponseEntity<Object> getServiceInfo() {
        return ResponseEntity.ok(new Object() {
            public final String service = "PDF Summarizer";
            public final String version = "1.0.0";
            public final String description = "Service for extracting text from PDFs and generating summaries";
            public final String[] supportedFormats = {"PDF"};
            public final Object endpoints = new Object() {
                public final String summarize = "POST /api/pdf-summarizer/summarize - Generate PDF summary";
                public final String summarizeTextOnly = "POST /api/pdf-summarizer/summarize-text-only - Generate text-only summary";
                public final String download = "GET /api/pdf-summarizer/download/{filename} - Download summary PDF";
                public final String health = "GET /api/pdf-summarizer/health - Health check";
                public final String info = "GET /api/pdf-summarizer/info - Service information";
            };
        });
    }

    private boolean isPdfFile(MultipartFile file) {
        String contentType = file.getContentType();
        String filename = file.getOriginalFilename();
        
        return (contentType != null && contentType.equals("application/pdf")) ||
               (filename != null && filename.toLowerCase().endsWith(".pdf"));
    }
}