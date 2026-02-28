package com.ashishsaranshakya.randomresearcheragent;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

@ConfigurationProperties(prefix = "app.openrouter")
public class OpenRouterProperties {

    private String baseUrl;
    private String apiKey;
    private String model;
    private double temperature;
    private boolean logprobs;
    private Map<String, String> httpHeaders;

    // getters & setters

    public String getBaseUrl() { return baseUrl; }
    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }

    public String getApiKey() { return apiKey; }
    public void setApiKey(String apiKey) { this.apiKey = apiKey; }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public double getTemperature() { return temperature; }
    public void setTemperature(double temperature) { this.temperature = temperature; }

    public boolean isLogprobs() { return logprobs; }
    public void setLogprobs(boolean logprobs) { this.logprobs = logprobs; }

    public Map<String, String> getHttpHeaders() { return httpHeaders; }
    public void setHttpHeaders(Map<String, String> httpHeaders) { this.httpHeaders = httpHeaders; }

    @Override
    public String toString() {
        return "OpenRouterProperties{" +
                "baseUrl='" + baseUrl + '\'' +
                ", apiKey='" + apiKey + '\'' +
                ", model='" + model + '\'' +
                ", temperature=" + temperature +
                ", logprobs=" + logprobs +
                ", httpHeaders=" + httpHeaders +
                '}';
    }
}
