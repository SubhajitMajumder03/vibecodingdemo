# PDF Summarizer Spring Boot Application

A Spring Boot application that reads PDF files, extracts text content, and generates context summaries in PDF format.

## Features

- **PDF Text Extraction**: Extract text content from PDF files using Apache PDFBox
- **Intelligent Summarization**: Generate summaries using OpenAI GPT or fallback to extractive summarization
- **PDF Generation**: Create well-formatted summary PDFs using iText
- **Multiple Summary Types**: Basic summaries and detailed summaries with statistics
- **REST API**: Easy-to-use REST endpoints for file upload and processing
- **File Download**: Download generated summary PDFs
- **Health Monitoring**: Built-in health check and service information endpoints

## Prerequisites

- Java 17 or higher
- Maven 3.6 or higher
- OpenAI API Key (optional, for enhanced summarization)

## Quick Start

### 1. Clone and Build

```bash
git clone <repository-url>
cd pdf-summarizer
mvn clean install
```

### 2. Configuration

Set up your configuration in `src/main/resources/application.properties`:

```properties
# Server configuration
server.port=8080
server.servlet.context-path=/api

# File upload configuration
spring.servlet.multipart.max-file-size=50MB
spring.servlet.multipart.max-request-size=50MB

# Application configuration
app.upload.dir=./uploads
app.output.dir=./outputs

# OpenAI configuration (optional)
openai.api.key=${OPENAI_API_KEY:}
```

### 3. Set OpenAI API Key (Optional)

For enhanced AI-powered summarization, set your OpenAI API key:

```bash
export OPENAI_API_KEY=your-openai-api-key-here
```

### 4. Run the Application

```bash
mvn spring-boot:run
```

Or run the JAR file:

```bash
java -jar target/pdf-summarizer-1.0.0.jar
```

The application will start on `http://localhost:8080/api`

## API Endpoints

### 1. Generate PDF Summary

**POST** `/pdf-summarizer/summarize`

Upload a PDF file and generate a summary PDF.

**Parameters:**
- `file` (required): PDF file to summarize
- `summaryType` (optional): "basic" or "detailed" (default: "basic")
- `maxWords` (optional): Maximum words in summary, 50-1000 (default: 300)

**Example:**
```bash
curl -X POST http://localhost:8080/api/pdf-summarizer/summarize \
  -F "file=@document.pdf" \
  -F "summaryType=detailed" \
  -F "maxWords=500"
```

**Response:**
```json
{
  "originalFilename": "document.pdf",
  "summaryFilename": "document_summary_20241208_143022.pdf",
  "summaryText": "This document discusses...",
  "success": true,
  "message": "Summary generated successfully"
}
```

### 2. Generate Text-Only Summary

**POST** `/pdf-summarizer/summarize-text-only`

Upload a PDF file and get only the text summary (no PDF generated).

**Parameters:**
- `file` (required): PDF file to summarize
- `maxWords` (optional): Maximum words in summary, 50-1000 (default: 300)

**Example:**
```bash
curl -X POST http://localhost:8080/api/pdf-summarizer/summarize-text-only \
  -F "file=@document.pdf" \
  -F "maxWords=200"
```

### 3. Download Summary PDF

**GET** `/pdf-summarizer/download/{filename}`

Download a generated summary PDF file.

**Example:**
```bash
curl -X GET http://localhost:8080/api/pdf-summarizer/download/document_summary_20241208_143022.pdf \
  -O -J
```

### 4. Health Check

**GET** `/pdf-summarizer/health`

Check if the service is running.

**Example:**
```bash
curl http://localhost:8080/api/pdf-summarizer/health
```

### 5. Service Information

**GET** `/pdf-summarizer/info`

Get service information and available endpoints.

**Example:**
```bash
curl http://localhost:8080/api/pdf-summarizer/info
```

## Summary Types

### Basic Summary
- Clean, formatted summary PDF
- Original document information
- Generated timestamp
- Summary content with proper formatting

### Detailed Summary
- All basic summary features
- Document statistics (page count, word count)
- Summary statistics (compression ratio)
- Enhanced formatting and layout

## Summarization Methods

### 1. OpenAI-Powered Summarization (Primary)
- Uses GPT-3.5-turbo-instruct for high-quality summaries
- Requires OpenAI API key
- Provides context-aware, coherent summaries

### 2. Extractive Summarization (Fallback)
- Frequency-based sentence scoring
- Position-based importance weighting
- Maintains original sentence structure
- Works without external dependencies

## Architecture

```
src/main/java/com/example/pdfsummarizer/
├── PdfSummarizerApplication.java          # Main Spring Boot application
├── controller/
│   └── PdfSummarizerController.java       # REST API endpoints
├── service/
│   ├── PdfReaderService.java              # PDF text extraction
│   ├── TextSummarizerService.java         # Text summarization
│   ├── PdfGeneratorService.java           # PDF generation
│   └── PdfSummarizerService.java          # Main orchestration service
└── model/
    ├── SummaryRequest.java                # Request models
    └── SummaryResponse.java               # Response models
```

## Dependencies

- **Spring Boot 3.2.0**: Web framework and dependency injection
- **Apache PDFBox 3.0.1**: PDF text extraction
- **iText 5.5.13.3**: PDF generation
- **OpenAI Java Client 0.18.2**: AI-powered summarization
- **Apache Commons IO 2.11.0**: File operations

## Error Handling

The application includes comprehensive error handling:

- File validation (PDF format checking)
- Empty file detection
- PDF processing errors
- Summarization failures with fallback
- File system errors
- Network errors (OpenAI API)

## Security Considerations

- File type validation (PDF only)
- File size limits (50MB max)
- Path traversal protection
- Temporary file cleanup
- CORS enabled for web clients

## Performance

- Streaming file upload
- Efficient memory usage
- Automatic temporary file cleanup
- Configurable processing limits
- Background processing capability

## Monitoring

- Comprehensive logging with SLF4J
- Health check endpoint
- Service information endpoint
- Detailed error messages
- Processing time tracking

## Development

### Running Tests

```bash
mvn test
```

### Building for Production

```bash
mvn clean package -Pprod
java -jar target/pdf-summarizer-1.0.0.jar
```

### Docker Support

Create a `Dockerfile`:

```dockerfile
FROM openjdk:17-jre-slim

WORKDIR /app
COPY target/pdf-summarizer-1.0.0.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
```

Build and run:

```bash
docker build -t pdf-summarizer .
docker run -p 8080:8080 -e OPENAI_API_KEY=your-key pdf-summarizer
```

## Troubleshooting

### Common Issues

1. **Large File Upload Errors**
   - Increase `spring.servlet.multipart.max-file-size` in application.properties

2. **OpenAI API Errors**
   - Verify API key is correctly set
   - Check API rate limits
   - Application will fallback to extractive summarization

3. **PDF Processing Errors**
   - Ensure PDF contains readable text
   - Check PDF is not password protected
   - Verify PDF is not corrupted

4. **File Permission Errors**
   - Ensure upload and output directories are writable
   - Check disk space availability

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests
5. Submit a pull request

## License

This project is licensed under the MIT License - see the LICENSE file for details.