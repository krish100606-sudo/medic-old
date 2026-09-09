package com.example.Health_Data_Management.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "case_answers")
public class CaseAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medical_case_id", nullable = false)
    private MedicalCase medicalCase;

    @Column(name = "question_code", length = 100, nullable = false)
    private String questionCode;

    @Column(name = "question_text", length = 1000, nullable = false)
    private String questionText;

    @Column(name = "answer_text", length = 3000)
    private String answerText;

    @Enumerated(EnumType.STRING)
    @Column(name = "input_type", length = 20)
    private InputType inputType = InputType.TEXT;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public CaseAnswer() {
    }

    public CaseAnswer(MedicalCase medicalCase, String questionCode, String questionText, String answerText, InputType inputType) {
        this.medicalCase = medicalCase;
        this.questionCode = questionCode;
        this.questionText = questionText;
        this.answerText = answerText;
        this.inputType = inputType != null ? inputType : InputType.TEXT;
        this.createdAt = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public MedicalCase getMedicalCase() {
        return medicalCase;
    }

    public void setMedicalCase(MedicalCase medicalCase) {
        this.medicalCase = medicalCase;
    }

    public String getQuestionCode() {
        return questionCode;
    }

    public void setQuestionCode(String questionCode) {
        this.questionCode = questionCode;
    }

    public String getQuestionText() {
        return questionText;
    }

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    public String getAnswerText() {
        return answerText;
    }

    public void setAnswerText(String answerText) {
        this.answerText = answerText;
    }

    public InputType getInputType() {
        return inputType;
    }

    public void setInputType(InputType inputType) {
        this.inputType = inputType;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
