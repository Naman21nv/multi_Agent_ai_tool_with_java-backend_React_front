package com.aiagent.controller;

import com.aiagent.model.ChatMessage;
import com.aiagent.model.Conversation;
import com.aiagent.service.ConversationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST controller for conversation history management.
 * Provides CRUD operations and dashboard statistics.
 */
@RestController
@RequestMapping("/api/conversations")
public class ConversationController {

    private final ConversationService conversationService;

    public ConversationController(ConversationService conversationService) {
        this.conversationService = conversationService;
    }

    @GetMapping
    public ResponseEntity<List<Conversation>> getAllConversations() {
        return ResponseEntity.ok(conversationService.getAllConversations());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Conversation> getConversation(@PathVariable String id) {
        return ResponseEntity.ok(conversationService.getConversation(id));
    }

    @GetMapping("/{id}/messages")
    public ResponseEntity<List<ChatMessage>> getMessages(@PathVariable String id) {
        return ResponseEntity.ok(conversationService.getMessages(id));
    }

    @PostMapping
    public ResponseEntity<Conversation> createConversation(@RequestBody Map<String, String> body) {
        String title = body.getOrDefault("title", "New Conversation");
        return ResponseEntity.ok(conversationService.createConversation(title));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteConversation(@PathVariable String id) {
        conversationService.deleteConversation(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    public ResponseEntity<List<Conversation>> searchConversations(@RequestParam String q) {
        return ResponseEntity.ok(conversationService.searchConversations(q));
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        return ResponseEntity.ok(conversationService.getStats());
    }
}
