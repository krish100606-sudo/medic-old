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

/**
 * Gemini AI Service
 * Powered by Google Gemini 3.5 Flash for dynamic adaptive clinical questioning,
 * empathetic real-time conversation, and differential triage synthesis.
 */
@Service
public class GeminiAiService {

    private static final Logger log = LoggerFactory.getLogger(GeminiAiService.class);

    @Value("${gemini.api.key:}")
    private String apiKey;

    @Value("${gemini.model:gemini-3.5-flash}")
    private String modelName;

    @Value("${gemini.api.url:https://generativelanguage.googleapis.com/v1beta/models}")
    private String apiUrl;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(4))
            .build();

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<String, List<AdaptiveQuestion>> questionsCache = new java.util.concurrent.ConcurrentHashMap<>();

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
                    "Return ONLY valid JSON (no markdown fences, or wrapped in ```json) as an array of 2 objects with this exact structure: " +
                    "[{\"questionCode\": \"Q_ADAPTIVE_AI_1\", \"questionTextEn\": \"Question in English?\", \"questionTextHi\": \"Question in Hindi (Devanagari script)?\", \"touchOptions\": [\"Option 1 in English / हिंदी\", \"Option 2 in English / हिंदी\", \"Option 3 in English / हिंदी\", \"None / कोई नहीं\"]}, " +
                    "{\"questionCode\": \"Q_ADAPTIVE_AI_2\", \"questionTextEn\": \"...\", \"questionTextHi\": \"...\", \"touchOptions\": [...]}]";

            String responseText = callGemini(prompt, 10);
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
            log.warn("Gemini adaptive question generation failed, falling back to local engine: {}", e.getMessage());
        }

        return Collections.emptyList();
    }

    /**
     * Generates a conversational, empathetic response for the interactive patient chat transcript.
     */
    public String generateConversationalResponse(String patientName, String questionText, String answerText, String chiefComplaint) {
        if (!isConfigured()) {
            return generateFallbackConversationalResponse(chiefComplaint, answerText);
        }

        try {
            String prompt = "You are MediKiosk AI, an empathetic and professional digital clinical intake nurse at a hospital OPD kiosk. " +
                    "The patient " + (patientName != null ? patientName : "Patient") + " (Chief complaint: " + chiefComplaint + ") just answered the question: " +
                    "\"" + questionText + "\" with the answer: \"" + answerText + "\". " +
                    "Provide a warm, reassuring, and clinically astute response in 1-2 sentences acknowledging what they shared and guiding them smoothly to the next question. " +
                    "Do NOT provide a definitive diagnosis or prescribe medication. Keep it polite, clear, and reassuring. Respond in English with an optional reassuring Hindi phrase if appropriate.";

            String res = callGemini(prompt, 6);
            if (res != null && !res.isBlank()) {
                return res.trim().replace("\"", "");
            }
        } catch (Exception e) {
            log.warn("Gemini conversation response failed: {}", e.getMessage());
        }

        return generateFallbackConversationalResponse(chiefComplaint, answerText);
    }

    /**
     * Generates an AI clinical summary and differential assessment for doctor portal.
     */
    public String generateClinicalInsights(String chiefComplaint, Map<String, String> answers, String topMlDisease, double mlProb) {
        if (!isConfigured()) {
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

            String res = callGemini(prompt, 8);
            if (res != null && !res.isBlank()) {
                return res.trim();
            }
        } catch (Exception e) {
            log.warn("Gemini clinical insights generation failed: {}", e.getMessage());
        }

        return "ML Predictive Suspected Condition: " + topMlDisease + " (" + (int)(mlProb * 100) + "% confidence).";
    }

    private String callGemini(String prompt, int timeoutSeconds) {
        try {
            String endpoint = apiUrl + "/" + modelName + ":generateContent?key=" + apiKey;

            Map<String, Object> part = Map.of("text", prompt);
            Map<String, Object> content = Map.of("parts", List.of(part));
            Map<String, Object> requestBody = Map.of("contents", List.of(content));

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
            } else {
                log.warn("Gemini API returned status {}: {}", response.statusCode(), response.body());
            }
        } catch (Exception e) {
            log.warn("Error calling Gemini API: {}", e.getMessage());
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
        return "Thank you. Your response has been securely recorded into your clinical intake file.";
    }
}
