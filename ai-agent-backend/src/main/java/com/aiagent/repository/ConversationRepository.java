package com.aiagent.repository;

import com.aiagent.model.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, String> {

    List<Conversation> findAllByOrderByUpdatedAtDesc();

    List<Conversation> findByStatus(Conversation.ConversationStatus status);

    List<Conversation> findByTitleContainingIgnoreCaseOrderByUpdatedAtDesc(String keyword);
}
