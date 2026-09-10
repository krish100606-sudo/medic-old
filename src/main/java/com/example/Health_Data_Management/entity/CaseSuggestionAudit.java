package com.example.Health_Data_Management.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "case_suggestion_audits")
public class CaseSuggestionAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "medical_case_id", nullable = false)
    private Long medicalCaseId;

    @Column(name = "case_number", length = 50)
    private String caseNumber;

    @Column(name = "doctor_id")
    private Long doctorId;

    @Column(name = "doctor_name", length = 150)
    private String doctorName;

    @Column(name = "action", length = 30, nullable = false)
    private String action; // ACCEPTED, EDITED, REJECTED

    @Column(name = "ai_diagnosis_suggestion", length = 500)
    private String aiDiagnosisSuggestion;

    @Column(name = "ai_reasoning", columnDefinition = "TEXT")
    private String aiReasoning;

    @Column(name = "ai_treatment_options_json", columnDefinition = "TEXT")
    private String aiTreatmentOptionsJson;

    @Column(name = "ai_confidence", length = 50)
    private String aiConfidence;

    @Column(name = "final_diagnosis", length = 500)
    private String finalDiagnosis;

    @Column(name = "final_treatment", columnDefinition = "TEXT")
    private String finalTreatment;

    @Column(name = "doctor_notes", columnDefinition = "TEXT")
    private String doctorNotes;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public CaseSuggestionAudit() {
        this.createdAt = LocalDateTime.now();
    }

    public CaseSuggestionAudit(Long medicalCaseId, String caseNumber, Long doctorId, String doctorName,
                               String action, String aiDiagnosisSuggestion, String aiReasoning,
                               String aiTreatmentOptionsJson, String aiConfidence,
                               String finalDiagnosis, String finalTreatment, String doctorNotes) {
        this.medicalCaseId = medicalCaseId;
        this.caseNumber = caseNumber;
        this.doctorId = doctorId;
        this.doctorName = doctorName;
        this.action = action;
        this.aiDiagnosisSuggestion = aiDiagnosisSuggestion;
        this.aiReasoning = aiReasoning;
        this.aiTreatmentOptionsJson = aiTreatmentOptionsJson;
        this.aiConfidence = aiConfidence;
        this.finalDiagnosis = finalDiagnosis;
        this.finalTreatment = finalTreatment;
        this.doctorNotes = doctorNotes;
        this.createdAt = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getMedicalCaseId() {
        return medicalCaseId;
    }

    public void setMedicalCaseId(Long medicalCaseId) {
        this.medicalCaseId = medicalCaseId;
    }

    public String getCaseNumber() {
        return caseNumber;
    }

    public void setCaseNumber(String caseNumber) {
        this.caseNumber = caseNumber;
    }

    public Long getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(Long doctorId) {
        this.doctorId = doctorId;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getAiDiagnosisSuggestion() {
        return aiDiagnosisSuggestion;
    }

    public void setAiDiagnosisSuggestion(String aiDiagnosisSuggestion) {
        this.aiDiagnosisSuggestion = aiDiagnosisSuggestion;
    }

    public String getAiReasoning() {
        return aiReasoning;
    }

    public void setAiReasoning(String aiReasoning) {
        this.aiReasoning = aiReasoning;
    }

    public String getAiTreatmentOptionsJson() {
        return aiTreatmentOptionsJson;
    }

    public void setAiTreatmentOptionsJson(String aiTreatmentOptionsJson) {
        this.aiTreatmentOptionsJson = aiTreatmentOptionsJson;
    }

    public String getAiConfidence() {
        return aiConfidence;
    }

    public void setAiConfidence(String aiConfidence) {
        this.aiConfidence = aiConfidence;
    }

    public String getFinalDiagnosis() {
        return finalDiagnosis;
    }

    public void setFinalDiagnosis(String finalDiagnosis) {
        this.finalDiagnosis = finalDiagnosis;
    }

    public String getFinalTreatment() {
        return finalTreatment;
    }

    public void setFinalTreatment(String finalTreatment) {
        this.finalTreatment = finalTreatment;
    }

    public String getDoctorNotes() {
        return doctorNotes;
    }

    public void setDoctorNotes(String doctorNotes) {
        this.doctorNotes = doctorNotes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
