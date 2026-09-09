package com.example.Health_Data_Management;

import com.example.Health_Data_Management.entity.*;
import com.example.Health_Data_Management.repository.*;
import com.example.Health_Data_Management.service.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class FlowVerificationTest {

    @Autowired
    private CaseService caseService;

    @Autowired
    private AdaptiveQuestionService adaptiveQuestionService;

    @Autowired
    private RedFlagService redFlagService;

    @Autowired
    private SummaryService summaryService;

    @Autowired
    private AbdmIntegrationService abdmIntegrationService;

    @Test
    void testAdaptiveQuestionsForDifferentComplaints() {
        // Chest pain should have 12 questions (including exertion & cardiac risk)
        List<AdaptiveQuestionService.AdaptiveQuestion> chestQuestions =
                adaptiveQuestionService.getQuestionsForCase("Chest Pain", Map.of());
        assertEquals(12, chestQuestions.size());

        // Fever should have 12 questions (fever pattern & travel)
        List<AdaptiveQuestionService.AdaptiveQuestion> feverQuestions =
                adaptiveQuestionService.getQuestionsForCase("Fever and Cough", Map.of());
        assertEquals(12, feverQuestions.size());

        // Generic should have 10 questions
        List<AdaptiveQuestionService.AdaptiveQuestion> genericQuestions =
                adaptiveQuestionService.getQuestionsForCase("General Checkup", Map.of());
        assertEquals(10, genericQuestions.size());
    }

    @Test
    void testRedFlagDetection() {
        MedicalCase c = new MedicalCase();
        c.setChiefComplaint("Chest Pain");
        c.setAssociatedSymptoms("Shortness of breath, pain radiating to left arm");
        c.setSeverity("Severe (8/10)");

        RedFlagService.RedFlagEvaluation eval = redFlagService.evaluate(c);
        assertTrue(eval.isRedFlagsDetected());
        assertEquals(CasePriority.CRITICAL, eval.getPriority());
    }

    @Test
    void testAbdmFhirBundle() {
        MedicalCase c = new MedicalCase();
        c.setId(101L);
        c.setChiefComplaint("Chest Pain");
        c.setCurrentMedication("Tab. Metformin 500 mg BD");

        Map<String, Object> bundle = abdmIntegrationService.generateFhirBundle(c);
        assertNotNull(bundle);
        assertEquals("Bundle", bundle.get("resourceType"));
        assertTrue(abdmIntegrationService.validateAbhaId("91-2026-4491-8899"));
    }

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void testCaseCreationAndSubmissionFlow() {
        String testEmail = "testpatient_" + System.currentTimeMillis() + "@medikiosk.com";
        String testPatientId = "P-TEST-" + System.currentTimeMillis();
        User user = new User("Test Patient", testEmail, "pass123", Role.PATIENT);
        userRepository.save(user);

        Patient patient = new Patient(user, testPatientId, 30, "Male", "9876543219", "General Medicine", "English");
        patientRepository.save(patient);

        MedicalCase draft = caseService.getOrCreateDraftCase(patient);
        assertNotNull(draft);
        assertNotNull(draft.getId());

        // Test saving digital signature (large string)
        String sampleSignature = "data:image/png;base64," + "A".repeat(5000);
        draft.setDigitalSignature(sampleSignature);
        caseService.saveCase(draft);

        // Test saving answer
        caseService.saveOrUpdateAnswer(draft.getId(), "Q_CHIEF_COMPLAINT", "Main Health Problem", "Fever and Cough", InputType.TOUCH);

        MedicalCase updated = caseService.getCaseById(draft.getId());
        assertEquals("Fever and Cough", updated.getChiefComplaint());

        // Test submit case
        MedicalCase submitted = caseService.submitCase(draft.getId());
        assertEquals(CaseStatus.SUBMITTED, submitted.getStatus());
        assertNotNull(submitted.getTokenNumber());
        assertNotNull(submitted.getCaseNumber());
        assertFalse(submitted.getCaseNumber().startsWith("DRAFT-"), "Case number should be finalized on submit, not remain DRAFT");
        assertNotNull(submitted.getStructuredSummary());
    }

    @Autowired
    private GeminiAiService geminiAiService;

    @Autowired
    private LocalMlInferenceService localMlInferenceService;

    @Test
    void testSummaryGeneration() {
        MedicalCase mc = new MedicalCase();
        mc.setChiefComplaint("Migraine");
        String summary = summaryService.generateStructuredSummary(mc, List.of());
        assertNotNull(summary);
        assertTrue(summary.contains("Migraine"));
    }

    @Test
    void testGeminiAndLocalMlIntegration() {
        // Test Gemini configuration
        assertNotNull(geminiAiService);
        assertTrue(geminiAiService.isConfigured(), "Gemini AI API key should be configured");

        // Test fallback conversational response
        String reply = geminiAiService.generateConversationalResponse("Rahul", "What is your main health problem?", "Chest pain and sweating", "Chest Pain");
        assertNotNull(reply);
        assertFalse(reply.isBlank());

        // Test Local Random Forest ML inference
        LocalMlInferenceService.MlInferenceResult mlResult = localMlInferenceService.predict(45, "Male", List.of("chest pain", "sweating", "shortness of breath"));
        assertNotNull(mlResult);
        assertNotNull(mlResult.getTopDisease());
        assertNotNull(mlResult.getPredictions());
        assertFalse(mlResult.getPredictions().isEmpty());
    }
}
