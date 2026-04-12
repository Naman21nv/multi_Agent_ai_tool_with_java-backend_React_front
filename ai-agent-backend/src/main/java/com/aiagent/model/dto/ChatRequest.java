package com.aiagent.model.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for sending a chat message to the agent.
 */
public class ChatRequest {

    @NotBlank(message = "Message cannot be blank")
    private String message;

    private String conversationId;

    public ChatRequest() {}

    public ChatRequest(String message) {
        this.message = message;
    }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getConversationId() { return conversationId; }
    public void setConversationId(String conversationId) { this.conversationId = conversationId; }
}
