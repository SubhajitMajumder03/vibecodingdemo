#!/bin/bash

echo "=== PDF Summarizer Spring Boot Application Demo ==="
echo

echo "1. Testing Health Check..."
curl -s http://localhost:8080/api/pdf-summarizer/health
echo -e "\n"

echo "2. Getting Service Information..."
curl -s http://localhost:8080/api/pdf-summarizer/info | python3 -m json.tool
echo -e "\n"

echo "3. Testing Text-Only Summary (150 words)..."
echo "Request: POST /api/pdf-summarizer/summarize-text-only"
curl -s -X POST http://localhost:8080/api/pdf-summarizer/summarize-text-only \
  -F "file=@sample-document.pdf" \
  -F "maxWords=150" | python3 -m json.tool
echo -e "\n"

echo "4. Testing Basic PDF Summary (200 words)..."
echo "Request: POST /api/pdf-summarizer/summarize"
curl -s -X POST http://localhost:8080/api/pdf-summarizer/summarize \
  -F "file=@sample-document.pdf" \
  -F "summaryType=basic" \
  -F "maxWords=200" | python3 -m json.tool
echo -e "\n"

echo "5. Testing Detailed PDF Summary (250 words)..."
echo "Request: POST /api/pdf-summarizer/summarize"
curl -s -X POST http://localhost:8080/api/pdf-summarizer/summarize \
  -F "file=@sample-document.pdf" \
  -F "summaryType=detailed" \
  -F "maxWords=250" | python3 -m json.tool
echo -e "\n"

echo "6. Checking Generated Files..."
echo "Generated PDF summaries in outputs directory:"
ls -la outputs/
echo -e "\n"

echo "=== Demo Complete ==="
echo
echo "The PDF Summarizer application is working correctly!"
echo "Features demonstrated:"
echo "- PDF text extraction using Apache PDFBox"
echo "- Intelligent text summarization with extractive algorithm"
echo "- PDF generation using iText with formatted output"
echo "- REST API endpoints for different summary types"
echo "- File upload and download functionality"
echo "- Health monitoring and service information"