package com.aiagent.service;

import com.aiagent.model.ChatMessage;
import com.aiagent.model.Conversation;
import com.aiagent.repository.ChatMessageRepository;
import com.aiagent.repository.ConversationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service for managing conversations and chat message persistence.
 */
@Service
@Transactional
public class ConversationService {

    private static final Logger log = LoggerFactory.getLogger(ConversationService.class);

    private final ConversationRepository conversationRepository;
    private final ChatMessageRepository chatMessageRepository;

    public ConversationService(ConversationRepository conversationRepository,
                               ChatMessageRepository chatMessageRepository) {
        this.conversationRepository = conversationRepository;
        this.chatMessageRepository = chatMessageRepository;
    }

    public Conversation createConversation(String title) {
        Conversation conversation = new Conversation(title);
        conversation = conversationRepository.save(conversation);
        log.info("Created conversation: {} ({})", title, conversation.getId());
        return conversation;
    }

    public Conversation getConversation(String id) {
        return conversationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Conversation not found: " + id));
    }

    public List<Conversation> getAllConversations() {
        return conversationRepository.findAllByOrderByUpdatedAtDesc();
    }

    public List<Conversation> searchConversations(String keyword) {
        return conversationRepository.findByTitleContainingIgnoreCaseOrderByUpdatedAtDesc(keyword);
    }

    public void deleteConversation(String id) {
        conversationRepository.deleteById(id);
        log.info("Deleted conversation: {}", id);
    }

    public ChatMessage addMessage(String conversationId, String content, ChatMessage.MessageRole role) {
        Conversation conversation = getConversation(conversationId);

        ChatMessage message = new ChatMessage(content, role);
        conversation.addMessage(message);
        conversationRepository.save(conversation);

        return message;
    }

    public List<ChatMessage> getMessages(String conversationId) {
        return chatMessageRepository.findByConversationIdOrderByTimestampAsc(conversationId);
    }

    /**
     * Get dashboard statistics.
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalConversations", conversationRepository.count());
        stats.put("totalMessages", chatMessageRepository.countTotalMessages());
        stats.put("totalAgentResponses", chatMessageRepository.countAssistantMessages());

        List<Conversation> recent = conversationRepository.findAllByOrderByUpdatedAtDesc();
        stats.put("recentConversations", recent.stream().limit(5).toList());

        return stats;
    }
}
