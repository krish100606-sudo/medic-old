package com.example.Health_Data_Management.dto;

import java.util.ArrayList;
import java.util.List;

public class SimilarCaseDto {

    private Long caseId;
    private String caseNumber;
    private Integer patientAge;
    private String patientGender;
    private String symptoms;
    private List<String> symptomTags = new ArrayList<>();
    private String diagnosis;
    private String treatment;
    private String vitals;
    private String outcome;
    private String doctorNotes;
    private int similarityScore; // 0 to 100
    private String matchConfidence; // "HIGH", "MODERATE", "LOW"
    private List<String> matchReasons = new ArrayList<>();

    public SimilarCaseDto() {
    }

    public Long getCaseId() {
        return caseId;
    }

    public void setCaseId(Long caseId) {
        this.caseId = caseId;
    }

    public String getCaseNumber() {
        return caseNumber;
    }

    public void setCaseNumber(String caseNumber) {
        this.caseNumber = caseNumber;
    }

    public Integer getPatientAge() {
        return patientAge;
    }

    public void setPatientAge(Integer patientAge) {
        this.patientAge = patientAge;
    }

    public String getPatientGender() {
        return patientGender;
    }

    public void setPatientGender(String patientGender) {
        this.patientGender = patientGender;
    }

    public String getSymptoms() {
        return symptoms;
    }

    public void setSymptoms(String symptoms) {
        this.symptoms = symptoms;
    }

    public List<String> getSymptomTags() {
        return symptomTags;
    }

    public void setSymptomTags(List<String> symptomTags) {
        this.symptomTags = symptomTags != null ? symptomTags : new ArrayList<>();
    }

    public String getDiagnosis() {
        return diagnosis;
    }

    public void setDiagnosis(String diagnosis) {
        this.diagnosis = diagnosis;
    }

    public String getTreatment() {
        return treatment;
    }

    public void setTreatment(String treatment) {
        this.treatment = treatment;
    }

    public String getVitals() {
        return vitals;
    }

    public void setVitals(String vitals) {
        this.vitals = vitals;
    }

    public String getOutcome() {
        return outcome;
    }

    public void setOutcome(String outcome) {
        this.outcome = outcome;
    }

    public String getDoctorNotes() {
        return doctorNotes;
    }

    public void setDoctorNotes(String doctorNotes) {
        this.doctorNotes = doctorNotes;
    }

    public int getSimilarityScore() {
        return similarityScore;
    }

    public void setSimilarityScore(int similarityScore) {
        this.similarityScore = similarityScore;
    }

    public String getMatchConfidence() {
        return matchConfidence;
    }

    public void setMatchConfidence(String matchConfidence) {
        this.matchConfidence = matchConfidence;
    }

    public List<String> getMatchReasons() {
        return matchReasons;
    }

    public void setMatchReasons(List<String> matchReasons) {
        this.matchReasons = matchReasons != null ? matchReasons : new ArrayList<>();
    }
}
