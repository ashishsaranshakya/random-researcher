package com.ashishsaranshakya.outputmcpserver.tools;

import com.lowagie.text.Document;
import com.lowagie.text.Paragraph;
import com.lowagie.text.html.simpleparser.HTMLWorker;
import com.lowagie.text.html.simpleparser.StyleSheet;
import com.lowagie.text.pdf.PdfWriter;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.data.MutableDataSet;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.StringReader;

// NOTE: You would typically use a library like 'flexmark' to convert Markdown to HTML,
// and then use OpenPDF to convert the HTML to a PDF. This simplified example uses basic text.

@Service
public class PdfService {

    private String convertMarkdownToHtml(String markdown) {
        // Configure Flexmark for standard Markdown processing
        MutableDataSet options = new MutableDataSet();
        Parser parser = Parser.builder(options).build();
        HtmlRenderer renderer = HtmlRenderer.builder(options).build();

        // 1. Parse the Markdown text into a Document object
        com.vladsch.flexmark.util.ast.Document document = parser.parse(markdown);

        // 2. Render the Document object to an HTML string
        String htmlBody = renderer.render(document);

        // 3. Wrap the HTML body with minimal CSS for better PDF styling (Crucial for OpenPDF appearance!)
        return """
            <!DOCTYPE html>
            <html>
            <body>
                %s
            </body>
            </html>
            """.formatted(htmlBody);
    }

    // 🚩 Tool 1: PDF Generation
    @Tool(name = "generate_pdf",
            description = "Converts final markdown study content to a PDF file and returns the local file path.")
    public String generatePdf(String title, String markdownContent) {
        String fileName = title.replaceAll("[^a-zA-Z0-9\\s]", "").replace(' ', '_') + "_" + System.currentTimeMillis() + ".pdf";
        String filePath = System.getProperty("java.io.tmpdir") + File.separator + fileName;

        String htmlToRender = convertMarkdownToHtml(markdownContent);
        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            Document document = new Document();
            PdfWriter writer = PdfWriter.getInstance(document, fos);
            document.open();

            // 2. Use HTMLWorker to parse the formatted HTML and write it to the PDF Document
            StyleSheet styles = new StyleSheet();
//            styles.loadTagStyle(StyleSheet.DEFAULT, "body", "leading", "16,0");

            // HTMLWorker converts the HTML tags into OpenPDF's internal components (Paragraphs, Lists, etc.)
            HTMLWorker worker = new HTMLWorker(document);
            worker.setStyleSheet(styles);

            // Read the HTML content from a StringReader
            worker.startDocument();
            worker.parse(new StringReader(htmlToRender));
            worker.endDocument();

            document.close();

            System.out.println("🤖 PDF Generated successfully at: " + filePath);
            return filePath;

        } catch (Exception e) {
            System.err.println("PDF Generation Failed: " + e.getMessage());
            // Log the HTML content itself for debugging if the error is due to bad HTML structure
            throw new RuntimeException("PDF generation failed for topic: " + title + ". Error: " + e.getMessage());
        }
    }
}
