package com.aiagent.model.dto;

import com.aiagent.model.AgentTrace;
import java.util.List;

/**
 * Response DTO containing the agent's final answer plus execution trace.
 */
public class ChatResponse {

    private String conversationId;
    private String messageId;
    private String content;
    private long processingTimeMs;
    private List<AgentTrace> traces;
    private boolean success;
    private String error;

    public ChatResponse() {
        this.success = true;
    }

    public static ChatResponse success(String content, String conversationId, List<AgentTrace> traces, long processingTimeMs) {
        ChatResponse response = new ChatResponse();
        response.setContent(content);
        response.setConversationId(conversationId);
        response.setTraces(traces);
        response.setProcessingTimeMs(processingTimeMs);
        response.setSuccess(true);
        return response;
    }

    public static ChatResponse error(String errorMessage) {
        ChatResponse response = new ChatResponse();
        response.setSuccess(false);
        response.setError(errorMessage);
        return response;
    }

    // Getters and Setters
    public String getConversationId() { return conversationId; }
    public void setConversationId(String conversationId) { this.conversationId = conversationId; }

    public String getMessageId() { return messageId; }
    public void setMessageId(String messageId) { this.messageId = messageId; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public long getProcessingTimeMs() { return processingTimeMs; }
    public void setProcessingTimeMs(long processingTimeMs) { this.processingTimeMs = processingTimeMs; }

    public List<AgentTrace> getTraces() { return traces; }
    public void setTraces(List<AgentTrace> traces) { this.traces = traces; }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getError() { return error; }
    public void setError(String error) { this.error = error; }
}
