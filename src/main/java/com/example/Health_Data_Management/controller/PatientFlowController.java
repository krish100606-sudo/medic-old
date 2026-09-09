package com.example.Health_Data_Management.controller;

import com.example.Health_Data_Management.entity.*;
import com.example.Health_Data_Management.repository.PatientRepository;
import com.example.Health_Data_Management.repository.UserRepository;
import com.example.Health_Data_Management.service.AdaptiveQuestionService;
import com.example.Health_Data_Management.service.CaseService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.example.Health_Data_Management.service.GeminiAiService;
import com.example.Health_Data_Management.service.LocalMlInferenceService;

@Controller
@RequestMapping("/patient")
public class PatientFlowController {

    private final PatientRepository patientRepository;
    private final UserRepository userRepository;
    private final CaseService caseService;
    private final AdaptiveQuestionService adaptiveQuestionService;
    private final GeminiAiService geminiAiService;
    private final LocalMlInferenceService localMlInferenceService;

    private final String uploadDir = "uploads";

    public PatientFlowController(
            PatientRepository patientRepository,
            UserRepository userRepository,
            CaseService caseService,
            AdaptiveQuestionService adaptiveQuestionService,
            GeminiAiService geminiAiService,
            LocalMlInferenceService localMlInferenceService) {
        this.patientRepository = patientRepository;
        this.userRepository = userRepository;
        this.caseService = caseService;
        this.adaptiveQuestionService = adaptiveQuestionService;
        this.geminiAiService = geminiAiService;
        this.localMlInferenceService = localMlInferenceService;

        File dir = new File(uploadDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    private Patient getCurrentPatient(Authentication authentication) {
        if (authentication == null) return null;
        String email = authentication.getName();
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) return null;
        return patientRepository.findByUserId(user.getId()).orElse(null);
    }

    // ---------------------------------------------------------
    // PATIENT DASHBOARD
    // ---------------------------------------------------------
    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication, Model model) {
        Patient patient = getCurrentPatient(authentication);
        if (patient == null) {
            return "redirect:/login";
        }

        List<MedicalCase> cases = caseService.getCasesForPatient(patient.getId());
        MedicalCase currentCase = cases.stream()
                .filter(c -> c.getStatus() == CaseStatus.DRAFT || c.getStatus() == CaseStatus.SUBMITTED || c.getStatus() == CaseStatus.UNDER_REVIEW)
                .findFirst()
                .orElse(null);

        model.addAttribute("patient", patient);
        model.addAttribute("currentCase", currentCase);
        model.addAttribute("cases", cases);
        return "patient/dashboard";
    }

    // ---------------------------------------------------------
    // START NEW CASE / CONSENT SCREEN
    // ---------------------------------------------------------
    @GetMapping("/consent")
    public String consentScreen(Authentication authentication, Model model) {
        Patient patient = getCurrentPatient(authentication);
        if (patient == null) return "redirect:/login";

        model.addAttribute("patient", patient);
        return "patient/consent";
    }

    @PostMapping("/consent")
    public String acceptConsent(
            Authentication authentication,
            @RequestParam(value = "consent", defaultValue = "false") boolean consent,
            @RequestParam(value = "abhaId", required = false) String abhaId,
            @RequestParam(value = "digitalSignature", required = false) String digitalSignature) {
        Patient patient = getCurrentPatient(authentication);
        if (patient == null) return "redirect:/login";

        if (!consent) {
            return "redirect:/patient/consent?error=required";
        }

        patient.setConsentAccepted(true);
        patient.setConsentAcceptedAt(LocalDateTime.now());
        if (abhaId != null && !abhaId.trim().isEmpty()) {
            patient.setAbhaId(abhaId.trim());
        }
        patientRepository.save(patient);

        // Start a brand new draft case, removing any old chat messages and previous intake draft
        MedicalCase draftCase = caseService.startNewDraftCase(patient);
        if (digitalSignature != null && !digitalSignature.trim().isEmpty()) {
            draftCase.setDigitalSignature(digitalSignature.trim());
            caseService.saveCase(draftCase);
        }

        return "redirect:/patient/language";
    }

