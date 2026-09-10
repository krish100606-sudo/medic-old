package com.example.Health_Data_Management.dto;

import java.util.ArrayList;
import java.util.List;

public class AiSuggestionResponse {

    private Long caseId;
    private String diagnosis_suggestion;
    private String reasoning;
    private List<String> treatment_options = new ArrayList<>();
    private String confidence;
    private String disclaimer = "AI-generated suggestion based on multi-patient case history — not a diagnosis. Doctor must verify before finalizing.";
    private int similarCasesCount = 0;
    private String formattedHtml;

    public AiSuggestionResponse() {}

    public Long getCaseId() { return caseId; }
    public void setCaseId(Long caseId) { this.caseId = caseId; }

    public String getDiagnosis_suggestion() { return diagnosis_suggestion; }
    public void setDiagnosis_suggestion(String diagnosis_suggestion) { this.diagnosis_suggestion = diagnosis_suggestion; }

    public String getReasoning() { return reasoning; }
    public void setReasoning(String reasoning) { this.reasoning = reasoning; }

    public List<String> getTreatment_options() { return treatment_options; }
    public void setTreatment_options(List<String> treatment_options) { this.treatment_options = treatment_options != null ? treatment_options : new ArrayList<>(); }

    public String getConfidence() { return confidence; }
    public void setConfidence(String confidence) { this.confidence = confidence; }

    public String getDisclaimer() { return disclaimer; }
    public void setDisclaimer(String disclaimer) { this.disclaimer = disclaimer; }

    public int getSimilarCasesCount() { return similarCasesCount; }
    public void setSimilarCasesCount(int similarCasesCount) { this.similarCasesCount = similarCasesCount; }

    public String getFormattedHtml() { return formattedHtml; }
    public void setFormattedHtml(String formattedHtml) { this.formattedHtml = formattedHtml; }
}
