package com.example.Health_Data_Management.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "conversation_messages")
public class ConversationMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medical_case_id", nullable = false)
    private MedicalCase medicalCase;

    @Column(nullable = false, length = 20)
    private String sender; // SYSTEM, PATIENT, DOCTOR

    @Column(name = "message_text", nullable = false, length = 3000)
    private String messageText;

    @Column(name = "message_type", length = 30)
    private String messageType; // QUESTION, ANSWER, FOLLOW_UP, SYSTEM_NOTE, RED_FLAG_ALERT

    @Column(name = "question_code", length = 100)
    private String questionCode;

    @Column(length = 20)
    private String language; // English, Hindi

    @Column(name = "input_method", length = 20)
    private String inputMethod; // VOICE, TOUCH, TEXT, AUTO

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public ConversationMessage() {
    }

    public ConversationMessage(MedicalCase medicalCase, String sender, String messageText,
                               String messageType, String questionCode, String language, String inputMethod) {
        this.medicalCase = medicalCase;
        this.sender = sender;
        this.messageText = messageText;
        this.messageType = messageType;
        this.questionCode = questionCode;
        this.language = language;
        this.inputMethod = inputMethod;
        this.createdAt = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public MedicalCase getMedicalCase() { return medicalCase; }
    public void setMedicalCase(MedicalCase medicalCase) { this.medicalCase = medicalCase; }

    public String getSender() { return sender; }
    public void setSender(String sender) { this.sender = sender; }

    public String getMessageText() { return messageText; }
    public void setMessageText(String messageText) { this.messageText = messageText; }

    public String getMessageType() { return messageType; }
    public void setMessageType(String messageType) { this.messageType = messageType; }

    public String getQuestionCode() { return questionCode; }
    public void setQuestionCode(String questionCode) { this.questionCode = questionCode; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public String getInputMethod() { return inputMethod; }
    public void setInputMethod(String inputMethod) { this.inputMethod = inputMethod; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getFormattedTime() {
        if (createdAt == null) return "";
        return String.format("%02d:%02d", createdAt.getHour(), createdAt.getMinute());
    }
}