    // ---------------------------------------------------------
    // LANGUAGE SELECTION
    // ---------------------------------------------------------
    @GetMapping("/language")
    public String languageScreen(Authentication authentication, Model model) {
        Patient patient = getCurrentPatient(authentication);
        if (patient == null) return "redirect:/login";

        model.addAttribute("patient", patient);
        return "patient/language";
    }

    @PostMapping("/language")
    public String saveLanguage(
            Authentication authentication,
            @RequestParam("language") String language) {
        Patient patient = getCurrentPatient(authentication);
        if (patient == null) return "redirect:/login";

        patient.setPreferredLanguage(language);
        patientRepository.save(patient);

        // Ensure draft case exists
        caseService.getOrCreateDraftCase(patient);

        return "redirect:/patient/case-taking?step=1";
    }

    // ---------------------------------------------------------
    // CLINICAL CASE-TAKING (STEP-BY-STEP GUIDED INTERFACE)
    // ---------------------------------------------------------
    @GetMapping("/case-taking/new")
    public String newCaseTaking(Authentication authentication) {
        Patient patient = getCurrentPatient(authentication);
        if (patient == null) return "redirect:/login";

        caseService.startNewDraftCase(patient);
        return "redirect:/patient/case-taking?step=1";
    }

    @GetMapping("/case-taking")
    public String caseTaking(
            Authentication authentication,
            @RequestParam(value = "step", defaultValue = "1") int step,
            @RequestParam(value = "reset", defaultValue = "false") boolean reset,
            Model model) {
        Patient patient = getCurrentPatient(authentication);
        if (patient == null) return "redirect:/login";

        if (reset) {
            caseService.startNewDraftCase(patient);
            return "redirect:/patient/case-taking?step=1";
        }

        MedicalCase medicalCase = caseService.getOrCreateDraftCase(patient);

        Map<String, String> existingAnswers = new HashMap<>();
        if (medicalCase.getAnswers() != null) {
            for (CaseAnswer a : medicalCase.getAnswers()) {
                existingAnswers.put(a.getQuestionCode(), a.getAnswerText());
            }
        }

        int age = patient.getAge() != null ? patient.getAge() : 35;
        String gender = patient.getGender() != null ? patient.getGender() : "Male";

        List<AdaptiveQuestionService.AdaptiveQuestion> questions = adaptiveQuestionService.getQuestionsForCase(
                medicalCase.getChiefComplaint(), existingAnswers, age, gender);
        int totalSteps = questions.isEmpty() ? 10 : questions.size();

        if (step < 1) step = 1;
        if (step > totalSteps) step = totalSteps;

        AdaptiveQuestionService.AdaptiveQuestion currentQuestion = (step >= 1 && step <= questions.size())
                ? questions.get(step - 1) : null;

        List<ConversationMessage> conversationMessages = caseService.getConversationHistory(medicalCase.getId());

        int progressPercent = totalSteps > 0 ? Math.min(100, (int) Math.round((step * 100.0) / totalSteps)) : 0;

        model.addAttribute("patient", patient);
        model.addAttribute("medicalCase", medicalCase);
        model.addAttribute("currentStep", step);
        model.addAttribute("totalSteps", totalSteps);
        model.addAttribute("progressPercent", progressPercent);
        model.addAttribute("currentQuestion", currentQuestion);
        model.addAttribute("questionsList", questions);
        model.addAttribute("conversationMessages", conversationMessages);
        model.addAttribute("lang", patient.getPreferredLanguage() != null ? patient.getPreferredLanguage() : "English");

        return "patient/case-taking";
    }

