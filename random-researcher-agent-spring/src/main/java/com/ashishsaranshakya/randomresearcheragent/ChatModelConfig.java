package com.ashishsaranshakya.randomresearcheragent;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class ChatModelConfig {

    @Bean
    public ChatModel openaiModel(OpenRouterProperties props) {
        return OpenAiChatModel.builder()
                .openAiApi(OpenAiApi.builder()
                        .baseUrl(props.getBaseUrl())
                        .apiKey(props.getApiKey())
                        .build())
                .defaultOptions(OpenAiChatOptions.builder()
                        .model(props.getModel())
                        .temperature(props.getTemperature())
                        .logprobs(props.isLogprobs())
                        .httpHeaders(props.getHttpHeaders())
                        .build())
                .build();
    }

}
