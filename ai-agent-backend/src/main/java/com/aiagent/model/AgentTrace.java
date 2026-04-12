package com.aiagent.model;

import java.time.LocalDateTime;

/**
 * Represents a single step in the agent's ReAct execution trace.
 * Used to show the Think → Act → Observe loop in the UI.
 */
public class AgentTrace {

    private String id;
    private TraceType type;
    private String content;
    private String toolName;
    private String toolInput;
    private String toolOutput;
    private long durationMs;
    private LocalDateTime timestamp;
    private TraceStatus status;

    public enum TraceType {
        THINKING, TOOL_CALL, TOOL_RESULT, FINAL_ANSWER, ERROR
    }

    public enum TraceStatus {
        PENDING, RUNNING, COMPLETED, FAILED
    }

    public AgentTrace() {
        this.timestamp = LocalDateTime.now();
        this.status = TraceStatus.PENDING;
    }

    public AgentTrace(TraceType type, String content) {
        this();
        this.type = type;
        this.content = content;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public TraceType getType() { return type; }
    public void setType(TraceType type) { this.type = type; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getToolName() { return toolName; }
    public void setToolName(String toolName) { this.toolName = toolName; }

    public String getToolInput() { return toolInput; }
    public void setToolInput(String toolInput) { this.toolInput = toolInput; }

    public String getToolOutput() { return toolOutput; }
    public void setToolOutput(String toolOutput) { this.toolOutput = toolOutput; }

    public long getDurationMs() { return durationMs; }
    public void setDurationMs(long durationMs) { this.durationMs = durationMs; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public TraceStatus getStatus() { return status; }
    public void setStatus(TraceStatus status) { this.status = status; }
}
