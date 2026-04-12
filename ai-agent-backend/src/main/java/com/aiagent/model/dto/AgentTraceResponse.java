package com.aiagent.model.dto;

import com.aiagent.model.AgentTrace;

/**
 * DTO sent over WebSocket to stream agent trace events in real-time.
 */
public class AgentTraceResponse {

    private String conversationId;
    private AgentTrace trace;
    private boolean isComplete;

    public AgentTraceResponse() {}

    public AgentTraceResponse(String conversationId, AgentTrace trace, boolean isComplete) {
        this.conversationId = conversationId;
        this.trace = trace;
        this.isComplete = isComplete;
    }

    public String getConversationId() { return conversationId; }
    public void setConversationId(String conversationId) { this.conversationId = conversationId; }

    public AgentTrace getTrace() { return trace; }
    public void setTrace(AgentTrace trace) { this.trace = trace; }

    public boolean isComplete() { return isComplete; }
    public void setComplete(boolean complete) { isComplete = complete; }
}
