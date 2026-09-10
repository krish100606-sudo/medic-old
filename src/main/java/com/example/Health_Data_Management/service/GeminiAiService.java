package com.example.Health_Data_Management.service;

import com.example.Health_Data_Management.dto.AiSuggestionResponse;
import com.example.Health_Data_Management.dto.SimilarCaseDto;
import com.example.Health_Data_Management.entity.MedicalCase;
import com.example.Health_Data_Management.service.AdaptiveQuestionService.AdaptiveQuestion;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Gemini AI Service
 * Powered by Google Gemini (gemini-3.5-flash-lite / gemini-3.5-flash) for dynamic adaptive
 * clinical questioning, empathetic real-time conversation, and differential triage synthesis.
 * Built with circuit-breaker rate-limit backoff, request caching, and seamless local fallbacks.
 */
@Service
public class GeminiAiService {

    private static final Logger log = LoggerFactory.getLogger(GeminiAiService.class);

    @Value("${gemini.api.key:}")
    private String apiKey;

    @Value("${gemini.model:gemini-3.5-flash-lite}")
    private String modelName;

    @Value("${gemini.api.url:https://generativelanguage.googleapis.com/v1beta/models}")
    private String apiUrl;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<String, List<AdaptiveQuestion>> questionsCache = new ConcurrentHashMap<>();

    // Circuit-breaker backoff timestamp in millis when 429 quota is hit
    private volatile long rateLimitCooldownUntil = 0;

    public boolean isConfigured() {
        return apiKey != null && !apiKey.trim().isEmpty();
    }

    /**
     * Calls Gemini to generate dynamic clinical follow-up questions tailored to patient answers.
     */
    public List<AdaptiveQuestion> generateAdaptiveQuestions(String chiefComplaint, Map<String, String> answers, int age, String gender) {
        if (!isConfigured()) {
            return Collections.emptyList();
        }

        String cacheKey = (chiefComplaint != null ? chiefComplaint.toLowerCase().trim() : "general") + "_" + age + "_" + (gender != null ? gender : "M");
        if (questionsCache.containsKey(cacheKey)) {
            return questionsCache.get(cacheKey);
        }

        try {
            StringBuilder context = new StringBuilder();
            context.append("Patient: Age=").append(age > 0 ? age : 35).append(", Gender=").append(gender != null ? gender : "Unspecified").append(". ");
            context.append("Chief Complaint: ").append(chiefComplaint != null ? chiefComplaint : "General symptoms").append(". ");
            if (answers != null && !answers.isEmpty()) {
                context.append("Current answers: ");
                answers.forEach((k, v) -> context.append(k).append("=").append(v).append("; "));
            }

            String prompt = "You are an intelligent clinical triage intake assistant for a hospital OPD kiosk. " +
                    "Based on the following patient presentation: [" + context + "], " +
                    "generate exactly 2 high-yield clinical branching questions to narrow down the differential diagnosis. " +
                    "Return ONLY valid JSON as an array of 2 objects with this exact structure: " +
                    "[{\"questionCode\": \"Q_ADAPTIVE_AI_1\", \"questionTextEn\": \"Question in English?\", \"questionTextHi\": \"Question in Hindi (Devanagari script)?\", \"touchOptions\": [\"Option 1 in English / हिंदी\", \"Option 2 in English / हिंदी\", \"None / कोई नहीं\"]}, " +
                    "{\"questionCode\": \"Q_ADAPTIVE_AI_2\", \"questionTextEn\": \"...\", \"questionTextHi\": \"...\", \"touchOptions\": [...]}]";

            String responseText = callGemini(prompt, 8, 300);
            if (responseText == null || responseText.isBlank()) {
                return Collections.emptyList();
            }

            // Extract JSON array
            String cleanJson = cleanJsonString(responseText);
            JsonNode root = objectMapper.readTree(cleanJson);
            if (root.isArray()) {
                List<AdaptiveQuestion> result = new ArrayList<>();
                int idx = 1;
                for (JsonNode node : root) {
                    String code = node.has("questionCode") ? node.get("questionCode").asText() : ("Q_ADAPTIVE_AI_" + idx);
                    String qEn = node.has("questionTextEn") ? node.get("questionTextEn").asText() : "";
                    String qHi = node.has("questionTextHi") ? node.get("questionTextHi").asText() : "";
                    List<String> options = new ArrayList<>();
                    if (node.has("touchOptions") && node.get("touchOptions").isArray()) {
                        for (JsonNode opt : node.get("touchOptions")) {
                            if (opt.isTextual()) {
                                options.add(opt.asText());
                            } else if (opt.has("optionTextEn")) {
                                String en = opt.get("optionTextEn").asText();
                                String hi = opt.has("optionTextHi") ? " / " + opt.get("optionTextHi").asText() : "";
                                options.add(en + hi);
                            }
                        }
                    }
                    if (!qEn.isBlank()) {
                        result.add(new AdaptiveQuestion(code, qEn, qHi, "ADAPTIVE_AI", options, true, true));
                        idx++;
                    }
                }
                if (!result.isEmpty()) {
                    questionsCache.put(cacheKey, result);
                    return result;
                }
            }

        } catch (Exception e) {
            log.info("Gemini adaptive question generation: switching to local engine ({})", e.getMessage());
        }

        return Collections.emptyList();
    }

