package com.aiagent.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Service for communicating with the Ollama API.
 * Handles both simple text generation and function-calling (tool_use) chat completions.
 * This is the Java equivalent of ChatOpenAI(base_url="http://localhost:11434/v1") from Python.
 */
@Service
public class OllamaService {

    private static final Logger log = LoggerFactory.getLogger(OllamaService.class);
    private static final MediaType JSON_MEDIA = MediaType.parse("application/json");

    private final OkHttpClient httpClient;
    private final String baseUrl;
    private final String model;
    private final double temperature;
    private final ObjectMapper objectMapper;

    public OllamaService(
            OkHttpClient ollamaHttpClient,
            @Qualifier("ollamaBaseUrl") String baseUrl,
            @Qualifier("ollamaModel") String model,
            @Qualifier("ollamaTemperature") double temperature) {
        this.httpClient = ollamaHttpClient;
        this.baseUrl = baseUrl;
        this.model = model;
        this.temperature = temperature;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Simple text generation (used by SummarizerTool).
     */
    public String generateSimple(String prompt, double temp) throws IOException {
        ObjectNode body = objectMapper.createObjectNode();
        body.put("model", model);
        body.put("prompt", prompt);
        body.put("stream", false);

        ObjectNode options = objectMapper.createObjectNode();
        options.put("temperature", temp);
        body.set("options", options);

        Request request = new Request.Builder()
                .url(baseUrl + "/api/generate")
                .post(RequestBody.create(body.toString(), JSON_MEDIA))
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Ollama API error: " + response.code() + " " + response.message());
            }
            JsonNode json = objectMapper.readTree(response.body().string());
            return json.path("response").asText("");
        }
    }

    /**
     * Chat completion with function calling support (ReAct agent loop).
     * Returns the raw JSON response for parsing tool calls.
     */
    public JsonNode chatWithTools(List<Map<String, Object>> messages, List<Map<String, Object>> tools) throws IOException {
        ObjectNode body = objectMapper.createObjectNode();
        body.put("model", model);
        body.put("stream", false);

        // Build messages array
        ArrayNode messagesNode = objectMapper.valueToTree(messages);
        body.set("messages", messagesNode);

        // Build tools array if provided
        if (tools != null && !tools.isEmpty()) {
            ArrayNode toolsNode = objectMapper.valueToTree(tools);
            body.set("tools", toolsNode);
        }

        ObjectNode options = objectMapper.createObjectNode();
        options.put("temperature", temperature);
        body.set("options", options);

        log.debug("Sending chat request to Ollama with {} messages and {} tools",
                messages.size(), tools != null ? tools.size() : 0);

        Request request = new Request.Builder()
                .url(baseUrl + "/api/chat")
                .post(RequestBody.create(body.toString(), JSON_MEDIA))
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "No body";
                throw new IOException("Ollama chat API error: " + response.code() + " - " + errorBody);
            }
            return objectMapper.readTree(response.body().string());
        }
    }

    /**
     * Health check — verify Ollama is reachable and serving the configured model.
     */
    public boolean isHealthy() {
        try {
            Request request = new Request.Builder()
                    .url(baseUrl + "/api/tags")
                    .get()
                    .build();
            try (Response response = httpClient.newCall(request).execute()) {
                return response.isSuccessful();
            }
        } catch (Exception e) {
            log.warn("Ollama health check failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * List available models.
     */
    public JsonNode listModels() throws IOException {
        Request request = new Request.Builder()
                .url(baseUrl + "/api/tags")
                .get()
                .build();
        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Failed to list models: " + response.code());
            }
            return objectMapper.readTree(response.body().string());
        }
    }
}
