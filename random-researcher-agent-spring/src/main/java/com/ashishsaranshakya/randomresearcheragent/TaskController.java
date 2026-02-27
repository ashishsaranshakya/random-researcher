package com.ashishsaranshakya.randomresearcheragent;

import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.GraphStateException;
import org.bsc.langgraph4j.spring.ai.agentexecutor.AgentExecutor;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerErrorException;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/agent")
public class TaskController {
    public record AgentRequest(String topic, String email) {}

    private final CompiledGraph<AgentExecutor.State> randomResearcher;
    private final CompiledGraph<AgentExecutor.State> studyStudyAgent;

    public TaskController(ChatModel chatModel, SyncMcpToolCallbackProvider provider) throws GraphStateException {

        List<ToolCallback> tools = Arrays.asList(provider.getToolCallbacks());

        this.randomResearcher = AgentExecutor.builder()
                .chatModel(chatModel)
                .tools(tools)
                .defaultSystem(Prompts.RANDOM_RESEARCHER_SYSTEM_PROMPT)
                .build()
                .compile();
        this.studyStudyAgent = AgentExecutor.builder()
                .chatModel(chatModel)
                .tools(tools)
                .defaultSystem(Prompts.STUDY_NOTES_AGENT_SYSTEM_PROMPT)
                .build()
                .compile();
    }

    @PostMapping("/run-random-researcher")
    public List<String> runRandomResearcher(@RequestBody AgentRequest request) {
        String userPrompt = String.format("Topic: %s\nEmail: %s", request.topic(), request.email());

        var response = randomResearcher.invoke(Map.of("messages", List.of(new UserMessage(userPrompt))));

        return response.orElseThrow(() -> new ServerErrorException("Internal Server Error", new Throwable()))
                .messages()
                .stream()
                .filter(AssistantMessage.class::isInstance)
                .map(AssistantMessage.class::cast)
                .map(AssistantMessage::getText)
                .toList();
    }

    @PostMapping("/run-study-agent")
    public List<String> runStudyAgent(@RequestBody AgentRequest request) {
        String userPrompt = String.format("Topic: %s\nEmail: %s", request.topic(), request.email());

        var response = studyStudyAgent.invoke(Map.of("messages", List.of(new UserMessage(userPrompt))));

        return response.orElseThrow(() -> new ServerErrorException("Internal Server Error", new Throwable()))
                .messages()
                .stream()
                .filter(AssistantMessage.class::isInstance)
                .map(AssistantMessage.class::cast)
                .map(AssistantMessage::getText)
                .toList();
    }
}
