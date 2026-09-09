package com.example.Health_Data_Management.service;

import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class StructuredExtractionService {

    public static class StructuredClinicalData {
        private List<String> diagnoses = new ArrayList<>();
        private List<ExtractedMedication> medications = new ArrayList<>();
        private List<ExtractedLabValue> labValues = new ArrayList<>();
        private double confidenceScore = 0.85;

        public List<String> getDiagnoses() { return diagnoses; }
        public void setDiagnoses(List<String> diagnoses) { this.diagnoses = diagnoses; }

        public List<ExtractedMedication> getMedications() { return medications; }
        public void setMedications(List<ExtractedMedication> medications) { this.medications = medications; }

        public List<ExtractedLabValue> getLabValues() { return labValues; }
        public void setLabValues(List<ExtractedLabValue> labValues) { this.labValues = labValues; }

        public double getConfidenceScore() { return confidenceScore; }
        public void setConfidenceScore(double confidenceScore) { this.confidenceScore = confidenceScore; }
    }

    public static class ExtractedMedication {
        private String name;
        private String dosage;
        private String frequency;

        public ExtractedMedication(String name, String dosage, String frequency) {
            this.name = name;
            this.dosage = dosage;
            this.frequency = frequency;
        }

        public String getName() { return name; }
        public String getDosage() { return dosage; }
        public String getFrequency() { return frequency; }
    }

    public static class ExtractedLabValue {
        private String testName;
        private String value;
        private String unit;
        private String status; // NORMAL, HIGH, LOW

        public ExtractedLabValue(String testName, String value, String unit, String status) {
            this.testName = testName;
            this.value = value;
            this.unit = unit;
            this.status = status;
        }

        public String getTestName() { return testName; }
        public String getValue() { return value; }
        public String getUnit() { return unit; }
        public String getStatus() { return status; }
    }

    public StructuredClinicalData extractFromText(String rawText) {
        StructuredClinicalData data = new StructuredClinicalData();
        if (rawText == null || rawText.isBlank()) return data;

        String lower = rawText.toLowerCase();

        // Extract Common Indian Medications
        extractMedicationIfPresent(rawText, "Metformin", data);
        extractMedicationIfPresent(rawText, "Aspirin", data);
        extractMedicationIfPresent(rawText, "Atorvastatin", data);
        extractMedicationIfPresent(rawText, "Amlodipine", data);
        extractMedicationIfPresent(rawText, "Telmisartan", data);
        extractMedicationIfPresent(rawText, "Pantoprazole", data);
        extractMedicationIfPresent(rawText, "Paracetamol", data);
        extractMedicationIfPresent(rawText, "Clopidogrel", data);
        extractMedicationIfPresent(rawText, "Warfarin", data);
        extractMedicationIfPresent(rawText, "Ciprofloxacin", data);
        extractMedicationIfPresent(rawText, "Azithromycin", data);

        // Extract Lab Values
        if (lower.contains("fasting blood sugar") || lower.contains("fbs") || lower.contains("glucose")) {
            extractNumericLab(rawText, "Fasting Blood Sugar", "mg/dL", 70, 100, data);
        }
        if (lower.contains("hba1c")) {
            extractNumericLab(rawText, "HbA1c", "%", 4.0, 5.7, data);
        }
        if (lower.contains("hemoglobin") || lower.contains("hb")) {
            extractNumericLab(rawText, "Hemoglobin", "g/dL", 12.0, 16.0, data);
        }
        if (lower.contains("creatinine")) {
            extractNumericLab(rawText, "Serum Creatinine", "mg/dL", 0.7, 1.3, data);
        }
        if (lower.contains("cholesterol")) {
            extractNumericLab(rawText, "Total Cholesterol", "mg/dL", 125, 200, data);
        }

        // Extract Diagnoses
        if (lower.contains("type 2 diabetes") || lower.contains("diabetes mellitus") || lower.contains("t2dm")) {
            data.getDiagnoses().add("Type 2 Diabetes Mellitus");
        }
        if (lower.contains("hypertension") || lower.contains("htn") || lower.contains("high bp")) {
            data.getDiagnoses().add("Essential Hypertension");
        }
        if (lower.contains("coronary artery disease") || lower.contains("cad") || lower.contains("angina")) {
            data.getDiagnoses().add("Coronary Artery Disease");
        }
        if (lower.contains("asthma") || lower.contains("bronchitis")) {
            data.getDiagnoses().add("Bronchial Asthma");
        }
        if (lower.contains("dyslipidemia")) {
            data.getDiagnoses().add("Dyslipidemia");
        }

        return data;
    }

    private void extractMedicationIfPresent(String text, String drugName, StructuredClinicalData data) {
        Pattern pattern = Pattern.compile("(?i)\\b" + Pattern.quote(drugName) + "\\b(?:\\s+(\\d+(?:\\.\\d+)?\\s*(?:mg|mcg|g)))?(?:\\s+(od|bd|tds|sos|once daily|twice daily))?");
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            String dose = matcher.group(1) != null ? matcher.group(1).trim() : "Standard";
            String freq = matcher.group(2) != null ? matcher.group(2).toUpperCase() : "OD";
            data.getMedications().add(new ExtractedMedication(drugName, dose, freq));
        }
    }

    private void extractNumericLab(String text, String testName, String unit, double normalMin, double normalMax, StructuredClinicalData data) {
        Pattern pattern = Pattern.compile("(?i)" + Pattern.quote(testName.split(" ")[0]) + "[^\\d]*?(\\d+(?:\\.\\d+)?)");
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            try {
                double val = Double.parseDouble(matcher.group(1));
                String status = val < normalMin ? "LOW" : (val > normalMax ? "HIGH" : "NORMAL");
                data.getLabValues().add(new ExtractedLabValue(testName, String.valueOf(val), unit, status));
            } catch (Exception ignored) {}
        }
    }
}
