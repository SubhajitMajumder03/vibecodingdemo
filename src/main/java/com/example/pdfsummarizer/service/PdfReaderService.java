package com.example.pdfsummarizer.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;

@Service
public class PdfReaderService {

    private static final Logger logger = LoggerFactory.getLogger(PdfReaderService.class);

    public String extractTextFromPdf(File pdfFile) throws IOException {
        logger.debug("Starting text extraction from PDF: {}", pdfFile.getName());
        
        try (PDDocument document = PDDocument.load(pdfFile)) {
            PDFTextStripper pdfStripper = new PDFTextStripper();
            String text = pdfStripper.getText(document);
            
            logger.debug("Successfully extracted {} characters from PDF", text.length());
            return text;
        } catch (IOException e) {
            logger.error("Error extracting text from PDF: {}", e.getMessage(), e);
            throw new IOException("Failed to extract text from PDF: " + e.getMessage(), e);
        }
    }

    public int getPageCount(File pdfFile) throws IOException {
        try (PDDocument document = PDDocument.load(pdfFile)) {
            return document.getNumberOfPages();
        }
    }

    public String extractTextFromPageRange(File pdfFile, int startPage, int endPage) throws IOException {
        logger.debug("Extracting text from pages {} to {} of PDF: {}", startPage, endPage, pdfFile.getName());
        
        try (PDDocument document = PDDocument.load(pdfFile)) {
            PDFTextStripper pdfStripper = new PDFTextStripper();
            pdfStripper.setStartPage(startPage);
            pdfStripper.setEndPage(endPage);
            
            String text = pdfStripper.getText(document);
            logger.debug("Successfully extracted {} characters from pages {} to {}", text.length(), startPage, endPage);
            return text;
        } catch (IOException e) {
            logger.error("Error extracting text from PDF pages {} to {}: {}", startPage, endPage, e.getMessage(), e);
            throw new IOException("Failed to extract text from PDF pages: " + e.getMessage(), e);
        }
    }
}