    @PostMapping("/case-taking/save-step")
    public String saveStep(
            Authentication authentication,
            @RequestParam("step") int step,
            @RequestParam(value = "questionCode", required = false) String questionCode,
            @RequestParam(value = "questionText", required = false) String questionText,
            @RequestParam(value = "answerText", required = false) String answerText,
            @RequestParam(value = "inputType", defaultValue = "TEXT") String inputTypeStr,
            @RequestParam(value = "action", defaultValue = "next") String action) {

        Patient patient = getCurrentPatient(authentication);
        if (patient == null) return "redirect:/login";

        MedicalCase medicalCase = caseService.getOrCreateDraftCase(patient);

        InputType inputType = InputType.TEXT;
        try {
            inputType = InputType.valueOf(inputTypeStr.toUpperCase());
        } catch (Exception ignored) {}

        if (questionCode != null && answerText != null && !answerText.trim().isEmpty()) {
            caseService.saveOrUpdateAnswer(medicalCase.getId(), questionCode, questionText, answerText.trim(), inputType);
            medicalCase = caseService.getCaseById(medicalCase.getId());

            // Zero-latency instant empathetic AI conversational feedback in chat transcript (< 1ms)
            String patientName = (patient.getUser() != null && patient.getUser().getName() != null)
                    ? patient.getUser().getName() : "Patient";
            String lang = patient.getPreferredLanguage() != null ? patient.getPreferredLanguage() : "English";

            if (geminiAiService != null) {
                try {
                    String instantReply = geminiAiService.generateInstantEmpatheticResponse(
                            patientName,
                            questionCode,
                            questionText != null ? questionText : questionCode,
                            answerText.trim(),
                            medicalCase != null ? medicalCase.getChiefComplaint() : "Symptoms"
                    );
                    if (instantReply != null && !instantReply.isBlank()) {
                        caseService.logConversationMessage(medicalCase.getId(), "SYSTEM", instantReply, "AI_REPLY", questionCode, lang, "AI");
                    }
                } catch (Exception ignored) {}
            }

            // Pre-warm adaptive branching questions in background as soon as chief complaint is recorded
            if ("Q_CHIEF_COMPLAINT".equals(questionCode) && adaptiveQuestionService != null) {
                int patientAge = patient.getAge() != null ? patient.getAge() : 35;
                String patientGender = patient.getGender() != null ? patient.getGender() : "Male";
                adaptiveQuestionService.prewarmAdaptiveQuestions(answerText.trim(), patientAge, patientGender);
            }
        }

        if ("exit".equalsIgnoreCase(action)) {
            return "redirect:/patient/dashboard";
        }

        if ("back".equalsIgnoreCase(action)) {
            int prevStep = Math.max(1, step - 1);
            return "redirect:/patient/case-taking?step=" + prevStep;
        }

        Map<String, String> existingAnswers = new HashMap<>();
        if (medicalCase != null && medicalCase.getAnswers() != null) {
            for (CaseAnswer a : medicalCase.getAnswers()) {
                existingAnswers.put(a.getQuestionCode(), a.getAnswerText());
            }
        }
        int age = patient.getAge() != null ? patient.getAge() : 35;
        String gender = patient.getGender() != null ? patient.getGender() : "Male";
        String currentChief = medicalCase != null ? medicalCase.getChiefComplaint() : null;
        int totalSteps = adaptiveQuestionService.getTotalQuestionCount(currentChief, existingAnswers, age, gender);

        int nextStep = step + 1;
        if (nextStep > totalSteps) {
            return "redirect:/patient/document-upload";
        }

        return "redirect:/patient/case-taking?step=" + nextStep;
    }

    // ---------------------------------------------------------
    // DOCUMENT UPLOAD & OCR
    // ---------------------------------------------------------
    @GetMapping("/document-upload")
    public String documentUploadScreen(Authentication authentication, Model model) {
        Patient patient = getCurrentPatient(authentication);
        if (patient == null) return "redirect:/login";

        MedicalCase medicalCase = caseService.getOrCreateDraftCase(patient);

        Map<String, String> existingAnswers = new HashMap<>();
        if (medicalCase.getAnswers() != null) {
            for (CaseAnswer a : medicalCase.getAnswers()) {
                existingAnswers.put(a.getQuestionCode(), a.getAnswerText());
            }
        }
        int lastStep = adaptiveQuestionService.getTotalQuestionCount(medicalCase.getChiefComplaint(), existingAnswers);

        model.addAttribute("patient", patient);
        model.addAttribute("medicalCase", medicalCase);
        model.addAttribute("documents", medicalCase.getDocuments());
        model.addAttribute("lastStep", lastStep > 0 ? lastStep : 10);
        return "patient/document-upload";
    }

