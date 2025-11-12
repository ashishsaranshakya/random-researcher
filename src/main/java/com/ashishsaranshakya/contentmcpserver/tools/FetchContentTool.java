package com.ashishsaranshakya.contentmcpserver.tools;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class FetchContentTool {
    public record ScrapeResult(String content) {}

    @Tool(name = "fetch_content",
            description = "Fetches and cleans the main text from a given URL for LLM processing.")
    public ScrapeResult fetchContent(String url) {
        System.out.println("🤖 MCP Server: Executing scrape for URL: " + url);

        try {
            Document doc = Jsoup.connect(url)
                    .timeout(10000)
                    .get();

            Elements paragraphs = doc.select("p");
            String mainText = paragraphs.text();

            return new ScrapeResult(mainText);
        } catch (IOException e) {
            System.err.println("MCP Scrape Error on " + url + ": " + e.getMessage());
            return new ScrapeResult("Error: Unable to retrieve content from the specified URL: " + e.getMessage());
        }
    }
}
