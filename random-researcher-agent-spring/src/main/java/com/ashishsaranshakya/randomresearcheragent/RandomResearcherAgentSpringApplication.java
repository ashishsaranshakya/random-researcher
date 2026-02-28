package com.ashishsaranshakya.randomresearcheragent;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;


@SpringBootApplication
@EnableConfigurationProperties(OpenRouterProperties.class)
public class RandomResearcherAgentSpringApplication {
    public static void main(String[] args) {
        SpringApplication.run(RandomResearcherAgentSpringApplication.class, args);
    }
}
