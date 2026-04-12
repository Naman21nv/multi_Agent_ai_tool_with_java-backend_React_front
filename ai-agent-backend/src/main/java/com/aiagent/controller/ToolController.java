package com.aiagent.controller;

import com.aiagent.service.OllamaService;
import com.aiagent.service.ToolExecutorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST controller for tool information and system health.
 */
@RestController
@RequestMapping("/api")
public class ToolController {

    private final ToolExecutorService toolExecutorService;
    private final OllamaService ollamaService;

    public ToolController(ToolExecutorService toolExecutorService, OllamaService ollamaService) {
        this.toolExecutorService = toolExecutorService;
        this.ollamaService = ollamaService;
    }

    /**
     * List all available agent tools with descriptions.
     */
    @GetMapping("/tools")
    public ResponseEntity<List<Map<String, String>>> getTools() {
        return ResponseEntity.ok(toolExecutorService.getToolInfoList());
    }

    /**
     * System health check — verifies Ollama connectivity and reports status.
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("ollamaConnected", ollamaService.isHealthy());

        try {
            health.put("availableModels", ollamaService.listModels());
        } catch (Exception e) {
            health.put("availableModels", "Unable to fetch models: " + e.getMessage());
        }

        health.put("registeredTools", toolExecutorService.getToolInfoList());
        return ResponseEntity.ok(health);
    }
}
