package com.aiagent.repository;

import com.aiagent.model.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, String> {

    List<ChatMessage> findByConversationIdOrderByTimestampAsc(String conversationId);

    @Query("SELECT COUNT(m) FROM ChatMessage m")
    long countTotalMessages();

    @Query("SELECT COUNT(m) FROM ChatMessage m WHERE m.role = 'ASSISTANT'")
    long countAssistantMessages();
}
