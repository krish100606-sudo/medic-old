package com.example.Health_Data_Management.service;

import com.example.Health_Data_Management.dto.SimilarCaseDto;
import com.example.Health_Data_Management.entity.CaseStatus;
import com.example.Health_Data_Management.entity.MedicalCase;
import com.example.Health_Data_Management.repository.MedicalCaseRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class CaseSimilarityService {

    private static final Logger log = LoggerFactory.getLogger(CaseSimilarityService.class);

    private final MedicalCaseRepository caseRepository;

    // Clinical synonym mapping to canonical tokens
    private static final Map<String, String> SYNONYM_MAP = new HashMap<>();

    static {
        // Cardiovascular & Respiratory
        registerSynonyms("chest_pain", "chest pain", "angina", "chest discomfort", "cardiac pain", "substernal pain", "chest tightness", "chest pressure");
        registerSynonyms("dyspnea", "shortness of breath", "breathing difficulty", "dyspnea", "breathlessness", "saans", "orthopnea", "breath shortness");
        registerSynonyms("palpitations", "palpitations", "rapid heartbeat", "tachycardia", "racing heart", "fluttering");
        registerSynonyms("diaphoresis", "sweating", "perspiration", "diaphoresis", "cold sweats");
        registerSynonyms("edema", "pedal edema", "swelling", "edema", "swollen feet", "swollen legs", "fluid retention");

        // Infectious & General
        registerSynonyms("fever", "fever", "pyrexia", "bukhar", "high temperature", "febrile", "chills", "rigors");
        registerSynonyms("cough", "cough", "khansi", "dry cough", "productive cough", "throat irritation");
        registerSynonyms("sore_throat", "sore throat", "throat pain", "pharyngitis", "gala kharab", "difficulty swallowing", "odynophagia");
        registerSynonyms("wheezing", "wheezing", "wheeze", "stridor", "bronchospasm", "asthma attack");
        registerSynonyms("headache", "headache", "sar dard", "migraine", "cephalea");
        registerSynonyms("fatigue", "fatigue", "weakness", "lethargy", "exhaustion", "malaise", "tiredness");
        registerSynonyms("nausea_vomiting", "nausea", "vomiting", "emesis", "ulti", "queasiness");
        registerSynonyms("abdominal_pain", "abdominal pain", "stomach pain", "pet dard", "cramps", "epigastric pain");
        registerSynonyms("diarrhea", "diarrhea", "loose stools", "watery stool", "motions", "dast", "loose motions");
        registerSynonyms("dizziness", "dizziness", "giddiness", "lightheadedness", "vertigo", "chakkar");
        registerSynonyms("congestion", "nasal congestion", "runny nose", "cold", "rhinitis", "blocked nose");
        registerSynonyms("rash", "rash", "skin eruption", "pruritus", "itching", "urticaria", "hives", "red spots");
        registerSynonyms("joint_pain", "joint pain", "arthralgia", "arthritis", "swollen joint", "knee pain", "joint stiffness");
        registerSynonyms("dysuria", "burning urination", "painful urination", "dysuria", "urinary burning", "burning micturition");
        registerSynonyms("jaundice", "jaundice", "yellow eyes", "yellow skin", "icterus", "piliya");
        registerSynonyms("back_pain", "back pain", "backache", "lumbar pain", "spine pain", "kamar dard");

        // Chronic categories
        registerSynonyms("diabetes", "diabetes", "diabetic", "t2dm", "high blood sugar", "hyperglycemia");
        registerSynonyms("hypertension", "hypertension", "high bp", "elevated bp", "htn");
    }

    private static void registerSynonyms(String canonical, String... phrases) {
        for (String phrase : phrases) {
            SYNONYM_MAP.put(phrase.toLowerCase(), canonical);
        }
    }

    public CaseSimilarityService(MedicalCaseRepository caseRepository) {
        this.caseRepository = caseRepository;
    }

    /**
     * Finds top N past cases similar to the given case ID.
     */
    public List<SimilarCaseDto> findSimilarCases(Long targetCaseId, int limit) {
        log.debug("Searching similar past cases for caseId={}, limit={}", targetCaseId, limit);
        MedicalCase targetCase = caseRepository.findById(targetCaseId).orElse(null);
        if (targetCase == null) {
            return Collections.emptyList();
        }

        Set<String> targetSymptoms = extractCanonicalSymptoms(targetCase);
        Integer targetAge = targetCase.getPatient() != null ? targetCase.getPatient().getAge() : null;
        String targetGender = targetCase.getPatient() != null ? targetCase.getPatient().getGender() : null;
        String targetHistory = targetCase.getPastMedicalHistory() != null ? targetCase.getPastMedicalHistory().toLowerCase() : "";

        List<MedicalCase> allCases = caseRepository.findAll();
        List<SimilarCaseDto> matches = new ArrayList<>();

        for (MedicalCase candidate : allCases) {
            // Exclude target case itself
            if (candidate.getId().equals(targetCaseId)) {
                continue;
            }

            // Only consider cases with past clinical records or completed/verified statuses
            boolean hasClinicalRecord = (candidate.getDiagnosis() != null && !candidate.getDiagnosis().isBlank())
                    || (candidate.getTreatment() != null && !candidate.getTreatment().isBlank())
                    || candidate.getStatus() == CaseStatus.VERIFIED
                    || candidate.getStatus() == CaseStatus.COMPLETED
                    || candidate.getStatus() == CaseStatus.DIAGNOSED
                    || candidate.getStatus() == CaseStatus.TREATMENT_STARTED;

            if (!hasClinicalRecord) {
                continue;
            }

            Set<String> candidateSymptoms = extractCanonicalSymptoms(candidate);
            List<String> matchReasons = new ArrayList<>();

            // 1. Symptom Similarity (Weight: 65%)
            double symptomScore = calculateSymptomSimilarity(targetSymptoms, candidateSymptoms, matchReasons);

            // 2. Diagnosis & History Overlap (Weight: 20%)
            double historyScore = calculateHistoryOverlap(targetHistory, candidate, matchReasons);

            // 3. Demographics Alignment (Weight: 15%)
            double demographicScore = calculateDemographicScore(targetAge, targetGender, candidate, matchReasons);

            // Overall weighted score (0.0 to 1.0)
            double combinedScore = (symptomScore * 0.65) + (historyScore * 0.20) + (demographicScore * 0.15);
            int percentageScore = (int) Math.round(Math.min(1.0, Math.max(0.0, combinedScore)) * 100);

            // Filter out candidates with very low or negligible similarity
            if (percentageScore < 20 && symptomScore < 0.15) {
                continue;
            }

            SimilarCaseDto dto = new SimilarCaseDto();
            dto.setCaseId(candidate.getId());
            dto.setCaseNumber(candidate.getCaseNumber());
            if (candidate.getPatient() != null) {
                dto.setPatientAge(candidate.getPatient().getAge());
                dto.setPatientGender(candidate.getPatient().getGender());
            }

            dto.setSymptoms(candidate.getSymptoms() != null ? candidate.getSymptoms() : candidate.getChiefComplaint());
            dto.setSymptomTags(candidate.getSymptomTags());
            dto.setDiagnosis(candidate.getDiagnosis() != null ? candidate.getDiagnosis() : "Clinical evaluation completed");
            dto.setTreatment(candidate.getTreatment() != null ? candidate.getTreatment() : candidate.getCurrentMedication());
            dto.setVitals(candidate.getVitals() != null ? candidate.getVitals() : "Standard baseline vitals");
            dto.setOutcome(candidate.getOutcome() != null ? candidate.getOutcome() : "Clinically Verified & Discharged");
            dto.setDoctorNotes(candidate.getDoctorClinicalNotes());
            dto.setSimilarityScore(percentageScore);

            if (percentageScore >= 65) {
                dto.setMatchConfidence("HIGH");
            } else if (percentageScore >= 40) {
                dto.setMatchConfidence("MODERATE");
            } else {
                dto.setMatchConfidence("LOW");
            }

            dto.setMatchReasons(matchReasons);
            matches.add(dto);
        }

        // Sort descending by similarity score
        matches.sort((a, b) -> Integer.compare(b.getSimilarityScore(), a.getSimilarityScore()));

        // Return top N
        int max = limit > 0 ? limit : 5;
        return matches.stream().limit(max).collect(Collectors.toList());
    }

    /**
     * Extracts canonical clinical symptom tags from a medical case.
     */
    public Set<String> extractCanonicalSymptoms(MedicalCase medicalCase) {
        Set<String> symptoms = new HashSet<>();
        if (medicalCase == null) return symptoms;

        // Structured symptoms field
        if (medicalCase.getSymptoms() != null) {
            extractTokensAndSynonyms(medicalCase.getSymptoms(), symptoms);
        }

        // Chief complaint
        if (medicalCase.getChiefComplaint() != null) {
            extractTokensAndSynonyms(medicalCase.getChiefComplaint(), symptoms);
        }

        // Associated symptoms
        if (medicalCase.getAssociatedSymptoms() != null) {
            extractTokensAndSynonyms(medicalCase.getAssociatedSymptoms(), symptoms);
        }

        // Patient statement
        if (medicalCase.getPatientStatement() != null) {
            extractTokensAndSynonyms(medicalCase.getPatientStatement(), symptoms);
        }

        // Case answers if available
        if (medicalCase.getAnswers() != null) {
            medicalCase.getAnswers().forEach(a -> {
                if (a.getAnswerText() != null) {
                    extractTokensAndSynonyms(a.getAnswerText(), symptoms);
                }
            });
        }

        return symptoms;
    }

    private void extractTokensAndSynonyms(String rawText, Set<String> output) {
        if (rawText == null || rawText.isBlank()) return;
        String lower = rawText.toLowerCase();

        // 1. Check known multi-word clinical synonyms
        for (Map.Entry<String, String> entry : SYNONYM_MAP.entrySet()) {
            if (lower.contains(entry.getKey())) {
                output.add(entry.getValue());
            }
        }

        // 2. Tokenize individual words
        String[] words = lower.split("[^a-zA-Z0-9]+");
        for (String word : words) {
            if (word.length() >= 3 && !isStopWord(word)) {
                if (SYNONYM_MAP.containsKey(word)) {
                    output.add(SYNONYM_MAP.get(word));
                } else {
                    output.add(word);
                }
            }
        }
    }

    private double calculateSymptomSimilarity(Set<String> target, Set<String> candidate, List<String> matchReasons) {
        if (target.isEmpty() || candidate.isEmpty()) {
            return 0.0;
        }

        Set<String> intersection = new HashSet<>(target);
        intersection.retainAll(candidate);

        Set<String> union = new HashSet<>(target);
        union.addAll(candidate);

        if (union.isEmpty()) return 0.0;

        double jaccard = (double) intersection.size() / union.size();

        // Extra boost for high-yield primary symptom matches
        boolean sharedPrimary = false;
        List<String> matchedLabels = new ArrayList<>();
        for (String matched : intersection) {
            matchedLabels.add(formatCanonicalTag(matched));
            if (matched.equals("chest_pain") || matched.equals("dyspnea") || matched.equals("fever")) {
                sharedPrimary = true;
            }
        }

        if (!matchedLabels.isEmpty() && matchReasons != null) {
            matchReasons.add("Shared symptoms: " + String.join(", ", matchedLabels.subList(0, Math.min(3, matchedLabels.size()))));
        }

        double score = jaccard;
        long canonicalMatches = intersection.stream().filter(CaseSimilarityService::isRecognizedCanonical).count();
        long targetCanonicalCount = target.stream().filter(CaseSimilarityService::isRecognizedCanonical).count();
        if (targetCanonicalCount > 0 && canonicalMatches > 0) {
            double conceptRecall = (double) canonicalMatches / targetCanonicalCount;
            score = Math.max(score, conceptRecall * 0.85);
        }

        if (sharedPrimary) {
            score = Math.min(1.0, score + 0.20);
        }

        return Math.min(1.0, score);
    }

    private static boolean isRecognizedCanonical(String tag) {
        return tag != null && (tag.contains("_") || SYNONYM_MAP.containsValue(tag));
    }

    private double calculateHistoryOverlap(String targetHistory, MedicalCase candidate, List<String> matchReasons) {
        if (candidate.getDiagnosis() == null && candidate.getPastMedicalHistory() == null) {
            return 0.3; // neutral baseline
        }

        String candDiag = candidate.getDiagnosis() != null ? candidate.getDiagnosis().toLowerCase() : "";
        String candHist = candidate.getPastMedicalHistory() != null ? candidate.getPastMedicalHistory().toLowerCase() : "";

        double score = 0.2;
        if (!targetHistory.isBlank()) {
            if (targetHistory.contains("diabetes") && (candDiag.contains("diabetes") || candHist.contains("diabetes"))) {
                score += 0.4;
                matchReasons.add("Related metabolic profile (Diabetes)");
            }
            if (targetHistory.contains("hypertension") && (candDiag.contains("hypertension") || candHist.contains("hypertension"))) {
                score += 0.4;
                matchReasons.add("Cardiovascular risk history (Hypertension)");
            }
            if (targetHistory.contains("cad") || targetHistory.contains("angina") || targetHistory.contains("coronary")) {
                if (candDiag.contains("coronary") || candDiag.contains("angina") || candDiag.contains("acs")) {
                    score += 0.5;
                    matchReasons.add("Known ischemic heart disease overlap");
                }
            }
        }

        return Math.min(1.0, score);
    }

    private double calculateDemographicScore(Integer targetAge, String targetGender, MedicalCase candidate, List<String> matchReasons) {
        if (candidate.getPatient() == null) return 0.5;

        Integer candAge = candidate.getPatient().getAge();
        String candGender = candidate.getPatient().getGender();

        double ageScore = 0.5;
        if (targetAge != null && candAge != null) {
            int diff = Math.abs(targetAge - candAge);
            if (diff <= 5) {
                ageScore = 1.0;
                matchReasons.add("Closely matched age group (±5 yrs)");
            } else if (diff <= 15) {
                ageScore = 0.8;
            } else if (diff <= 25) {
                ageScore = 0.5;
            } else {
                ageScore = 0.3;
            }
        }

        double genderScore = 0.5;
        if (targetGender != null && candGender != null) {
            if (targetGender.equalsIgnoreCase(candGender)) {
                genderScore = 0.8;
            }
        }

        return (ageScore * 0.7) + (genderScore * 0.3);
    }

    private String formatCanonicalTag(String raw) {
        if (raw == null) return "";
        return raw.replace("_", " ");
    }

    private boolean isStopWord(String word) {
        return Set.of("the", "and", "for", "with", "has", "had", "have", "this", "that", "from",
                "days", "today", "yesterday", "since", "patient", "reported", "having", "mild", "severe", "feeling").contains(word);
    }
}