    /**
     * Generates an instant, highly clinical, zero-latency empathetic acknowledgment.
     * Executes in < 1ms so patient step transitions are instantaneous.
     */
    public String generateInstantEmpatheticResponse(String patientName, String questionCode, String questionText, String answerText, String chiefComplaint) {
        String pName = (patientName != null && !patientName.isBlank() && !patientName.equalsIgnoreCase("Patient")) ? patientName : null;
        String ans = answerText != null ? answerText.trim() : "";
        String ansLower = ans.toLowerCase();
        String ccLower = chiefComplaint != null ? chiefComplaint.toLowerCase() : "";
        String qCode = questionCode != null ? questionCode : "";

        if ("Q_CHIEF_COMPLAINT".equals(qCode)) {
            return (pName != null ? pName + ", thank you " : "Thank you ") + "for specifying your main concern (" + ans + "). Let's gather a few quick details for your doctor.";
        }
        if ("Q_STATEMENT".equals(qCode)) {
            return "Thank you for describing your symptoms in your own words. These details have been captured for the physician's review.";
        }
        if ("Q_ONSET".equals(qCode)) {
            return "Noted that symptoms started " + ans + ". This timeline is vital for assessing clinical acuity.";
        }
        if ("Q_LOCATION".equals(qCode)) {
            return "Recorded location: " + ans + ". Anatomical mapping helps guide targeted bedside examination.";
        }
        if ("Q_SEVERITY".equals(qCode)) {
            return "Discomfort severity rated at " + ans + ". We have prioritized this in your intake profile.";
        }
        if ("Q_ASSOCIATED_SYMPTOMS".equals(qCode)) {
            return "Associated symptoms recorded: " + ans + ". Tracking co-occurring symptoms refines the diagnostic differential.";
        }
        if (qCode.startsWith("Q_ADAPTIVE_")) {
            return "Thank you for clarifying this follow-up detail. This adds essential clinical precision to your chart.";
        }
        if ("Q_PAST_DISEASES".equals(qCode)) {
            return "Past medical conditions logged: " + ans + ". Chronic health context guides safe clinical management.";
        }
        if ("Q_SURGERIES".equals(qCode)) {
            return "Surgical and procedural history saved: " + ans + ".";
        }
        if ("Q_MEDICATIONS".equals(qCode)) {
            return "Current medications recorded: " + ans + ". Stored for automated drug-drug interaction screening.";
        }
        if ("Q_ALLERGIES".equals(qCode)) {
            return "Allergies and family history noted: " + ans + ". All treatment recommendations will respect these sensitivities.";
        }

        // Symptom-specific fallback
        if (ansLower.contains("chest") || ansLower.contains("saans") || ansLower.contains("breath") || ansLower.contains("pain") || ccLower.contains("chest")) {
            return (pName != null ? pName + ", I " : "I ") + "understand. I have noted this symptom regarding your " + (chiefComplaint != null ? chiefComplaint.toLowerCase() : "health") + " with clinical priority.";
        }

        return "Thank you" + (pName != null ? ", " + pName : "") + ". Your response has been securely recorded into your clinical chart.";
    }

