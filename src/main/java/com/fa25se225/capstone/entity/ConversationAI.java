package com.fa25se225.capstone.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Entity
@Table(name = "Conversation_AI")
@AllArgsConstructor
@NoArgsConstructor
public class ConversationAI {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "message")
    private String message;

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    @Column(name = "aiResponse")
    private String aiResponse;

    @Column(name = "deleted", nullable = false)
    private Boolean deleted = false;

    @Column(name = "timestamp")
    private Instant timestamp;

    public ConversationAI(String message, User user, String aiResponse) {
        this.message = message;
        this.user = user;
        this.aiResponse = aiResponse;
        this.deleted = false;
        this.timestamp = Instant.now();
    }
}
