package com.example.Health_Data_Management.service;

public class DrugInteraction {
    private String drugA;
    private String drugB;
    private String severity; // CRITICAL, HIGH, MODERATE, MILD
    private String effect;
    private String clinicalRecommendation;

    public DrugInteraction() {}

    public DrugInteraction(String drugA, String drugB, String severity, String effect, String clinicalRecommendation) {
        this.drugA = drugA;
        this.drugB = drugB;
        this.severity = severity;
        this.effect = effect;
        this.clinicalRecommendation = clinicalRecommendation;
    }

    public String getDrugA() { return drugA; }
    public void setDrugA(String drugA) { this.drugA = drugA; }

    public String getDrugB() { return drugB; }
    public void setDrugB(String drugB) { this.drugB = drugB; }

    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }

    public String getEffect() { return effect; }
    public void setEffect(String effect) { this.effect = effect; }

    public String getClinicalRecommendation() { return clinicalRecommendation; }
    public void setClinicalRecommendation(String clinicalRecommendation) { this.clinicalRecommendation = clinicalRecommendation; }
}