    /**
     * Generates a conversational, empathetic response for the interactive patient chat transcript.
     * Uses a fast 2-second timeout so the patient's step transition is instant and responsive.
     */
    public String generateConversationalResponse(String patientName, String questionText, String answerText, String chiefComplaint) {
        if (isConfigured() && System.currentTimeMillis() >= rateLimitCooldownUntil) {
            try {
                String prompt = "You are MediKiosk AI, an empathetic digital clinical intake nurse at a hospital OPD. " +
                        "Patient " + (patientName != null ? patientName : "Patient") + " (Chief complaint: " + chiefComplaint + ") answered: " +
                        "\"" + questionText + "\" with: \"" + answerText + "\". " +
                        "Provide a warm, reassuring response in exactly 1-2 sentences acknowledging what they shared. Do not diagnose or prescribe.";

                String res = callGemini(prompt, 2, 70);
                if (res != null && !res.isBlank()) {
                    return res.trim().replace("\"", "");
                }
            } catch (Exception ignored) {}
        }

        return generateFallbackConversationalResponse(chiefComplaint, answerText);
    }

    /**
     * Generates a broad-picture AI clinical summary and differential assessment
     * comparing the patient against past verified cases from other patients.
     */
    public String generateBroadClinicalSummary(
            MedicalCase currentCase,
            List<SimilarCaseDto> similarCases,
            String topMlDisease,
            double mlProb) {

        if (currentCase == null) {
            return "<div class='text-muted small'>No case data available for evaluation.</div>";
        }

        // Build clinical presentation context
        int age = (currentCase.getPatient() != null && currentCase.getPatient().getAge() != null)
                ? currentCase.getPatient().getAge() : 35;
        String gender = (currentCase.getPatient() != null && currentCase.getPatient().getGender() != null)
                ? currentCase.getPatient().getGender() : "Unspecified";
        String chiefComplaint = currentCase.getChiefComplaint() != null ? currentCase.getChiefComplaint() : "General illness";
        String symptoms = currentCase.getSymptoms() != null ? currentCase.getSymptoms() :
                (currentCase.getAssociatedSymptoms() != null ? currentCase.getAssociatedSymptoms() : chiefComplaint);
        String vitals = currentCase.getVitals() != null ? currentCase.getVitals() : "Not explicitly recorded";
        String pastHistory = currentCase.getPastMedicalHistory() != null ? currentCase.getPastMedicalHistory() : "None reported";

        // Build historical patient precedents context
        StringBuilder precedentsBuilder = new StringBuilder();
        if (similarCases != null && !similarCases.isEmpty()) {
            int idx = 1;
            for (SimilarCaseDto sc : similarCases) {
                precedentsBuilder.append(String.format(
                        "Past Case #%d: [%s, %s, %dyrs] - Presenting Symptoms: '%s' | Verified Diagnosis: '%s' | Treatment Given: '%s' | Clinical Outcome: '%s' (Match Confidence: %d%%).\n",
                        idx++, sc.getCaseNumber(), sc.getPatientGender(), sc.getPatientAge() != null ? sc.getPatientAge() : 40,
                        sc.getSymptoms(), sc.getDiagnosis(), sc.getTreatment(), sc.getOutcome(), sc.getSimilarityScore()
                ));
            }
        } else {
            precedentsBuilder.append("No identical prior cases found in hospital database yet. Relying on primary clinical evidence guidelines.\n");
        }

        if (!isConfigured() || System.currentTimeMillis() < rateLimitCooldownUntil) {
            return generateFallbackBroadSummary(currentCase, similarCases);
        }

        try {
            String prompt = "You are an expert senior consulting physician and clinical decision support system in a hospital OPD.\n\n" +
                    "CURRENT PATIENT INTAKE:\n" +
                    "- Age & Gender: " + age + " years, " + gender + "\n" +
                    "- Chief Complaint: " + chiefComplaint + "\n" +
                    "- Presenting Symptoms: " + symptoms + "\n" +
                    "- Vitals: " + vitals + "\n" +
                    "- Past Medical History: " + pastHistory + "\n\n" +
                    "RELATED HISTORICAL CASES FROM OTHER PATIENTS IN HOSPITAL DATABASE (for comparative learning):\n" +
                    precedentsBuilder + "\n" +
                    "CLINICAL INSTRUCTIONS:\n" +
                    "1. Think on a BROAD CLINICAL PICTURE across all disease categories (viral respiratory, bacterial infections, asthma/COPD, cardiovascular, gastrointestinal, metabolic, etc.). DO NOT fixate or prematurely narrow down to COVID-19 or any single condition unless unequivocal.\n" +
                    "2. Explicitly compare the current patient's presentation with the other patients' historical cases above to see what was previously diagnosed and what treatments were effective.\n" +
                    "3. DO NOT output conversational preamble like 'Here is the summary' or 'Sure'.\n" +
                    "4. Output ONLY clean semantic HTML markup (using <div>, <h6>, <p>, <ul>, <li>, <strong>, <span class='badge ...'>) structured with these exact sections:\n" +
                    "   - <div class='mb-3'><h6 class='fw-bold text-dark mb-1'><i class='bi bi-activity text-primary me-1'></i> Clinical Pattern & Presentation</h6><p class='text-secondary small mb-0'>...</p></div>\n" +
                    "   - <div class='mb-3'><h6 class='fw-bold text-dark mb-1'><i class='bi bi-diagram-3 text-info me-1'></i> Broad Differential Possibilities</h6><ul class='list-unstyled mb-0 small'>...</ul></div>\n" +
                    "   - <div class='mb-3'><h6 class='fw-bold text-dark mb-1'><i class='bi bi-people-fill text-success me-1'></i> Comparative Insights from Other Patients' Data</h6><p class='text-secondary small mb-0'>...</p></div>\n" +
                    "   - <div class='mb-0'><h6 class='fw-bold text-dark mb-1'><i class='bi bi-clipboard2-pulse text-warning me-1'></i> Recommended Bedside Checks & Next Steps</h6><ul class='mb-0 ps-3 small text-secondary'>...</ul></div>";

            String responseText = callGemini(prompt, 12, 500);
            if (responseText != null && !responseText.isBlank()) {
                return formatClinicalInsightsHtml(responseText);
            }
        } catch (Exception e) {
            log.info("Gemini broad clinical summary: using comparative fallback ({})", e.getMessage());
        }

        return generateFallbackBroadSummary(currentCase, similarCases);
    }

