package com.ashishsaranshakya.outputmcpserver.tools;

import jakarta.mail.internet.MimeMessage;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.annotation.McpToolParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
public class EmailService {
    public enum ContentType {
        TEXT,
        HTML
    }

    @Autowired
    private JavaMailSender mailSender;

    @Value("${output.subdir.name}")
    private String OUPUT_SUBDIR;

    @McpTool(name = "send_email",
            description = "Send the generated PDF files as an attachments to the specified recipient.")
    public String sendEmail(
            @McpToolParam String recipientEmail,
            @McpToolParam String subject,
            @McpToolParam String content,
            @McpToolParam ContentType type,
            @McpToolParam String[] pdfFilePath) {
        String sender = "your-email@gmail.com";

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(recipientEmail);
            helper.setSubject(subject);
            helper.setFrom(sender);
            switch (type) {
                case HTML -> helper.setText(content, true);
                case TEXT -> helper.setText(content, false);
            }

            for(String pdfPath : pdfFilePath) {
                pdfPath = System.getProperty("user.dir") + File.separator + OUPUT_SUBDIR + File.separator + pdfPath;
                FileSystemResource file = new FileSystemResource(new File(pdfPath));
                helper.addAttachment(file.getFilename(), file);
            }

            mailSender.send(message);

            System.out.println("🤖 Email sent successfully to: " + recipientEmail);
            return "Email sent successfully to " + recipientEmail + " with PDF attachments.";

        } catch (Exception e) {
            System.err.println("Email Sending Failed: " + e.getMessage());
            return "Error: Could not send email. Check SMTP credentials or recipient address.";
        }
    }
}
