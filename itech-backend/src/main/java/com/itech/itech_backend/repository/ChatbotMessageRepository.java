package com.itech.itech_backend.repository;

import com.itech.itech_backend.model.ChatbotMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ChatbotMessageRepository extends JpaRepository<ChatbotMessage, Long> {
    
    // Find messages by session ID for conversation history
    List<ChatbotMessage> findBySessionIdOrderByCreatedAtAsc(String sessionId);
    
    // Find recent messages for a session (last 10 messages)
    @Query("SELECT cm FROM ChatbotMessage cm WHERE cm.sessionId = :sessionId ORDER BY cm.createdAt DESC")
    List<ChatbotMessage> findRecentMessagesBySessionId(@Param("sessionId") String sessionId);
    
    // Find messages by user if logged in
    List<ChatbotMessage> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    // Find messages created after a specific time
    List<ChatbotMessage> findByCreatedAtAfterOrderByCreatedAtDesc(LocalDateTime dateTime);
    
    // Count messages by session
    long countBySessionId(String sessionId);
}
