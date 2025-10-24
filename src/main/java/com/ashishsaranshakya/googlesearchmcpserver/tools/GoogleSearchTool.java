package com.ashishsaranshakya.googlesearchmcpserver.tools;


import com.ashishsaranshakya.googlesearchmcpserver.dto.GoogleSearchResponse;
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

    private final RestClient restClient; // <-- Declared final

    // 3. Constructor injection is the correct Spring pattern
    public GoogleSearchTool(RestClient googleRestClient) {
        this.restClient = googleRestClient;
    }

    // 2. Define the Base API URL
    private static final String API_PATH = "/customsearch/v1";
    // 3. Autowire RestClient (Best Practice: Create a singleton bean in a @Configuration class)

    // Define the formal response structure for better LLM integration
    public record SearchResult(String snippet, String source) {}

    @Tool(name = "google_search_tool",
            description = "Searches the web for general knowledge and returns top 5 snippets.")
    public List<SearchResult> searchWeb(String query) {

        try {
            // --- 1. Execute the HTTP GET request using RestClient ---
            GoogleSearchResponse apiResponse = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path(API_PATH)
                            .queryParam("key", API_KEY) // Required: Your API Key
                            .queryParam("cx", CX_ID)    // Required: Your Search Engine ID
                            .queryParam("q", query)     // Required: The user's search query
                            .queryParam("num", 10)       // Optional: Limit results to 5
                            .build())
                    .retrieve()
                    // Throw exception for 4xx/5xx status codes
                    .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                            (req, resp) -> {
                                throw new RuntimeException("Search API Error: " + resp.getStatusCode());
                            })
                    // --- 2. Map the JSON response to your DTO ---
                    .body(GoogleSearchResponse.class);

            // --- 3. Extract and Transform Results for the Agent ---
            if (apiResponse == null || apiResponse.items() == null) {
                return List.of(); // Return empty list if no results
            }

            return apiResponse.items().stream()
                    .map(item -> new SearchResult(
                            item.snippet(), // The summarized text snippet
                            item.link()     // The source URL
                    ))
                    .limit(5) // Ensure we stick to the top 5
                    .toList();

        } catch (Exception e) {
            // --- 4. Handle Errors ---
            System.err.println("MCP Search Error: " + e.getMessage());
            // Return a single failure object or rethrow a structured error
            return List.of(new SearchResult("Search failed due to external API error.", "error"));
        }
    }
}
