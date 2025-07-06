package com.itech.itech_backend.controller;

import com.itech.itech_backend.model.ChatbotMessage;
import com.itech.itech_backend.repository.ChatbotMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/chatbot")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "http://localhost:3000")
public class ChatbotAdminController {

    private final ChatbotMessageRepository chatbotMessageRepository;

    @GetMapping("/analytics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getChatbotAnalytics() {
        try {
            Map<String, Object> analytics = new HashMap<>();
            
            // Total conversations
            long totalMessages = chatbotMessageRepository.count();
            analytics.put("totalMessages", totalMessages);
            
            // Messages in the last 24 hours
            LocalDateTime yesterday = LocalDateTime.now().minusDays(1);
            List<ChatbotMessage> recentMessages = chatbotMessageRepository
                    .findByCreatedAtAfterOrderByCreatedAtDesc(yesterday);
            analytics.put("messagesLast24Hours", recentMessages.size());
            
            // Messages in the last week
            LocalDateTime lastWeek = LocalDateTime.now().minusDays(7);
            List<ChatbotMessage> weeklyMessages = chatbotMessageRepository
                    .findByCreatedAtAfterOrderByCreatedAtDesc(lastWeek);
            analytics.put("messagesLastWeek", weeklyMessages.size());
            
            // Unique sessions in the last week
            long uniqueSessions = weeklyMessages.stream()
                    .map(ChatbotMessage::getSessionId)
                    .distinct()
                    .count();
            analytics.put("uniqueSessionsLastWeek", uniqueSessions);
            
            return ResponseEntity.ok(analytics);
            
        } catch (Exception e) {
            log.error("Error fetching chatbot analytics: {}", e.getMessage(), e);
            return ResponseEntity.ok(Map.of("error", "Unable to fetch analytics"));
        }
    }

    @GetMapping("/conversations")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<ChatbotMessage>> getAllConversations(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        try {
            Sort sort = sortDir.equalsIgnoreCase("desc") 
                ? Sort.by(sortBy).descending() 
                : Sort.by(sortBy).ascending();
            
            Pageable pageable = PageRequest.of(page, size, sort);
            Page<ChatbotMessage> conversations = chatbotMessageRepository.findAll(pageable);
            
            return ResponseEntity.ok(conversations);
            
        } catch (Exception e) {
            log.error("Error fetching conversations: {}", e.getMessage(), e);
            return ResponseEntity.ok(Page.empty());
        }
    }

    @GetMapping("/conversation/{sessionId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ChatbotMessage>> getConversationBySession(@PathVariable String sessionId) {
        try {
            List<ChatbotMessage> conversation = chatbotMessageRepository
                    .findBySessionIdOrderByCreatedAtAsc(sessionId);
            return ResponseEntity.ok(conversation);
        } catch (Exception e) {
            log.error("Error fetching conversation: {}", e.getMessage(), e);
            return ResponseEntity.ok(List.of());
        }
    }

    @DeleteMapping("/conversation/{sessionId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteConversation(@PathVariable String sessionId) {
        try {
            List<ChatbotMessage> messages = chatbotMessageRepository
                    .findBySessionIdOrderByCreatedAtAsc(sessionId);
            chatbotMessageRepository.deleteAll(messages);
            return ResponseEntity.ok("Conversation deleted successfully");
        } catch (Exception e) {
            log.error("Error deleting conversation: {}", e.getMessage(), e);
            return ResponseEntity.ok("Error deleting conversation");
        }
    }

    @GetMapping("/recent-queries")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<String>> getRecentQueries(@RequestParam(defaultValue = "10") int limit) {
        try {
            List<ChatbotMessage> recentMessages = chatbotMessageRepository
                    .findByCreatedAtAfterOrderByCreatedAtDesc(LocalDateTime.now().minusDays(1));
            
            List<String> queries = recentMessages.stream()
                    .map(ChatbotMessage::getUserMessage)
                    .distinct()
                    .limit(limit)
                    .toList();
            
            return ResponseEntity.ok(queries);
        } catch (Exception e) {
            log.error("Error fetching recent queries: {}", e.getMessage(), e);
            return ResponseEntity.ok(List.of());
        }
    }
}
