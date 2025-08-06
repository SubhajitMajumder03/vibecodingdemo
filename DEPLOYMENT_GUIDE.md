# PDF Summarizer Deployment Guide

## Quick Start

### Prerequisites
- Java 17 or higher
- Maven 3.6 or higher
- OpenAI API Key (optional, for enhanced summarization)

### Installation Steps

1. **Clone the repository and navigate to the project:**
   ```bash
   cd /workspace
   ```

2. **Build the application:**
   ```bash
   mvn clean compile
   ```

3. **Set OpenAI API Key (optional):**
   ```bash
   export OPENAI_API_KEY=your-openai-api-key-here
   ```

4. **Run the application:**
   ```bash
   mvn spring-boot:run
   ```

5. **Test the application:**
   ```bash
   curl http://localhost:8080/api/pdf-summarizer/health
   ```

## API Endpoints

### 1. Health Check
- **URL:** `GET /api/pdf-summarizer/health`
- **Response:** `PDF Summarizer Service is running`

### 2. Service Information
- **URL:** `GET /api/pdf-summarizer/info`
- **Response:** JSON with service details and available endpoints

### 3. Generate PDF Summary
- **URL:** `POST /api/pdf-summarizer/summarize`
- **Parameters:**
  - `file` (required): PDF file to summarize
  - `summaryType` (optional): "basic" or "detailed" (default: "basic")
  - `maxWords` (optional): Maximum words in summary, 50-1000 (default: 300)
- **Response:** JSON with summary text and generated PDF filename

### 4. Generate Text-Only Summary
- **URL:** `POST /api/pdf-summarizer/summarize-text-only`
- **Parameters:**
  - `file` (required): PDF file to summarize
  - `maxWords` (optional): Maximum words in summary, 50-1000 (default: 300)
- **Response:** JSON with summary text only

### 5. Download Summary PDF
- **URL:** `GET /api/pdf-summarizer/download/{filename}`
- **Response:** PDF file download

## Example Usage

### Using cURL

1. **Text-only summary:**
   ```bash
   curl -X POST http://localhost:8080/api/pdf-summarizer/summarize-text-only \
     -F "file=@document.pdf" \
     -F "maxWords=200"
   ```

2. **Basic PDF summary:**
   ```bash
   curl -X POST http://localhost:8080/api/pdf-summarizer/summarize \
     -F "file=@document.pdf" \
     -F "summaryType=basic" \
     -F "maxWords=300"
   ```

3. **Detailed PDF summary:**
   ```bash
   curl -X POST http://localhost:8080/api/pdf-summarizer/summarize \
     -F "file=@document.pdf" \
     -F "summaryType=detailed" \
     -F "maxWords=500"
   ```

### Using JavaScript/HTML

```html
<!DOCTYPE html>
<html>
<head>
    <title>PDF Summarizer</title>
</head>
<body>
    <h1>PDF Summarizer</h1>
    <form id="uploadForm" enctype="multipart/form-data">
        <input type="file" id="pdfFile" accept=".pdf" required>
        <select id="summaryType">
            <option value="basic">Basic Summary</option>
            <option value="detailed">Detailed Summary</option>
        </select>
        <input type="number" id="maxWords" placeholder="Max words (50-1000)" min="50" max="1000" value="300">
        <button type="submit">Generate Summary</button>
    </form>
    
    <div id="result"></div>

    <script>
        document.getElementById('uploadForm').addEventListener('submit', async (e) => {
            e.preventDefault();
            
            const formData = new FormData();
            formData.append('file', document.getElementById('pdfFile').files[0]);
            formData.append('summaryType', document.getElementById('summaryType').value);
            formData.append('maxWords', document.getElementById('maxWords').value);
            
            try {
                const response = await fetch('/api/pdf-summarizer/summarize', {
                    method: 'POST',
                    body: formData
                });
                
                const result = await response.json();
                document.getElementById('result').innerHTML = `
                    <h3>Summary Result</h3>
                    <p><strong>Status:</strong> ${result.success ? 'Success' : 'Failed'}</p>
                    <p><strong>Message:</strong> ${result.message}</p>
                    <p><strong>Summary:</strong></p>
                    <pre>${result.summaryText}</pre>
                    ${result.summaryFilename ? `<p><a href="/api/pdf-summarizer/download/${result.summaryFilename}">Download PDF Summary</a></p>` : ''}
                `;
            } catch (error) {
                document.getElementById('result').innerHTML = `<p style="color: red;">Error: ${error.message}</p>`;
            }
        });
    </script>
</body>
</html>
```

## Configuration

### Application Properties
Edit `src/main/resources/application.properties`:

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

## Production Deployment

### Building for Production
```bash
mvn clean package -Pprod
```

### Docker Deployment
1. Create `Dockerfile`:
   ```dockerfile
   FROM openjdk:17-jre-slim
   
   WORKDIR /app
   COPY target/pdf-summarizer-1.0.0.jar app.jar
   
   # Create directories
   RUN mkdir -p uploads outputs
   
   EXPOSE 8080
   
   ENTRYPOINT ["java", "-jar", "app.jar"]
   ```

2. Build and run:
   ```bash
   docker build -t pdf-summarizer .
   docker run -p 8080:8080 -e OPENAI_API_KEY=your-key pdf-summarizer
   ```

### Systemd Service
Create `/etc/systemd/system/pdf-summarizer.service`:

```ini
[Unit]
Description=PDF Summarizer Service
After=network.target

[Service]
Type=simple
User=ubuntu
WorkingDirectory=/opt/pdf-summarizer
ExecStart=/usr/bin/java -jar pdf-summarizer-1.0.0.jar
Restart=always
RestartSec=10
Environment=OPENAI_API_KEY=your-api-key-here

[Install]
WantedBy=multi-user.target
```

## Monitoring and Troubleshooting

### Health Checks
- **Health endpoint:** `GET /api/pdf-summarizer/health`
- **Logs:** Check Spring Boot application logs for detailed error information

### Common Issues

1. **Large file upload errors:**
   - Increase `spring.servlet.multipart.max-file-size` in application.properties

2. **OpenAI API errors:**
   - Verify API key is correctly set
   - Check API rate limits
   - Application will fallback to extractive summarization

3. **PDF processing errors:**
   - Ensure PDF contains readable text
   - Check PDF is not password protected
   - Verify PDF is not corrupted

4. **Memory issues:**
   - Increase JVM heap size: `-Xmx2g`
   - Monitor application memory usage

### Performance Tuning
- **JVM Options:** `-XX:+UseG1GC -Xmx2g -Xms1g`
- **Connection Pool:** Configure database connection pooling if needed
- **Caching:** Implement caching for frequently processed documents

## Security Considerations

1. **File Upload Security:**
   - File type validation (PDF only)
   - File size limits (50MB max)
   - Path traversal protection

2. **API Security:**
   - Consider adding authentication/authorization
   - Rate limiting for production use
   - HTTPS in production

3. **Data Privacy:**
   - Automatic temporary file cleanup
   - Secure API key storage
   - Consider data encryption at rest

## Scaling

### Horizontal Scaling
- Deploy multiple instances behind a load balancer
- Use shared storage for upload/output directories
- Consider using cloud storage (AWS S3, Google Cloud Storage)

### Vertical Scaling
- Increase memory allocation for larger PDFs
- Optimize JVM settings for your hardware
- Use SSD storage for better I/O performance