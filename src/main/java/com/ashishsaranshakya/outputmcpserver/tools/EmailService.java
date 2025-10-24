package com.ashishsaranshakya.outputmcpserver.tools;

import jakarta.mail.internet.MimeMessage;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
public class EmailService {

    // ... (Inside the SynthesisTool component) ...
    @Autowired
    private JavaMailSender mailSender; // Injected by Spring Boot Starter Mail

    // 🚩 Tool 2: Email Sending
    @Tool(name = "send_study_guide_email",
            description = "Sends the generated PDF file as an attachment to the specified recipient.")
    public String sendEmail(String recipientEmail, String subject, String pdfFilePath) {

        // Use the username from application.properties as the sender
        String sender = "your-email@gmail.com";

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true); // 'true' enables multipart for attachments

            helper.setTo(recipientEmail);
            helper.setSubject("[DASGS] Daily Knowledge: " + subject);
            helper.setFrom(sender);
            helper.setText("Hello! Your daily knowledge synthesis for '" + subject +
                    "' is attached. Enjoy!", false); // 'false' for plain text body

            // Add Attachment
            FileSystemResource file = new FileSystemResource(new File(pdfFilePath));
            helper.addAttachment(file.getFilename(), file);

            mailSender.send(message);

            System.out.println("🤖 Email sent successfully to: " + recipientEmail);
            return "Email sent successfully to " + recipientEmail + " with PDF attachment.";

        } catch (Exception e) {
            System.err.println("Email Sending Failed: " + e.getMessage());
            // Do NOT rethrow. This tool should gracefully fail on network/auth errors.
            return "Error: Could not send email. Check SMTP credentials or recipient address.";
        }
    }
}