    /**
     * Backward-compatible delegation for clinical insights.
     */
    public String generateClinicalInsights(String chiefComplaint, Map<String, String> answers, String topMlDisease, double mlProb) {
        MedicalCase mc = new MedicalCase();
        mc.setChiefComplaint(chiefComplaint);
        if (answers != null && !answers.isEmpty()) {
            mc.setAssociatedSymptoms(String.join(", ", answers.values()));
        }
        return generateBroadClinicalSummary(mc, Collections.emptyList(), topMlDisease, mlProb);
    }

    /**
     * Stage 2: Generates a structured decision-support suggestion (possible diagnosis + treatment options)
     * based on current case + retrieved similar cases from other patients.
     */
    public AiSuggestionResponse generateStructuredSuggestion(MedicalCase currentCase, List<SimilarCaseDto> similarCases) {
        AiSuggestionResponse response = new AiSuggestionResponse();
        response.setCaseId(currentCase != null ? currentCase.getId() : null);
        response.setSimilarCasesCount(similarCases != null ? similarCases.size() : 0);

        int age = (currentCase != null && currentCase.getPatient() != null && currentCase.getPatient().getAge() != null)
                ? currentCase.getPatient().getAge() : 35;
        String gender = (currentCase != null && currentCase.getPatient() != null && currentCase.getPatient().getGender() != null)
                ? currentCase.getPatient().getGender() : "Unspecified";
        String chiefComplaint = currentCase != null && currentCase.getChiefComplaint() != null ? currentCase.getChiefComplaint() : "General symptoms";
        String symptoms = currentCase != null && currentCase.getSymptoms() != null ? currentCase.getSymptoms() : chiefComplaint;
        String vitals = currentCase != null && currentCase.getVitals() != null ? currentCase.getVitals() : "Normal baseline";

        StringBuilder pastCasesCtx = new StringBuilder();
        if (similarCases != null && !similarCases.isEmpty()) {
            for (SimilarCaseDto s : similarCases) {
                pastCasesCtx.append(String.format("- Similar Patient [%s]: Diagnosis was '%s', Treatment: '%s', Outcome: '%s' (%d%% match)\n",
                        s.getCaseNumber(), s.getDiagnosis(), s.getTreatment(), s.getOutcome(), s.getSimilarityScore()));
            }
        } else {
            pastCasesCtx.append("- No direct matching past cases in local repository yet.\n");
        }

        if (isConfigured() && System.currentTimeMillis() >= rateLimitCooldownUntil) {
            try {
                String prompt = "You are a clinical decision support AI assisting a licensed physician.\n" +
                        "Patient: Age=" + age + ", Gender=" + gender + ", Chief Complaint: " + chiefComplaint + ", Symptoms: " + symptoms + ", Vitals: " + vitals + ".\n" +
                        "Relevant historical cases from other patients in this hospital:\n" + pastCasesCtx + "\n" +
                        "INSTRUCTIONS:\n" +
                        "- Evaluate broad differential possibilities (do not assume COVID-19 or any narrow condition unless specifically indicated).\n" +
                        "- Synthesize a recommended diagnosis, clinical reasoning referencing past patient precedents if relevant, and 3-4 specific treatment/investigation options.\n" +
                        "- Explicitly flag uncertainty and recommend doctor confirmation. This is non-binding decision support.\n" +
                        "- Return ONLY valid JSON in this exact structure without code fences:\n" +
                        "{\n" +
                        "  \"diagnosis_suggestion\": \"Primary Suspected Diagnosis (with 1 differential)\",\n" +
                        "  \"reasoning\": \"Clinical reasoning based on symptoms and historical patient precedents...\",\n" +
                        "  \"treatment_options\": [\"Option 1: Med / Dose / Frequency\", \"Option 2: Bedside Test\", \"Option 3: Advice\"],\n" +
                        "  \"confidence\": \"Moderate (75%)\",\n" +
                        "  \"disclaimer\": \"AI-generated suggestion based on similar case history — not a diagnosis. Doctor must verify before finalizing.\"\n" +
                        "}";

                String res = callGemini(prompt, 12, 450);
                if (res != null && !res.isBlank()) {
                    String cleanJson = cleanJsonObjectString(res);
                    JsonNode root = objectMapper.readTree(cleanJson);
                    if (root.has("diagnosis_suggestion")) {
                        response.setDiagnosis_suggestion(root.path("diagnosis_suggestion").asText());
                        response.setReasoning(root.path("reasoning").asText());
                        response.setConfidence(root.path("confidence").asText("Moderate (70%)"));
                        if (root.has("treatment_options") && root.path("treatment_options").isArray()) {
                            List<String> options = new ArrayList<>();
                            for (JsonNode opt : root.path("treatment_options")) {
                                options.add(opt.asText());
                            }
                            response.setTreatment_options(options);
                        }
                        return response;
                    }
                }
            } catch (Exception e) {
                log.info("Gemini structured suggestion: using similarity fallback ({})", e.getMessage());
            }
        }

        // Fallback structured suggestion synthesized from top similar cases
        if (similarCases != null && !similarCases.isEmpty()) {
            SimilarCaseDto top = similarCases.get(0);
            response.setDiagnosis_suggestion(top.getDiagnosis());
            response.setReasoning("Presentation closely parallels historical patient (" + top.getCaseNumber() +
                    ", " + top.getSimilarityScore() + "% match) who presented with " + top.getSymptoms() + " and achieved favorable outcome with prescribed therapy.");
            List<String> options = new ArrayList<>();
            if (top.getTreatment() != null && !top.getTreatment().isBlank()) {
                options.add("Standard Precedent Regimen: " + top.getTreatment());
            }
            options.add("Vital signs monitoring and symptomatic support");
            options.add("Confirmatory bedside laboratory evaluation as per physician discretion");
            response.setTreatment_options(options);
            response.setConfidence(top.getMatchConfidence() + " (" + top.getSimilarityScore() + "%)");
        } else {
            response.setDiagnosis_suggestion("Acute Symptomatic Presentation — Comprehensive Clinical Triage");
            response.setReasoning("Patient presents with " + chiefComplaint + ". Broad differential evaluation required across primary systems.");
            response.setTreatment_options(List.of(
                    "Baseline vitals assessment and clinical examination",
                    "Targeted diagnostic workup based on presenting complaint",
                    "Symptomatic management pending confirmatory physician review"
            ));
            response.setConfidence("General Clinical Triage");
        }

        return response;
    }

