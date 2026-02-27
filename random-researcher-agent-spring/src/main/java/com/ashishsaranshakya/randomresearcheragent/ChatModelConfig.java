package com.ashishsaranshakya.randomresearcheragent;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
public class ChatModelConfig {

    @Bean
    public ChatModel openaiModel() {
        return OpenAiChatModel.builder()
                .openAiApi(OpenAiApi.builder()
                        .baseUrl("https://openrouter.ai/api")
                        .apiKey("API_KEY")
                        .build())
                .defaultOptions(OpenAiChatOptions.builder()
                        .model("nvidia/nemotron-3-nano-30b-a3b:free")
                        .logprobs(false)
                        .temperature(0.1)
                        .httpHeaders(Map.of("HTTP-Referer", "http://localhost", "X-Title", "Random Researcher"))
                        .build())
                .build();

    }

}
