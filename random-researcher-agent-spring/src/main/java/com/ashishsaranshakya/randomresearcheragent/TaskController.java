package com.ashishsaranshakya.randomresearcheragent;

import org.bsc.langgraph4j.NodeOutput;
import org.bsc.langgraph4j.prebuilt.MessagesState;
import org.bsc.langgraph4j.serializer.StateSerializer;
import org.bsc.langgraph4j.spring.ai.agent.ReactAgent;
import org.bsc.langgraph4j.spring.ai.agentexecutor.AgentExecutor;
import org.bsc.langgraph4j.spring.ai.serializer.std.SpringAIStateSerializer;
import org.bsc.langgraph4j.state.AgentState;
import org.bsc.langgraph4j.state.AgentStateFactory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.content.Content;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;


@Controller
public class TaskController implements CommandLineRunner {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TaskController.class);

    private final ChatModel chatModel;
    private final List<ToolCallback> tools;

    public TaskController(ChatModel chatModel, SyncMcpToolCallbackProvider provider) {
        this.chatModel = chatModel;
        this.tools = Arrays.asList(provider.getToolCallbacks());
    }

    @Override
    public void run(String... args) throws Exception {

        log.info("Welcome to the Spring Boot CLI application!");
        System.out.println(tools);

        var graph = AgentExecutor.builder()
                .chatModel(chatModel)
                .tools(tools)
                .defaultSystem(Prompts.RANDOM_RESEARCHER_SYSTEM_PROMPT)
                .build();

        var workflow = graph.compile();
        var topic = """
            Understand exception flow in Spring MVC
            
            Learn @ExceptionHandler basics
            """;
        var email = "notificationservice088@gmail.com";

//        var response = workflow.invoke(
//                Map.of(
//                        "messages",
//                        List.of(new UserMessage(
//                                String.format("Topic: %s\nEmail: %s", topic, email)
//                        ))
//                )
//        );
//
//        response.get().messages()
//                .stream()
//                .forEach(System.out::println);





        var result = workflow.stream(Map.of( "messages", new UserMessage(String.format("Topic: %s\nEmail: %s", topic, email))));

        var state = result.stream()
                .peek( s -> System.out.println( s.node() ) )
                .reduce((a, b) -> b)
                .map( NodeOutput::state)
                .orElseThrow();

        log.info( "result: {}", state.lastMessage()
                .map(AssistantMessage.class::cast)
                .map(AssistantMessage::getText)
                .orElseThrow() );
    }
}
