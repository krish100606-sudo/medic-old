package com.example.Health_Data_Management.service;

import com.example.Health_Data_Management.entity.CasePriority;
import com.example.Health_Data_Management.entity.MedicalCase;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class RedFlagService {

    public static class RedFlagEvaluation {
        private final CasePriority priority;
        private final boolean redFlagsDetected;
        private final String reason;
        private final List<String> detectedFlags;

        public RedFlagEvaluation(CasePriority priority, boolean redFlagsDetected, String reason, List<String> detectedFlags) {
            this.priority = priority;
            this.redFlagsDetected = redFlagsDetected;
            this.reason = reason;
            this.detectedFlags = detectedFlags != null ? detectedFlags : new ArrayList<>();
        }

        public RedFlagEvaluation(CasePriority priority, boolean redFlagsDetected, String reason) {
            this(priority, redFlagsDetected, reason, new ArrayList<>());
        }

        public CasePriority getPriority() { return priority; }
        public boolean isRedFlagsDetected() { return redFlagsDetected; }
        public String getReason() { return reason; }
        public List<String> getDetectedFlags() { return detectedFlags; }
    }

    /**
     * Comprehensive deterministic clinical red-flag detection rules
     * Covers: Cardiac, Neuro, Respiratory, Obstetric, Pediatric, Trauma, Sepsis, and Hindi keywords.
     */
    public RedFlagEvaluation evaluate(MedicalCase medicalCase) {
        if (medicalCase == null) {
            return new RedFlagEvaluation(CasePriority.NORMAL, false, "No clinical data available", List.of());
        }

        String complaint = normalize(medicalCase.getChiefComplaint());
        String symptoms = normalize(medicalCase.getAssociatedSymptoms());
        String statement = normalize(medicalCase.getPatientStatement());
        String severity = normalize(medicalCase.getSeverity());
        String pastHistory = normalize(medicalCase.getPastMedicalHistory());

        String all = complaint + " " + symptoms + " " + statement + " " + pastHistory;

        List<String> flags = new ArrayList<>();
        CasePriority finalPriority = CasePriority.NORMAL;

        // 1. Acute Cardiac / Thoracic
        boolean hasChestPain = all.contains("chest") || all.contains("heart") || all.contains("chhati") || all.contains("छाती") || all.contains("dil");
        boolean hasRadiation = all.contains("left arm") || all.contains("jaw") || all.contains("shoulder") || all.contains("kandhe");
        boolean hasSweating = all.contains("sweat") || all.contains("perspiration") || all.contains("paseena") || all.contains("पसीना");
        boolean isSevere = severity.contains("severe") || severity.contains("critical") || severity.contains("high") ||
                severity.contains("8") || severity.contains("9") || severity.contains("10");

        if (hasChestPain && (hasRadiation || hasSweating || isSevere)) {
            flags.add("Cardiovascular Red Flag: Acute coronary syndrome / myocardial ischemia risk");
            finalPriority = CasePriority.CRITICAL;
        } else if (hasChestPain) {
            flags.add("Cardiovascular Warning: Chest discomfort requiring immediate triage ECG");
            if (finalPriority != CasePriority.CRITICAL) finalPriority = CasePriority.HIGH;
        }

        // 2. Severe Respiratory Distress
        boolean hasBreathlessness = all.contains("breath") || all.contains("saans") || all.contains("सांस") || all.contains("dyspnea") || all.contains("wheez") || all.contains("cyanosis");
        if (hasBreathlessness && (isSevere || all.contains("stridor") || all.contains("blue") || all.contains("choking"))) {
            flags.add("Respiratory Emergency: Acute severe respiratory distress / airway compromise");
            finalPriority = CasePriority.CRITICAL;
        } else if (hasBreathlessness) {
            flags.add("Respiratory Red Flag: Tachypnea or shortness of breath");
            if (finalPriority != CasePriority.CRITICAL) finalPriority = CasePriority.HIGH;
        }

        // 3. Neurological / Stroke (FAST)
        if (all.contains("stroke") || all.contains("paralysis") || all.contains("slurred speech") || all.contains("facial droop") ||
            all.contains("weakness on one side") || all.contains("lakwa") || all.contains("लकवा") || all.contains("sudden loss of vision")) {
            flags.add("Neurological Emergency: Suspected acute stroke / focal deficit (FAST protocol)");
            finalPriority = CasePriority.CRITICAL;
        }

        // 4. Altered Sensorium / Unconsciousness / Seizures
        if (all.contains("unconscious") || all.contains("coma") || all.contains("behosh") || all.contains("बेहोश") ||
            all.contains("convulsion") || all.contains("seizure") || all.contains("daura") || all.contains("दौरा") || all.contains("fainting")) {
            flags.add("Central Nervous System Emergency: Altered level of consciousness or active seizures");
            finalPriority = CasePriority.CRITICAL;
        }

        // 5. Severe Hemorrhage / Trauma / Burns
        if (all.contains("bleeding") || all.contains("hemorrhage") || all.contains("khoon") || all.contains("खून") ||
            all.contains("fracture") || all.contains("head injury") || all.contains("trauma") || all.contains("burn") || all.contains("stab") || all.contains("accident")) {
            flags.add("Trauma / Hemorrhage Warning: Active bleeding or major trauma requiring resuscitation");
            if (finalPriority != CasePriority.CRITICAL) finalPriority = CasePriority.HIGH;
        }

        // 6. Sepsis / Severe Infection with Rigors
        if ((all.contains("fever") || all.contains("bukhar") || all.contains("बुखार")) &&
            (all.contains("stiff neck") || all.contains("confusion") || all.contains("hypothermia") || all.contains("petechiae") || all.contains("purpura"))) {
            flags.add("Infectious / Sepsis Red Flag: Severe febrile illness with systemic involvement or meningism");
            finalPriority = CasePriority.CRITICAL;
        }

        // 7. Obstetric Red Flags
        if (all.contains("pregnant") || all.contains("garbhavastha") || all.contains("गर्भावस्था") || all.contains("trimester")) {
            if (all.contains("bleeding") || all.contains("seizure") || all.contains("severe headache") || all.contains("leaking")) {
                flags.add("Obstetric Emergency: High-risk obstetric complication (antepartum hemorrhage / preeclampsia)");
                finalPriority = CasePriority.CRITICAL;
            }
        }

        // 8. Poisoning / Snakebite / Overdose
        if (all.contains("poison") || all.contains("snake") || all.contains("saanp") || all.contains("सांप") || all.contains("insecticide") || all.contains("overdose")) {
            flags.add("Toxicology Emergency: Suspected envenomation or acute intoxication");
            finalPriority = CasePriority.CRITICAL;
        }

        // 9. Psychiatric Crisis
        if (all.contains("suicide") || all.contains("self-harm") || all.contains("violent") || all.contains("hallucination")) {
            flags.add("Psychiatric Red Flag: Acute behavioral crisis or self-harm risk");
            if (finalPriority != CasePriority.CRITICAL) finalPriority = CasePriority.HIGH;
        }

        boolean redFlagsDetected = !flags.isEmpty();
        String summaryReason = redFlagsDetected
                ? String.join(" | ", flags)
                : "Standard clinical intake. No acute predefined red flags detected.";

        return new RedFlagEvaluation(finalPriority, redFlagsDetected, summaryReason, flags);
    }

    private String normalize(String input) {
        return input != null ? input.toLowerCase().trim() : "";
    }
}
