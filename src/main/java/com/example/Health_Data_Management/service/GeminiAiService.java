package com.example.Health_Data_Management.service;

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
     * Generates a conversational, empathetic response for the interactive patient chat transcript.
     * Uses a fast 3-second timeout so the patient's step transition is instant and responsive.
     */
    public String generateConversationalResponse(String patientName, String questionText, String answerText, String chiefComplaint) {
        if (isConfigured() && System.currentTimeMillis() >= rateLimitCooldownUntil) {
            try {
                String prompt = "You are MediKiosk AI, an empathetic digital clinical intake nurse at a hospital OPD. " +
                        "Patient " + (patientName != null ? patientName : "Patient") + " (Chief complaint: " + chiefComplaint + ") answered: " +
                        "\"" + questionText + "\" with: \"" + answerText + "\". " +
                        "Provide a warm, reassuring response in exactly 1-2 sentences acknowledging what they shared. Do not diagnose or prescribe.";

                String res = callGemini(prompt, 3, 70);
                if (res != null && !res.isBlank()) {
                    return res.trim().replace("\"", "");
                }
            } catch (Exception ignored) {}
        }

        return generateFallbackConversationalResponse(chiefComplaint, answerText);
    }

    /**
     * Generates an AI clinical summary and differential assessment for doctor portal.
     */
    public String generateClinicalInsights(String chiefComplaint, Map<String, String> answers, String topMlDisease, double mlProb) {
        if (!isConfigured() || System.currentTimeMillis() < rateLimitCooldownUntil) {
            return "ML Predictive Suspected Condition: " + topMlDisease + " (" + (int)(mlProb * 100) + "% confidence). Further physician verification advised.";
        }

        try {
            String prompt = "You are a clinical decision support assistant for a consulting physician. " +
                    "Patient intake data: Chief complaint: " + chiefComplaint + ". " +
                    "Reported answers: " + answers + ". " +
                    "Local ML Random Forest classification: Suspected " + topMlDisease + " (Confidence: " + (int)(mlProb * 100) + "%). " +
                    "Write a concise, 3-bullet clinical impression note for the doctor summarizing: " +
                    "1. Key presenting symptoms and red-flag risks, " +
                    "2. Top 2 differential diagnoses to investigate, " +
                    "3. Recommended bedside checks / tests.";

            String res = callGemini(prompt, 8, 250);
            if (res != null && !res.isBlank()) {
                return res.trim();
            }
        } catch (Exception e) {
            log.info("Gemini clinical insights: falling back to ML model summary ({})", e.getMessage());
        }

        return "ML Predictive Suspected Condition: " + topMlDisease + " (" + (int)(mlProb * 100) + "% confidence).";
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
