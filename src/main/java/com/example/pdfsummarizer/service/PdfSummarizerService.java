package com.example.pdfsummarizer.service;

import com.example.pdfsummarizer.model.SummaryResponse;
import com.itextpdf.text.DocumentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class PdfSummarizerService {

    private static final Logger logger = LoggerFactory.getLogger(PdfSummarizerService.class);

    @Autowired
    private PdfReaderService pdfReaderService;

    @Autowired
    private TextSummarizerService textSummarizerService;

    @Autowired
    private PdfGeneratorService pdfGeneratorService;

    @Value("${app.upload.dir:./uploads}")
    private String uploadDir;

    @Value("${app.output.dir:./outputs}")
    private String outputDir;

    public SummaryResponse processPdfSummary(MultipartFile file, String summaryType, int maxWords) {
        logger.info("Processing PDF summary for file: {}, type: {}, maxWords: {}", 
                   file.getOriginalFilename(), summaryType, maxWords);

        try {
            // Save uploaded file
            File uploadedFile = saveUploadedFile(file);
            
            // Extract text from PDF
            String extractedText = pdfReaderService.extractTextFromPdf(uploadedFile);
            if (extractedText.trim().isEmpty()) {
                return new SummaryResponse(file.getOriginalFilename(), null, null, false, 
                                         "No readable text found in the PDF");
            }

            // Generate summary
            String summaryText = textSummarizerService.summarizeText(extractedText, maxWords);
            
            // Generate summary PDF
            File outputDirFile = new File(outputDir);
            File summaryPdf;
            
            if ("detailed".equalsIgnoreCase(summaryType)) {
                int pageCount = pdfReaderService.getPageCount(uploadedFile);
                int wordCount = extractedText.split("\\s+").length;
                summaryPdf = pdfGeneratorService.generateDetailedSummaryPdf(
                    summaryText, file.getOriginalFilename(), outputDirFile, pageCount, wordCount);
            } else {
                summaryPdf = pdfGeneratorService.generateSummaryPdf(
                    summaryText, file.getOriginalFilename(), outputDirFile);
            }

            // Clean up uploaded file
            cleanupFile(uploadedFile);

            logger.info("Successfully processed PDF summary. Output file: {}", summaryPdf.getName());
            return new SummaryResponse(file.getOriginalFilename(), summaryPdf.getName(), 
                                     summaryText, true, "Summary generated successfully");

        } catch (Exception e) {
            logger.error("Error processing PDF summary: {}", e.getMessage(), e);
            return new SummaryResponse(file.getOriginalFilename(), null, null, false, 
                                     "Error processing PDF: " + e.getMessage());
        }
    }

    public SummaryResponse generateTextOnlySummary(MultipartFile file, int maxWords) {
        logger.info("Generating text-only summary for file: {}, maxWords: {}", 
                   file.getOriginalFilename(), maxWords);

        try {
            // Save uploaded file
            File uploadedFile = saveUploadedFile(file);
            
            // Extract text from PDF
            String extractedText = pdfReaderService.extractTextFromPdf(uploadedFile);
            if (extractedText.trim().isEmpty()) {
                return new SummaryResponse(file.getOriginalFilename(), null, null, false, 
                                         "No readable text found in the PDF");
            }

            // Generate summary
            String summaryText = textSummarizerService.summarizeText(extractedText, maxWords);

            // Clean up uploaded file
            cleanupFile(uploadedFile);

            logger.info("Successfully generated text-only summary");
            return new SummaryResponse(file.getOriginalFilename(), null, summaryText, true, 
                                     "Text summary generated successfully");

        } catch (Exception e) {
            logger.error("Error generating text summary: {}", e.getMessage(), e);
            return new SummaryResponse(file.getOriginalFilename(), null, null, false, 
                                     "Error generating summary: " + e.getMessage());
        }
    }

    private File saveUploadedFile(MultipartFile file) throws IOException {
        // Create upload directory if it doesn't exist
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Save file with timestamp to avoid conflicts
        String originalFilename = file.getOriginalFilename();
        String timestamp = String.valueOf(System.currentTimeMillis());
        String filename = timestamp + "_" + originalFilename;
        
        Path filePath = uploadPath.resolve(filename);
        Files.copy(file.getInputStream(), filePath);
        
        return filePath.toFile();
    }

    private void cleanupFile(File file) {
        try {
            if (file.exists()) {
                Files.delete(file.toPath());
                logger.debug("Cleaned up temporary file: {}", file.getName());
            }
        } catch (IOException e) {
            logger.warn("Failed to cleanup temporary file: {}", file.getName(), e);
        }
    }

    public File getOutputFile(String filename) {
        File outputDirFile = new File(outputDir);
        File requestedFile = new File(outputDirFile, filename);
        
        if (requestedFile.exists() && requestedFile.getParent().equals(outputDirFile.getAbsolutePath())) {
            return requestedFile;
        }
        
        return null;
    }
}