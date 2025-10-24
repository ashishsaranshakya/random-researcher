package com.ashishsaranshakya.contentmcpserver.tools;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class FetchContentTool {


    // Define the output structure for the new tool
    public record ScrapeResult(String content) {}


    // 🚀 NEW TOOL IMPLEMENTATION: fetch_content
    @Tool(name = "fetch_content",
            description = "Fetches and cleans the main text from a given URL for LLM processing.")
    public ScrapeResult fetchContent(String url) {

        System.out.println("🤖 MCP Server: Executing scrape for URL: " + url);

        try {
            // 1. Establish connection and download the HTML document
            // Use a timeout to prevent infinite blocking on dead sites
            Document doc = Jsoup.connect(url)
                    .timeout(10000) // 10 seconds timeout
                    .get();

            // 2. Extract main text content (a common pattern for text extraction)
            // Selects all paragraph elements and joins their text
            Elements paragraphs = doc.select("p");
            String mainText = paragraphs.text();

            // 3. Return the cleaned content
            return new ScrapeResult(mainText);

        } catch (IOException e) {
            // 4. Handle network/I/O errors (e.g., 404, connection refused, timeout)
            System.err.println("MCP Scrape Error on " + url + ": " + e.getMessage());
            return new ScrapeResult("Error: Unable to retrieve content from the specified URL: " + e.getMessage());
        }
    }

}
