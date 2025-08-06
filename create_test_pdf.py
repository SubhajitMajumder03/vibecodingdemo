#!/usr/bin/env python3

from reportlab.lib.pagesizes import letter
from reportlab.platypus import SimpleDocTemplate, Paragraph, Spacer
from reportlab.lib.styles import getSampleStyleSheet, ParagraphStyle
from reportlab.lib.units import inch

def create_pdf_from_text(text_file, pdf_file):
    # Read the text content
    with open(text_file, 'r', encoding='utf-8') as f:
        content = f.read()
    
    # Create PDF document
    doc = SimpleDocTemplate(pdf_file, pagesize=letter,
                           rightMargin=72, leftMargin=72,
                           topMargin=72, bottomMargin=18)
    
    # Get styles
    styles = getSampleStyleSheet()
    title_style = ParagraphStyle(
        'CustomTitle',
        parent=styles['Heading1'],
        fontSize=16,
        spaceAfter=30,
        alignment=1  # Center alignment
    )
    
    heading_style = ParagraphStyle(
        'CustomHeading',
        parent=styles['Heading2'],
        fontSize=14,
        spaceAfter=12,
        spaceBefore=12
    )
    
    normal_style = styles['Normal']
    normal_style.fontSize = 11
    normal_style.spaceAfter = 12
    
    # Build the story
    story = []
    
    lines = content.split('\n')
    for line in lines:
        line = line.strip()
        if not line:
            continue
            
        # Title (first line)
        if line == lines[0].strip():
            story.append(Paragraph(line, title_style))
            story.append(Spacer(1, 12))
        # Headings (lines that don't start with a number or dash and are not too long)
        elif (not line[0].isdigit() and not line.startswith('-') and 
              len(line) < 100 and not line.endswith('.') and
              line not in ['Introduction', 'What is Artificial Intelligence?', 
                          'Applications of AI', 'Challenges and Considerations',
                          'Future Outlook', 'Conclusion']):
            continue  # Skip intermediate processing for this simple version
        elif line in ['Introduction', 'What is Artificial Intelligence?', 
                     'Applications of AI', 'Challenges and Considerations',
                     'Future Outlook', 'Conclusion']:
            story.append(Paragraph(line, heading_style))
        else:
            # Regular paragraph
            story.append(Paragraph(line, normal_style))
    
    # Build PDF
    doc.build(story)
    print(f"PDF created successfully: {pdf_file}")

if __name__ == "__main__":
    create_pdf_from_text("sample-document.txt", "sample-document.pdf")