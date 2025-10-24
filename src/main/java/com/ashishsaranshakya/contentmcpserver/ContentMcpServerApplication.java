package com.ashishsaranshakya.contentmcpserver;

import com.ashishsaranshakya.contentmcpserver.tools.FetchContentTool;
import com.ashishsaranshakya.contentmcpserver.tools.GoogleSearchTool;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class ContentMcpServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ContentMcpServerApplication.class, args);
    }

    @Bean
    public ToolCallbackProvider weatherTools(GoogleSearchTool googleSearchTool, FetchContentTool fetchContentTool) {
        return MethodToolCallbackProvider.builder().toolObjects(googleSearchTool, fetchContentTool).build();
    }
}
