package com.aiagent.service;

import com.aiagent.tool.AgentTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service responsible for discovering, managing, and executing agent tools.
 * All tools are auto-discovered via Spring's component scanning.
 * This replaces Python's tools_list pattern.
 */
@Service
public class ToolExecutorService {

    private static final Logger log = LoggerFactory.getLogger(ToolExecutorService.class);
    private final Map<String, AgentTool> toolRegistry;

    public ToolExecutorService(List<AgentTool> tools) {
        this.toolRegistry = tools.stream()
                .collect(Collectors.toMap(AgentTool::getName, t -> t));
        log.info("Registered {} agent tools: {}", toolRegistry.size(), toolRegistry.keySet());
    }

    /**
     * Execute a tool by name with the given arguments.
     */
    public String executeTool(String toolName, Map<String, String> arguments) {
        AgentTool tool = toolRegistry.get(toolName);
        if (tool == null) {
            return "Error: Unknown tool '" + toolName + "'. Available tools: " + toolRegistry.keySet();
        }

        try {
            log.info("Executing tool '{}' with args: {}", toolName, arguments);
            long start = System.currentTimeMillis();
            String result = tool.execute(arguments);
            long duration = System.currentTimeMillis() - start;
            log.info("Tool '{}' completed in {}ms", toolName, duration);
            return result;
        } catch (Exception e) {
            log.error("Tool '{}' failed", toolName, e);
            return "Error executing tool '" + toolName + "': " + e.getMessage();
        }
    }

    /**
     * Get tool definitions in the Ollama function-calling format.
     */
    public List<Map<String, Object>> getToolDefinitions() {
        List<Map<String, Object>> definitions = new ArrayList<>();

        for (AgentTool tool : toolRegistry.values()) {
            Map<String, Object> toolDef = new LinkedHashMap<>();
            toolDef.put("type", "function");

            Map<String, Object> function = new LinkedHashMap<>();
            function.put("name", tool.getName());
            function.put("description", tool.getDescription());
            function.put("parameters", tool.getParameterSchema());

            toolDef.put("function", function);
            definitions.add(toolDef);
        }

        return definitions;
    }

    /**
     * Get a list of all registered tool names and descriptions (for UI).
     */
    public List<Map<String, String>> getToolInfoList() {
        List<Map<String, String>> info = new ArrayList<>();
        for (AgentTool tool : toolRegistry.values()) {
            Map<String, String> item = new LinkedHashMap<>();
            item.put("name", tool.getName());
            item.put("description", tool.getDescription());
            info.add(item);
        }
        return info;
    }

    public boolean hasTool(String name) {
        return toolRegistry.containsKey(name);
    }
}
