package com.example.Health_Data_Management.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "medical_documents")
public class MedicalDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medical_case_id")
    private MedicalCase medicalCase;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @Column(name = "file_name", length = 255, nullable = false)
    private String fileName;

    @Column(name = "original_file_name", length = 255)
    private String originalFileName;

    @Column(name = "file_type", length = 100)
    private String fileType;

    @Enumerated(EnumType.STRING)
    @Column(name = "document_type", length = 50)
    private DocumentType documentType = DocumentType.PRESCRIPTION;

    @Column(name = "extracted_text", length = 5000)
    private String extractedText;

    @Column(name = "extracted_diagnosis", length = 1000)
    private String extractedDiagnosis;

    @Column(name = "extracted_medications", length = 1000)
    private String extractedMedications;

    @Column(name = "extracted_investigations", length = 1000)
    private String extractedInvestigations;

    @Enumerated(EnumType.STRING)
    @Column(name = "ocr_status", length = 30)
    private OcrStatus ocrStatus = OcrStatus.PENDING;

    @Column(name = "uploaded_at", nullable = false)
    private LocalDateTime uploadedAt;

    public MedicalDocument() {
    }

    public MedicalDocument(Patient patient, MedicalCase medicalCase, String fileName, String originalFileName, String fileType, DocumentType documentType) {
        this.patient = patient;
        this.medicalCase = medicalCase;
        this.fileName = fileName;
        this.originalFileName = originalFileName;
        this.fileType = fileType;
        this.documentType = documentType != null ? documentType : DocumentType.PRESCRIPTION;
        this.ocrStatus = OcrStatus.PENDING;
        this.uploadedAt = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        if (uploadedAt == null) {
            uploadedAt = LocalDateTime.now();
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

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getOriginalFileName() {
        return originalFileName;
    }

    public void setOriginalFileName(String originalFileName) {
        this.originalFileName = originalFileName;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public DocumentType getDocumentType() {
        return documentType;
    }

    public void setDocumentType(DocumentType documentType) {
        this.documentType = documentType;
    }

    public String getExtractedText() {
        return extractedText;
    }

    public void setExtractedText(String extractedText) {
        this.extractedText = extractedText;
    }

    public String getExtractedDiagnosis() {
        return extractedDiagnosis;
    }

    public void setExtractedDiagnosis(String extractedDiagnosis) {
        this.extractedDiagnosis = extractedDiagnosis;
    }

    public String getExtractedMedications() {
        return extractedMedications;
    }

    public void setExtractedMedications(String extractedMedications) {
        this.extractedMedications = extractedMedications;
    }

    public String getExtractedInvestigations() {
        return extractedInvestigations;
    }

    public void setExtractedInvestigations(String extractedInvestigations) {
        this.extractedInvestigations = extractedInvestigations;
    }

    public OcrStatus getOcrStatus() {
        return ocrStatus;
    }

    public void setOcrStatus(OcrStatus ocrStatus) {
        this.ocrStatus = ocrStatus;
    }

    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(LocalDateTime uploadedAt) {
        this.uploadedAt = uploadedAt;
    }
}