    @PostMapping("/document-upload")
    public String uploadDocument(
            Authentication authentication,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "documentType", defaultValue = "PRESCRIPTION") String docTypeStr) {

        Patient patient = getCurrentPatient(authentication);
        if (patient == null) return "redirect:/login";

        MedicalCase medicalCase = caseService.getOrCreateDraftCase(patient);

        DocumentType docType = DocumentType.PRESCRIPTION;
        try {
            docType = DocumentType.valueOf(docTypeStr.toUpperCase());
        } catch (Exception ignored) {}

        if (!file.isEmpty()) {
            String originalFileName = file.getOriginalFilename();
            String storedFileName = System.currentTimeMillis() + "_" + (originalFileName != null ? originalFileName.replaceAll("\\s+", "_") : "doc.jpg");
            try {
                Path targetPath = Paths.get(uploadDir, storedFileName);
                Files.write(targetPath, file.getBytes());
            } catch (IOException e) {
                // In demo, fallback gracefully
            }
            caseService.addAndProcessDocument(medicalCase.getId(), patient.getId(), storedFileName, originalFileName, file.getContentType(), docType);
        }

        return "redirect:/patient/document-upload";
    }

    @PostMapping("/document-upload/sample")
    public String attachSampleDocument(
            Authentication authentication,
            @RequestParam("sampleType") String sampleType) {

        Patient patient = getCurrentPatient(authentication);
        if (patient == null) return "redirect:/login";

        MedicalCase medicalCase = caseService.getOrCreateDraftCase(patient);

        if ("prescription".equalsIgnoreCase(sampleType)) {
            caseService.addAndProcessDocument(medicalCase.getId(), patient.getId(), "previous-prescription.jpg", "previous-prescription.jpg", "image/jpeg", DocumentType.PRESCRIPTION);
        } else if ("blood".equalsIgnoreCase(sampleType)) {
            caseService.addAndProcessDocument(medicalCase.getId(), patient.getId(), "blood-report.jpg", "blood-report.jpg", "image/jpeg", DocumentType.BLOOD_REPORT);
        }

        return "redirect:/patient/document-upload";
    }

    // ---------------------------------------------------------
    // FINAL CLINICAL REVIEW
    // ---------------------------------------------------------
    @GetMapping("/review")
    public String reviewScreen(Authentication authentication, Model model) {
        Patient patient = getCurrentPatient(authentication);
        if (patient == null) return "redirect:/login";

        MedicalCase medicalCase = caseService.getOrCreateDraftCase(patient);
        medicalCase = caseService.generateSummaryAndEvaluateRedFlags(medicalCase.getId());

        model.addAttribute("patient", patient);
        model.addAttribute("medicalCase", medicalCase);
        model.addAttribute("documents", medicalCase.getDocuments());
        return "patient/review";
    }

    // ---------------------------------------------------------
    // SUBMIT CASE
    // ---------------------------------------------------------
    @PostMapping("/submit-case")
    public String submitCase(Authentication authentication) {
        Patient patient = getCurrentPatient(authentication);
        if (patient == null) return "redirect:/login";

        MedicalCase medicalCase = caseService.getOrCreateDraftCase(patient);
        MedicalCase submitted = caseService.submitCase(medicalCase.getId());

        return "redirect:/patient/case-complete?id=" + submitted.getId();
    }

    // ---------------------------------------------------------
    // CASE COMPLETE CONFIRMATION
    // ---------------------------------------------------------
    @GetMapping("/case-complete")
    public String caseComplete(
            @RequestParam("id") Long caseId,
            Authentication authentication,
            Model model) {
        Patient patient = getCurrentPatient(authentication);
        if (patient == null) return "redirect:/login";

        MedicalCase medicalCase = caseService.getCaseById(caseId);
        if (medicalCase == null) return "redirect:/patient/dashboard";

        model.addAttribute("patient", patient);
        model.addAttribute("medicalCase", medicalCase);
        return "patient/case-complete";
    }
}