    /**
     * Converts markdown / bulleted AI responses into clean, elegant HTML
     * preventing raw asterisks and markdown fences from showing on screen.
     */
    public String formatClinicalInsightsHtml(String raw) {
        if (raw == null || raw.isBlank()) {
            return "<div class='text-muted small'>No clinical summary generated.</div>";
        }

        String s = raw.trim();
        // Remove code fences
        if (s.startsWith("```html")) s = s.substring(7);
        else if (s.startsWith("```")) s = s.substring(3);
        if (s.endsWith("```")) s = s.substring(0, s.length() - 3);
        s = s.trim();

        // If already cleanly formatted semantic HTML without raw markdown asterisks, return it directly
        if (s.contains("<div") && s.contains("<h6") && !s.contains("* **") && !s.contains("###")) {
            return s;
        }

        String[] lines = s.split("\\r?\\n");
        StringBuilder html = new StringBuilder();
        html.append("<div class='ai-clinical-summary-box'>");

        boolean inList = false;

        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.isEmpty()) continue;

            // Skip conversational opener lines
            String lower = trimmed.toLowerCase();
            if (lower.startsWith("here is") || lower.startsWith("certainly") || lower.startsWith("sure,")
                    || lower.startsWith("below is") || lower.startsWith("as requested") || lower.startsWith("note for your review")) {
                continue;
            }

