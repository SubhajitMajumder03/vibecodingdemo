package com.example.pdfsummarizer.model;

public class SummaryResponse {
    private String originalFilename;
    private String summaryFilename;
    private String summaryText;
    private boolean success;
    private String message;

    public SummaryResponse() {}

    public SummaryResponse(String originalFilename, String summaryFilename, String summaryText, boolean success, String message) {
        this.originalFilename = originalFilename;
        this.summaryFilename = summaryFilename;
        this.summaryText = summaryText;
        this.success = success;
        this.message = message;
    }

    public String getOriginalFilename() {
        return originalFilename;
    }

    public void setOriginalFilename(String originalFilename) {
        this.originalFilename = originalFilename;
    }

    public String getSummaryFilename() {
        return summaryFilename;
    }

    public void setSummaryFilename(String summaryFilename) {
        this.summaryFilename = summaryFilename;
    }

    public String getSummaryText() {
        return summaryText;
    }

    public void setSummaryText(String summaryText) {
        this.summaryText = summaryText;
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