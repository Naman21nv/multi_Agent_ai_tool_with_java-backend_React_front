package com.aiagent.model;

import java.time.LocalDateTime;

/**
 * Represents a tool call made by the agent during execution.
 * Tracks tool usage statistics for the dashboard.
 */
public class ToolCall {

    private String id;
    private String toolName;
    private String input;
    private String output;
    private long durationMs;
    private boolean success;
    private LocalDateTime timestamp;
    private String conversationId;

    public ToolCall() {
        this.timestamp = LocalDateTime.now();
    }

    public ToolCall(String toolName, String input) {
        this();
        this.toolName = toolName;
        this.input = input;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getToolName() { return toolName; }
    public void setToolName(String toolName) { this.toolName = toolName; }

    public String getInput() { return input; }
    public void setInput(String input) { this.input = input; }

    public String getOutput() { return output; }
    public void setOutput(String output) { this.output = output; }

    public long getDurationMs() { return durationMs; }
    public void setDurationMs(long durationMs) { this.durationMs = durationMs; }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public String getConversationId() { return conversationId; }
    public void setConversationId(String conversationId) { this.conversationId = conversationId; }
}
