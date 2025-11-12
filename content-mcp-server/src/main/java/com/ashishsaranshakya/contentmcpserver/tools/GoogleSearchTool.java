package com.ashishsaranshakya.contentmcpserver.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;

@Service
public class GoogleSearchTool {
    @Value("${google.search.api-key}")
    private String API_KEY;
    @Value("${google.search.cx-id}")
    private String CX_ID;
    private final RestClient restClient;
    private static final String API_PATH = "/customsearch/v1";

    public GoogleSearchTool(RestClient googleRestClient) {
        this.restClient = googleRestClient;
    }

    public record Item(String title, String link, String snippet) {}
    public record SearchResult(String snippet, String source) {}
    public record GoogleSearchResponse(List<Item> items) {}

    @Tool(name = "google_search_tool",
            description = "Searches the web for general knowledge and returns top 5 snippets.")
    public List<SearchResult> searchWeb(String query) {
        try {
            GoogleSearchResponse apiResponse = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path(API_PATH)
                            .queryParam("key", API_KEY)
                            .queryParam("cx", CX_ID)
                            .queryParam("q", query)
                            .queryParam("num", 10)
                            .build())
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                            (req, resp) -> {
                                throw new RuntimeException("Search API Error: " + resp.getStatusCode());
                            })
                    .body(GoogleSearchResponse.class);

            if (apiResponse == null || apiResponse.items() == null) {
                return List.of();
            }

            return apiResponse.items().stream()
                    .map(item -> new SearchResult(
                            item.snippet(),
                            item.link()
                    ))
                    .limit(10)
                    .toList();

        } catch (Exception e) {
            System.err.println("MCP Search Error: " + e.getMessage());
            return List.of(new SearchResult("Search failed due to external API error.", "error"));
        }
    }
}
