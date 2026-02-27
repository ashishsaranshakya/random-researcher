package com.ashishsaranshakya.outputmcpserver.tools;

import com.lowagie.text.Document;
import com.lowagie.text.html.simpleparser.HTMLWorker;
import com.lowagie.text.html.simpleparser.StyleSheet;
import com.lowagie.text.pdf.PdfWriter;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.data.MutableDataSet;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.annotation.McpToolParam;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.StringReader;

@Service
public class PdfService {
    @Value("${output.subdir.name}")
    private String OUPUT_SUBDIR;

    private String convertMarkdownToHtml(String markdown) {
        MutableDataSet options = new MutableDataSet();
        Parser parser = Parser.builder(options).build();
        HtmlRenderer renderer = HtmlRenderer.builder(options).build();

        com.vladsch.flexmark.util.ast.Document document = parser.parse(markdown);

        String htmlBody = renderer.render(document);

        return """
            <!DOCTYPE html>
            <html>
            <body>
                %s
            </body>
            </html>
            """.formatted(htmlBody);
    }

    @McpTool(name = "generate_pdf",
            description = "Converts final markdown study content to a PDF file and returns the local file path.")
    public String generatePdf(
            @McpToolParam String title,
            @McpToolParam String markdownContent) {
        String fileName = title.replaceAll("[^a-zA-Z0-9\\s]", "").replace(' ', '_') + "_" + System.currentTimeMillis() + ".pdf";
        String filePath = System.getProperty("user.dir") + File.separator + OUPUT_SUBDIR + File.separator + fileName;

        String htmlToRender = convertMarkdownToHtml(markdownContent);
        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            Document document = new Document();
            PdfWriter writer = PdfWriter.getInstance(document, fos);
            document.open();

            StyleSheet styles = new StyleSheet();

            HTMLWorker worker = new HTMLWorker(document);
            worker.setStyleSheet(styles);

            worker.startDocument();
            worker.parse(new StringReader(htmlToRender));
            worker.endDocument();

            document.close();

            System.out.println("🤖 PDF Generated successfully: " + fileName);
            return fileName;
        } catch (Exception e) {
            System.err.println("PDF Generation Failed: " + e.getMessage());
            throw new RuntimeException("PDF generation failed for topic: " + title + ". Error: " + e.getMessage());
        }
    }
}