            // Case A: Section Header with bold label, e.g.
            // "* **Presenting Symptoms & Risk Stratification:** Patient presents with..."
            // or "### 1. Differential Diagnoses" or "**Top Differential Diagnoses:**"
            java.util.regex.Matcher headerMatcher = java.util.regex.Pattern
                    .compile("^(?:\\*\\s*)?(?:###?\\s*)?(?:\\*\\*)?([A-Za-z0-9\\s&/—\\-]+?)(?:\\*\\*)?:\\s*(.*)$")
                    .matcher(trimmed);

            if (trimmed.startsWith("###") || trimmed.startsWith("##") || (trimmed.contains("**") && trimmed.contains(":") && headerMatcher.matches())) {
                if (inList) {
                    html.append("</ul>");
                    inList = false;
                }

                String headerTitle;
                String trailingBody = "";

                if (headerMatcher.matches()) {
                    headerTitle = headerMatcher.group(1).replaceAll("^[#\\*\\-\\s]+", "").trim();
                    trailingBody = headerMatcher.group(2).trim();
                } else {
                    headerTitle = trimmed.replaceAll("^[#\\*\\-\\s]+", "").replace("**", "").replace(":", "").trim();
                }

                // Choose semantic icon based on header content
                String titleLower = headerTitle.toLowerCase();
                String iconClass = "bi bi-dot text-primary fs-5";
                if (titleLower.contains("pattern") || titleLower.contains("symptom") || titleLower.contains("presentation") || titleLower.contains("risk")) {
                    iconClass = "bi bi-activity text-primary me-2";
                } else if (titleLower.contains("differential") || titleLower.contains("diagnos")) {
                    iconClass = "bi bi-diagram-3 text-info me-2";
                } else if (titleLower.contains("patient") || titleLower.contains("comparative") || titleLower.contains("case") || titleLower.contains("precedent")) {
                    iconClass = "bi bi-people-fill text-success me-2";
                } else if (titleLower.contains("bedside") || titleLower.contains("check") || titleLower.contains("test") || titleLower.contains("order") || titleLower.contains("step")) {
                    iconClass = "bi bi-clipboard2-pulse text-warning me-2";
                }

                html.append("<div class='mt-3 mb-1 fw-bold text-dark d-flex align-items-center'>")
                    .append("<i class='").append(iconClass).append("'></i>")
                    .append("<span>").append(headerTitle).append("</span>")
                    .append("</div>");

                if (!trailingBody.isEmpty()) {
                    String cleanBody = formatInlineMarkdown(trailingBody);
                    html.append("<p class='small text-secondary mb-2 ps-3'>").append(cleanBody).append("</p>");
                }
                continue;
            }

            // Case B: Numbered list item, e.g. "1. Viral respiratory illness (Influenza, COVID-19)"
            java.util.regex.Matcher numMatcher = java.util.regex.Pattern.compile("^(\\d+)[\\.\\)]\\s*(.*)$").matcher(trimmed);
            if (numMatcher.matches()) {
                if (!inList) {
                    html.append("<ul class='list-unstyled ps-2 mb-2'>");
                    inList = true;
                }
                String num = numMatcher.group(1);
                String itemText = formatInlineMarkdown(numMatcher.group(2));
                html.append("<li class='mb-1 small text-secondary d-flex align-items-start gap-2'>")
                    .append("<span class='badge bg-primary-subtle text-primary border me-1' style='font-size:0.75rem;'>").append(num).append("</span>")
                    .append("<span>").append(itemText).append("</span>")
                    .append("</li>");
                continue;
            }

            // Case C: Bullet points, e.g. "* Obtain vital signs..." or "- Check SpO2"
            if (trimmed.startsWith("*") || trimmed.startsWith("-")) {
                if (!inList) {
                    html.append("<ul class='list-unstyled ps-2 mb-2'>");
                    inList = true;
                }
                String content = trimmed.replaceAll("^[\\*\\-\\s]+", "");
                String itemText = formatInlineMarkdown(content);
                html.append("<li class='mb-1 small text-secondary d-flex align-items-start gap-2'>")
                    .append("<i class='bi bi-chevron-right text-primary mt-1' style='font-size: 0.7rem;'></i>")
                    .append("<span>").append(itemText).append("</span>")
                    .append("</li>");
                continue;
            }

