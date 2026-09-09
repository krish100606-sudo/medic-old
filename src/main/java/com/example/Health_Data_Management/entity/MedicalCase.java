package com.example.Health_Data_Management.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "medical_cases")
public class MedicalCase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "case_number", unique = true, nullable = false, length = 50)
    private String caseNumber;

    @Column(name = "token_number")
    private Integer tokenNumber;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "doctor_id")
    private Doctor doctor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CaseStatus status = CaseStatus.DRAFT;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CasePriority priority = CasePriority.NORMAL;

    @Column(name = "priority_reason", columnDefinition = "TEXT")
    private String priorityReason;

    @Column(name = "red_flags_detected")
    private boolean redFlagsDetected = false;

    @Column(name = "red_flags_details", columnDefinition = "TEXT")
    private String redFlagsDetails;

    // ---------------- Clinical Intake Fields ----------------
    @Column(name = "chief_complaint", length = 1000)
    private String chiefComplaint;

    @Column(name = "patient_statement", columnDefinition = "TEXT")
    private String patientStatement;

    @Column(length = 255)
    private String onset;

    @Column(length = 255)
    private String location;

    @Column(length = 255)
    private String severity;

    @Column(name = "associated_symptoms", columnDefinition = "TEXT")
    private String associatedSymptoms;

    @Column(name = "past_medical_history", columnDefinition = "TEXT")
    private String pastMedicalHistory;

    @Column(name = "surgical_history", columnDefinition = "TEXT")
    private String surgicalHistory;

    @Column(name = "current_medication", columnDefinition = "TEXT")
    private String currentMedication;

    @Column(length = 1000)
    private String allergies;

    @Column(name = "family_history", columnDefinition = "TEXT")
    private String familyHistory;

    @Column(name = "personal_history", columnDefinition = "TEXT")
    private String personalHistory;

    @Column(name = "investigations", columnDefinition = "TEXT")
    private String investigations;

    @Column(name = "medical_timeline", columnDefinition = "TEXT")
    private String medicalTimeline;

    @Column(name = "structured_summary", columnDefinition = "TEXT")
    private String structuredSummary;

    // ---------------- AYUSH Dashavidha & Clinical Additions ----------------
    @Column(name = "dashavidha_assessment", columnDefinition = "TEXT")
    private String dashavidhaAssessment;

    @Column(name = "prakriti_type", length = 50)
    private String prakritiType;

    @Column(name = "dosha_imbalance", length = 100)
    private String doshaImbalance;

    @Column(name = "drug_interactions_json", columnDefinition = "TEXT")
    private String drugInteractionsJson;

    @Column(name = "digital_signature", columnDefinition = "TEXT")
    private String digitalSignature;

    // ---------------- Doctor Verification Fields ----------------
    @Column(name = "is_doctor_edited")
    private boolean isDoctorEdited = false;

    @Column(name = "doctor_clinical_notes", columnDefinition = "TEXT")
    private String doctorClinicalNotes;

    @Column(name = "verified_by_doctor", length = 150)
    private String verifiedByDoctor;

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "medicalCase", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CaseAnswer> answers = new ArrayList<>();

    @OneToMany(mappedBy = "medicalCase", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MedicalDocument> documents = new ArrayList<>();

    public MedicalCase() {
    }

    public MedicalCase(Patient patient) {
        this.patient = patient;
        this.status = CaseStatus.DRAFT;
        this.priority = CasePriority.NORMAL;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (caseNumber == null) {
            caseNumber = "CASE-" + System.currentTimeMillis() % 1000000;
        }
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCaseNumber() {
        return caseNumber;
    }

    public void setCaseNumber(String caseNumber) {
        this.caseNumber = caseNumber;
    }

    public Integer getTokenNumber() {
        return tokenNumber;
    }

    public void setTokenNumber(Integer tokenNumber) {
        this.tokenNumber = tokenNumber;
    }

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public Doctor getDoctor() {
        return doctor;
    }

    public void setDoctor(Doctor doctor) {
        this.doctor = doctor;
    }

    public CaseStatus getStatus() {
        return status;
    }

    public void setStatus(CaseStatus status) {
        this.status = status;
    }

    public CasePriority getPriority() {
        return priority;
    }

    public void setPriority(CasePriority priority) {
        this.priority = priority;
    }

    public String getPriorityReason() {
        return priorityReason;
    }

    public void setPriorityReason(String priorityReason) {
        this.priorityReason = priorityReason;
    }

    public boolean isRedFlagsDetected() {
        return redFlagsDetected;
    }

    public void setRedFlagsDetected(boolean redFlagsDetected) {
        this.redFlagsDetected = redFlagsDetected;
    }

    public String getRedFlagsDetails() {
        return redFlagsDetails;
    }

    public void setRedFlagsDetails(String redFlagsDetails) {
        this.redFlagsDetails = redFlagsDetails;
    }

    public String getChiefComplaint() {
        return chiefComplaint;
    }

    public void setChiefComplaint(String chiefComplaint) {
        this.chiefComplaint = chiefComplaint;
    }

    public String getPatientStatement() {
        return patientStatement;
    }

    public void setPatientStatement(String patientStatement) {
        this.patientStatement = patientStatement;
    }

    public String getOnset() {
        return onset;
    }

    public void setOnset(String onset) {
        this.onset = onset;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public String getAssociatedSymptoms() {
        return associatedSymptoms;
    }

    public void setAssociatedSymptoms(String associatedSymptoms) {
        this.associatedSymptoms = associatedSymptoms;
    }

    public String getPastMedicalHistory() {
        return pastMedicalHistory;
    }

    public void setPastMedicalHistory(String pastMedicalHistory) {
        this.pastMedicalHistory = pastMedicalHistory;
    }

    public String getSurgicalHistory() {
        return surgicalHistory;
    }

    public void setSurgicalHistory(String surgicalHistory) {
        this.surgicalHistory = surgicalHistory;
    }

    public String getCurrentMedication() {
        return currentMedication;
    }

    public void setCurrentMedication(String currentMedication) {
        this.currentMedication = currentMedication;
    }

    public String getAllergies() {
        return allergies;
    }

    public void setAllergies(String allergies) {
        this.allergies = allergies;
    }

    public String getFamilyHistory() {
        return familyHistory;
    }

    public void setFamilyHistory(String familyHistory) {
        this.familyHistory = familyHistory;
    }

    public String getPersonalHistory() {
        return personalHistory;
    }

    public void setPersonalHistory(String personalHistory) {
        this.personalHistory = personalHistory;
    }

    public String getInvestigations() {
        return investigations;
    }

    public void setInvestigations(String investigations) {
        this.investigations = investigations;
    }

    public String getMedicalTimeline() {
        return medicalTimeline;
    }

    public void setMedicalTimeline(String medicalTimeline) {
        this.medicalTimeline = medicalTimeline;
    }

    public String getStructuredSummary() {
        return structuredSummary;
    }

    public void setStructuredSummary(String structuredSummary) {
        this.structuredSummary = structuredSummary;
    }

    public boolean isDoctorEdited() {
        return isDoctorEdited;
    }

    public void setDoctorEdited(boolean doctorEdited) {
        isDoctorEdited = doctorEdited;
    }

    public String getDoctorClinicalNotes() {
        return doctorClinicalNotes;
    }

    public void setDoctorClinicalNotes(String doctorClinicalNotes) {
        this.doctorClinicalNotes = doctorClinicalNotes;
    }

    public String getVerifiedByDoctor() {
        return verifiedByDoctor;
    }

    public void setVerifiedByDoctor(String verifiedByDoctor) {
        this.verifiedByDoctor = verifiedByDoctor;
    }

    public LocalDateTime getVerifiedAt() {
        return verifiedAt;
    }

    public void setVerifiedAt(LocalDateTime verifiedAt) {
        this.verifiedAt = verifiedAt;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(LocalDateTime submittedAt) {
        this.submittedAt = submittedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<CaseAnswer> getAnswers() {
        return answers;
    }

    public void setAnswers(List<CaseAnswer> answers) {
        this.answers = answers;
    }

    public List<MedicalDocument> getDocuments() {
        return documents;
    }

    public void setDocuments(List<MedicalDocument> documents) {
        this.documents = documents;
    }

    public String getDashavidhaAssessment() {
        return dashavidhaAssessment;
    }

    public void setDashavidhaAssessment(String dashavidhaAssessment) {
        this.dashavidhaAssessment = dashavidhaAssessment;
    }

    public String getPrakritiType() {
        return prakritiType;
    }

    public void setPrakritiType(String prakritiType) {
        this.prakritiType = prakritiType;
    }

    public String getDoshaImbalance() {
        return doshaImbalance;
    }

    public void setDoshaImbalance(String doshaImbalance) {
        this.doshaImbalance = doshaImbalance;
    }

    public String getDrugInteractionsJson() {
        return drugInteractionsJson;
    }

    public void setDrugInteractionsJson(String drugInteractionsJson) {
        this.drugInteractionsJson = drugInteractionsJson;
    }

    public String getDigitalSignature() {
        return digitalSignature;
    }

    public void setDigitalSignature(String digitalSignature) {
        this.digitalSignature = digitalSignature;
    }

    public List<String[]> getTimelineList() {
        List<String[]> list = new ArrayList<>();
        if (medicalTimeline == null || medicalTimeline.isBlank()) {
            return list;
        }
        for (String line : medicalTimeline.split("\n")) {
            String trimmed = line.trim();
            if (trimmed.isEmpty()) continue;
            if (trimmed.contains("|")) {
                String[] parts = trimmed.split("\\|", 2);
                list.add(new String[]{parts[0].trim(), parts[1].trim()});
            } else {
                list.add(new String[]{"Event", trimmed});
            }
        }
        return list;
    }

    public String getFormattedCreatedAt() {
        return createdAt != null ? createdAt.toLocalDate().toString() : "";
    }
}
