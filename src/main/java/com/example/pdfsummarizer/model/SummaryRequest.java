package com.example.pdfsummarizer.model;

public class SummaryRequest {
    private String filename;
    private String summaryType;
    private int maxWords;

    public SummaryRequest() {}

    public SummaryRequest(String filename, String summaryType, int maxWords) {
        this.filename = filename;
        this.summaryType = summaryType;
        this.maxWords = maxWords;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getSummaryType() {
        return summaryType;
    }

    public void setSummaryType(String summaryType) {
        this.summaryType = summaryType;
    }

    public int getMaxWords() {
        return maxWords;
    }

    public void setMaxWords(int maxWords) {
        this.maxWords = maxWords;
    }
}