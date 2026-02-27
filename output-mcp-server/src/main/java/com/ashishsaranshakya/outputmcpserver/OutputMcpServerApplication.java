package com.ashishsaranshakya.outputmcpserver;

import com.ashishsaranshakya.outputmcpserver.tools.EmailService;
import com.ashishsaranshakya.outputmcpserver.tools.PdfService;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class OutputMcpServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(OutputMcpServerApplication.class, args);
    }

//    @Bean
//    public ToolCallbackProvider weatherTools(PdfService pdfService, EmailService emailService) {
//        return MethodToolCallbackProvider.builder().toolObjects(pdfService, emailService).build();
//    }
}
