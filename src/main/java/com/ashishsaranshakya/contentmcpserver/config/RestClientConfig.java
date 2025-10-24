package com.ashishsaranshakya.contentmcpserver.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    // Define the host/scheme only once here.
    private static final String GOOGLE_SEARCH_BASE_URL = "https://www.googleapis.com";

    @Bean
    public RestClient googleRestClient() {
        return RestClient.builder()
                .baseUrl(GOOGLE_SEARCH_BASE_URL)
                .build();
    }
}