            // Case D: Regular paragraph text
            if (inList) {
                html.append("</ul>");
                inList = false;
            }
            String pContent = formatInlineMarkdown(trimmed);
            html.append("<p class='small text-secondary mb-2'>").append(pContent).append("</p>");
        }

        if (inList) {
            html.append("</ul>");
        }

        html.append("</div>");
        return html.toString();
    }

    private String formatInlineMarkdown(String text) {
        if (text == null) return "";
        // Replace bold **text** with <strong class='text-dark'>text</strong>
        String s = text.replaceAll("\\*\\*(.*?)\\*\\*", "<strong class='text-dark'>$1</strong>");
        // Replace *italic* with <em>italic</em>
        s = s.replaceAll("(?<!\\*)\\*(?!\\*)(.*?)(?<!\\*)\\*(?!\\*)", "<em>$1</em>");
        // Remove any stray unparsed single asterisks
        s = s.replace("*", "");
        return s;
    }

    private String cleanJsonObjectString(String raw) {
        String s = raw.trim();
        if (s.startsWith("```json")) s = s.substring(7);
        else if (s.startsWith("```")) s = s.substring(3);
        if (s.endsWith("```")) s = s.substring(0, s.length() - 3);
        s = s.trim();
        int start = s.indexOf('{');
        int end = s.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return s.substring(start, end + 1);
        }
        return s;
    }

    private String generateFallbackBroadSummary(MedicalCase currentCase, List<SimilarCaseDto> similarCases) {
        StringBuilder sb = new StringBuilder();
        sb.append("<div class='ai-summary-fallback'>");
        sb.append("<div class='mb-3'>");
        sb.append("<h6 class='fw-bold text-dark mb-1'><i class='bi bi-activity text-primary me-1'></i> Clinical Pattern & Presentation</h6>");
        sb.append("<p class='text-secondary small mb-0'>Patient presents with <strong>")
          .append(currentCase.getChiefComplaint() != null ? currentCase.getChiefComplaint() : "reported symptoms")
          .append("</strong>. Comprehensive screening across cardiorespiratory, infectious, and metabolic domains is indicated.</p>");
        sb.append("</div>");

        sb.append("<div class='mb-3'>");
        sb.append("<h6 class='fw-bold text-dark mb-1'><i class='bi bi-diagram-3 text-info me-1'></i> Broad Differential Possibilities</h6>");
        sb.append("<ul class='list-unstyled mb-0 small'>");
        if (similarCases != null && !similarCases.isEmpty()) {
            SimilarCaseDto top = similarCases.get(0);
            sb.append("<li class='mb-1'><span class='badge bg-primary-subtle text-primary border me-1'>Primary Precedent</span> <strong>")
              .append(top.getDiagnosis()).append("</strong> (matches past patient ").append(top.getCaseNumber()).append(" with ").append(top.getSimilarityScore()).append("% alignment)</li>");
            if (similarCases.size() > 1) {
                SimilarCaseDto second = similarCases.get(1);
                sb.append("<li class='mb-1'><span class='badge bg-secondary-subtle text-secondary border me-1'>Differential</span> <strong>")
                  .append(second.getDiagnosis()).append("</strong> (precedent from case ").append(second.getCaseNumber()).append(")</li>");
            }
        } else {
            sb.append("<li class='mb-1'><span class='badge bg-primary-subtle text-primary border me-1'>Evaluation</span> Broad multi-system differential required based on clinical findings.</li>");
        }
        sb.append("</ul>");
        sb.append("</div>");

        sb.append("<div class='mb-3'>");
        sb.append("<h6 class='fw-bold text-dark mb-1'><i class='bi bi-people-fill text-success me-1'></i> Precedents from Other Patients' Data</h6>");
        if (similarCases != null && !similarCases.isEmpty()) {
            sb.append("<p class='text-secondary small mb-0'>Compared against <strong>").append(similarCases.size()).append(" historical cases</strong> in the hospital database with similar presenting symptomatology. Previous patients achieved positive outcomes with targeted therapy.</p>");
        } else {
            sb.append("<p class='text-secondary small mb-0'>No prior matching cases found. As this case is verified by the doctor, it will be added to the hospital learning database for future comparisons.</p>");
        }
        sb.append("</div>");

        sb.append("<div class='mb-0'>");
        sb.append("<h6 class='fw-bold text-dark mb-1'><i class='bi bi-clipboard2-pulse text-warning me-1'></i> Recommended Bedside Checks & Next Steps</h6>");
        sb.append("<ul class='mb-0 ps-3 small text-secondary'>");
        sb.append("<li>Verify complete vital signs (BP, Pulse, SpO2, Temperature).</li>");
        sb.append("<li>Perform focused physical examination corresponding to presenting complaint.</li>");
        sb.append("<li>Order confirmatory diagnostics as per physician clinical judgment.</li>");
        sb.append("</ul>");
        sb.append("</div>");
        sb.append("</div>");
        return sb.toString();
    }

    private String callGemini(String prompt, int timeoutSeconds, int maxTokens) {
        // Skip call if circuit breaker is cooling down after a 429 quota event
        if (System.currentTimeMillis() < rateLimitCooldownUntil) {
            return null;
        }

        try {
            String activeModel = (modelName != null && !modelName.isBlank()) ? modelName : "gemini-3.5-flash-lite";
            String endpoint = apiUrl + "/" + activeModel + ":generateContent?key=" + apiKey;

            Map<String, Object> part = Map.of("text", prompt);
            Map<String, Object> content = Map.of("parts", List.of(part));

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("contents", List.of(content));

            Map<String, Object> genConfig = new HashMap<>();
            genConfig.put("maxOutputTokens", maxTokens > 0 ? maxTokens : 200);
            genConfig.put("temperature", 0.3);
            requestBody.put("generationConfig", genConfig);

            String jsonPayload = objectMapper.writeValueAsString(requestBody);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(endpoint))
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(timeoutSeconds))
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonNode root = objectMapper.readTree(response.body());
                JsonNode candidates = root.path("candidates");
                if (candidates.isArray() && !candidates.isEmpty()) {
                    JsonNode parts = candidates.get(0).path("content").path("parts");
                    if (parts.isArray() && !parts.isEmpty()) {
                        return parts.get(0).path("text").asText();
                    }
                }
            } else if (response.statusCode() == 429) {
                // Rate limited: cool down for 60 seconds to avoid spamming the endpoint
                rateLimitCooldownUntil = System.currentTimeMillis() + 60_000;
                log.info("Gemini API rate limit active. Automatically engaging local ML & rule fallbacks for 60 seconds.");
            } else {
                log.info("Gemini API status {}: fallback engaged.", response.statusCode());
            }
        } catch (java.net.http.HttpTimeoutException e) {
            log.info("Gemini API latency exceeded {}s: local fallback engaged seamlessly.", timeoutSeconds);
        } catch (Exception e) {
            log.info("Gemini API call: using local fallback ({})", e.getMessage());
        }
        return null;
    }

    private String cleanJsonString(String raw) {
        String s = raw.trim();
        if (s.startsWith("```json")) {
            s = s.substring(7);
        } else if (s.startsWith("```")) {
            s = s.substring(3);
        }
        if (s.endsWith("```")) {
            s = s.substring(0, s.length() - 3);
        }
        s = s.trim();
        int start = s.indexOf('[');
        int end = s.lastIndexOf(']');
        if (start >= 0 && end > start) {
            return s.substring(start, end + 1);
        }
        return s;
    }

    private String generateFallbackConversationalResponse(String chiefComplaint, String answerText) {
        String answer = answerText != null ? answerText.toLowerCase() : "";
        String cc = chiefComplaint != null ? chiefComplaint.toLowerCase() : "";
        if (answer.contains("chest") || answer.contains("saans") || answer.contains("breath") || answer.contains("severe") || cc.contains("chest")) {
            return "I understand. Thank you for clarifying. I have noted this symptom regarding your " + (chiefComplaint != null ? chiefComplaint.toLowerCase() : "condition") + " for your doctor to review right away.";
        }
        if (answer.contains("fever") || answer.contains("bukhar") || answer.contains("cough") || answer.contains("khansi")) {
            return "Thank you for describing your symptoms. This information has been recorded for your clinical assessment.";
        }
        if (answer.contains("today") || answer.contains("days") || answer.contains("week") || answer.contains("month")) {
            return "Got it. Recording the timeline of your symptoms helps your doctor tailor the best care.";
        }
        return "Thank you. Your response has been securely recorded into your clinical intake file.";
    }
}
