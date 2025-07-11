package com.itech.itech_backend.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatbotMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String sessionId; // To track conversation sessions
    
    private String userMessage;
    
    @Column(columnDefinition = "TEXT")
    private String botResponse;
    
    private String userIp; // Optional: to track user sessions
    
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
    
    // Optional: link to user if they are logged in
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = true)
    private User user;
}
