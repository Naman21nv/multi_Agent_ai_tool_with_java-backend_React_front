package com.aiagent.tool;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Web search tool that uses DuckDuckGo HTML scraping.
 * Equivalent to the Python `web_search` tool using DDGS.
 * No API key required — fully privacy-preserving.
 */
@Component
public class WebSearchTool implements AgentTool {

    private static final Logger log = LoggerFactory.getLogger(WebSearchTool.class);
    private static final String DDG_URL = "https://html.duckduckgo.com/html/?q=";

    @Override
    public String getName() {
        return "web_search";
    }

    @Override
    public String getDescription() {
        return "Search the web for real-time info. Best for news or facts.";
    }

    @Override
    public Map<String, Object> getParameterSchema() {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");

        Map<String, Object> properties = new HashMap<>();
        Map<String, Object> queryProp = new HashMap<>();
        queryProp.put("type", "string");
        queryProp.put("description", "The search query");
        properties.put("query", queryProp);

        schema.put("properties", properties);
        schema.put("required", new String[]{"query"});
        return schema;
    }

    @Override
    public String execute(Map<String, String> arguments) throws Exception {
        String query = arguments.get("query");
        if (query == null || query.isBlank()) {
            return "Error: No search query provided.";
        }

        log.info("Executing web search for: {}", query);

        try {
            String encoded = URLEncoder.encode(query, StandardCharsets.UTF_8);
            Document doc = Jsoup.connect(DDG_URL + encoded)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .timeout(10000)
                    .get();

            Elements results = doc.select(".result");
            if (results.isEmpty()) {
                return "No results found for: " + query;
            }

            StringBuilder sb = new StringBuilder();
            int count = 0;
            for (var result : results) {
                if (count >= 5) break;

                String title = result.select(".result__a").text();
                String snippet = result.select(".result__snippet").text();
                String url = result.select(".result__a").attr("href");

                if (!title.isEmpty()) {
                    sb.append("Title: ").append(title).append("\n");
                    sb.append("Snippet: ").append(snippet).append("\n");
                    sb.append("URL: ").append(url).append("\n\n");
                    count++;
                }
            }

            return sb.length() > 0 ? sb.toString() : "No relevant results found for: " + query;

        } catch (IOException e) {
            log.error("Web search failed for query: {}", query, e);
            return "Error performing web search: " + e.getMessage();
        }
    }
}
