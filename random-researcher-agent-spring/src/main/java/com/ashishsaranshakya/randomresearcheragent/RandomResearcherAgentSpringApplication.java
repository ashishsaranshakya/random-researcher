package com.ashishsaranshakya.randomresearcheragent;

import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.service.tool.ToolExecutor;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Map;

@SpringBootApplication
public class RandomResearcherAgentSpringApplication {
    public static void main(String[] args) {
        SpringApplication.run(RandomResearcherAgentSpringApplication.class, args);
    }
}
