package com.example.Health_Data_Management.controller;

import com.example.Health_Data_Management.entity.*;
import com.example.Health_Data_Management.repository.DoctorRepository;
import com.example.Health_Data_Management.repository.UserRepository;
import com.example.Health_Data_Management.service.CaseService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import com.example.Health_Data_Management.service.GeminiAiService;
import com.example.Health_Data_Management.service.LocalMlInferenceService;

@Controller
@RequestMapping("/doctor")
public class DoctorPortalController {

    private final CaseService caseService;
    private final DoctorRepository doctorRepository;
    private final UserRepository userRepository;
    private final LocalMlInferenceService localMlInferenceService;
    private final GeminiAiService geminiAiService;
    private final com.example.Health_Data_Management.service.CaseSimilarityService caseSimilarityService;

    public DoctorPortalController(
            CaseService caseService,
            DoctorRepository doctorRepository,
            UserRepository userRepository,
            LocalMlInferenceService localMlInferenceService,
            GeminiAiService geminiAiService,
            com.example.Health_Data_Management.service.CaseSimilarityService caseSimilarityService) {
        this.caseService = caseService;
        this.doctorRepository = doctorRepository;
        this.userRepository = userRepository;
        this.localMlInferenceService = localMlInferenceService;
        this.geminiAiService = geminiAiService;
        this.caseSimilarityService = caseSimilarityService;
    }

    private Doctor getCurrentDoctor(Authentication authentication) {
        if (authentication == null) return null;
        String email = authentication.getName();
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) return null;
        return doctorRepository.findByUserId(user.getId()).orElse(null);
    }

    // ---------------------------------------------------------
    // DOCTOR DASHBOARD & PATIENT QUEUE
    // ---------------------------------------------------------
    @GetMapping("/dashboard")
    public String dashboard(
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "priority", defaultValue = "ALL") String priority,
            @RequestParam(value = "status", defaultValue = "ALL") String status,
            Authentication authentication,
            Model model) {

        Doctor doctor = getCurrentDoctor(authentication);
        List<MedicalCase> queueCases = caseService.getAllCasesForQueue(search, priority, status);

        long totalSubmitted = caseService.getCountByStatus(CaseStatus.SUBMITTED);
        long totalUnderReview = caseService.getCountByStatus(CaseStatus.UNDER_REVIEW);
        long totalVerified = caseService.getCountByStatus(CaseStatus.VERIFIED);
        long totalHighPriority = caseService.getCountByPriority(CasePriority.HIGH) + caseService.getCountByPriority(CasePriority.CRITICAL);

        model.addAttribute("doctor", doctor);
        model.addAttribute("cases", queueCases);
        model.addAttribute("search", search);
        model.addAttribute("selectedPriority", priority);
        model.addAttribute("selectedStatus", status);

        model.addAttribute("statTotalQueue", totalSubmitted + totalUnderReview);
        model.addAttribute("statHighPriority", totalHighPriority);
        model.addAttribute("statPendingReview", totalSubmitted);
        model.addAttribute("statVerified", totalVerified);
        model.addAttribute("todayDate", java.time.LocalDate.now().toString());

        return "doctor/dashboard";
    }

    // ---------------------------------------------------------
    // VIEW PATIENT CASE
    // ---------------------------------------------------------
    @GetMapping("/case/{id}")
    public String viewCase(
            @PathVariable("id") Long caseId,
            Authentication authentication,
            Model model) {

        Doctor doctor = getCurrentDoctor(authentication);
        MedicalCase medicalCase = caseService.getCaseById(caseId);
        if (medicalCase == null) {
            return "redirect:/doctor/dashboard";
        }

        // Move to UNDER_REVIEW if currently SUBMITTED
        if (medicalCase.getStatus() == CaseStatus.SUBMITTED) {
            medicalCase = caseService.doctorReviewCase(caseId, doctor);
        }

        // ML & Gemini Clinical Insights
        int age = (medicalCase.getPatient() != null && medicalCase.getPatient().getAge() != null)
                ? medicalCase.getPatient().getAge().intValue() : 35;
        String gender = (medicalCase.getPatient() != null && medicalCase.getPatient().getGender() != null)
                ? medicalCase.getPatient().getGender() : "Male";

        List<String> symptoms = new ArrayList<>();
        if (medicalCase.getChiefComplaint() != null) symptoms.add(medicalCase.getChiefComplaint());
        if (medicalCase.getAssociatedSymptoms() != null) symptoms.add(medicalCase.getAssociatedSymptoms());
        if (medicalCase.getPatientStatement() != null) symptoms.add(medicalCase.getPatientStatement());

        LocalMlInferenceService.MlInferenceResult mlResult = localMlInferenceService.predict(age, gender, symptoms);
        model.addAttribute("mlPrediction", mlResult);

        // Retrieve similar / related past cases from other patients
        List<com.example.Health_Data_Management.dto.SimilarCaseDto> similarCases =
                caseSimilarityService.findSimilarCases(caseId, 4);
        model.addAttribute("similarCases", similarCases);

        // Filter out noisy flat ML indications (< 40%) so AI thinks on broad clinical picture rather than anchoring on COVID-19 8%
        String topMl = (mlResult != null && mlResult.getTopProbability() >= 0.40) ? mlResult.getTopDisease() : null;
        double topMlProb = (mlResult != null && mlResult.getTopProbability() >= 0.40) ? mlResult.getTopProbability() : 0.0;

        // Generate broad-picture AI clinical summary comparing against other patients
        String aiInsights = geminiAiService.generateBroadClinicalSummary(
                medicalCase,
                similarCases,
                topMl,
                topMlProb
        );
        model.addAttribute("aiClinicalInsights", aiInsights);

        // Decision-support AI Suggestion (Accept / Edit / Reject)
        com.example.Health_Data_Management.dto.AiSuggestionResponse aiSuggestion =
                geminiAiService.generateStructuredSuggestion(medicalCase, similarCases);
        model.addAttribute("aiSuggestion", aiSuggestion);

        model.addAttribute("doctor", doctor);
        model.addAttribute("medicalCase", medicalCase);
        model.addAttribute("patient", medicalCase.getPatient());
        model.addAttribute("documents", medicalCase.getDocuments());
        model.addAttribute("answers", medicalCase.getAnswers());
        model.addAttribute("conversationMessages", caseService.getConversationHistory(caseId));

        return "doctor/patient-case";
    }

    // ---------------------------------------------------------
    // EDIT CLINICAL SUMMARY (DOCTOR EDITING)
    // ---------------------------------------------------------
    @PostMapping("/case/{id}/edit")
    public String editCase(
            @PathVariable("id") Long caseId,
            @RequestParam(value = "chiefComplaint", required = false) String chiefComplaint,
            @RequestParam(value = "patientStatement", required = false) String patientStatement,
            @RequestParam(value = "pastMedicalHistory", required = false) String pastMedicalHistory,
            @RequestParam(value = "currentMedication", required = false) String currentMedication,
            @RequestParam(value = "allergies", required = false) String allergies,
            @RequestParam(value = "investigations", required = false) String investigations,
            @RequestParam(value = "doctorClinicalNotes", required = false) String doctorClinicalNotes,
            @RequestParam(value = "priority", defaultValue = "HIGH") String priorityStr,
            @RequestParam(value = "diagnosis", required = false) String diagnosis,
            @RequestParam(value = "treatment", required = false) String treatment,
            @RequestParam(value = "vitals", required = false) String vitals,
            @RequestParam(value = "outcome", required = false) String outcome) {

        CasePriority priority = CasePriority.NORMAL;
        try {
            priority = CasePriority.valueOf(priorityStr.toUpperCase());
        } catch (Exception ignored) {}

        caseService.doctorEditCase(caseId, chiefComplaint, patientStatement, pastMedicalHistory,
                currentMedication, allergies, investigations, doctorClinicalNotes, priority,
                diagnosis, treatment, vitals, outcome);

        return "redirect:/doctor/case/" + caseId + "?edited=true";
    }

    @PostMapping("/case/{id}/verify")
    public String verifyCase(
            @PathVariable("id") Long caseId,
            @RequestParam(value = "chiefComplaint", required = false) String chiefComplaint,
            @RequestParam(value = "patientStatement", required = false) String patientStatement,
            @RequestParam(value = "pastMedicalHistory", required = false) String pastMedicalHistory,
            @RequestParam(value = "currentMedication", required = false) String currentMedication,
            @RequestParam(value = "allergies", required = false) String allergies,
            @RequestParam(value = "investigations", required = false) String investigations,
            @RequestParam(value = "doctorClinicalNotes", required = false) String doctorClinicalNotes,
            @RequestParam(value = "doctorNotes", required = false) String doctorNotes,
            @RequestParam(value = "priority", defaultValue = "NORMAL") String priorityStr,
            @RequestParam(value = "diagnosis", required = false) String diagnosis,
            @RequestParam(value = "treatment", required = false) String treatment,
            @RequestParam(value = "vitals", required = false) String vitals,
            @RequestParam(value = "outcome", required = false) String outcome,
            Authentication authentication) {

        String finalNotes = (doctorClinicalNotes != null && !doctorClinicalNotes.trim().isEmpty()) ?
                doctorClinicalNotes : doctorNotes;

        if (chiefComplaint != null) {
            CasePriority priority = CasePriority.NORMAL;
            try {
                priority = CasePriority.valueOf(priorityStr.toUpperCase());
            } catch (Exception ignored) {}
            caseService.doctorEditCase(caseId, chiefComplaint, patientStatement, pastMedicalHistory,
                    currentMedication, allergies, investigations, finalNotes, priority,
                    diagnosis, treatment, vitals, outcome);
        }

        Doctor doctor = getCurrentDoctor(authentication);
        String name = (doctor != null && doctor.getUser() != null) ? doctor.getUser().getName() : "Dr. Ananya Roy";
        String prefix = name.startsWith("Dr.") ? "" : "Dr. ";
        String qual = (doctor != null && doctor.getQualification() != null) ? " (" + doctor.getQualification() + ")" : ", MD";
        String doctorName = prefix + name + qual;

        caseService.doctorVerifyCase(caseId, doctorName, finalNotes, diagnosis, treatment, vitals, outcome);

        return "redirect:/doctor/case/" + caseId + "?verified=true";
    }

    // ---------------------------------------------------------
    // LOG AI SUGGESTION AUDIT (ACCEPT / EDIT / REJECT)
    // ---------------------------------------------------------
    @PostMapping("/case/{id}/suggestion-audit")
    @ResponseBody
    public org.springframework.http.ResponseEntity<?> logSuggestionAudit(
            @PathVariable("id") Long caseId,
            @RequestBody com.example.Health_Data_Management.entity.CaseSuggestionAudit audit,
            Authentication authentication) {

        Doctor doctor = getCurrentDoctor(authentication);
        if (doctor != null) {
            audit.setDoctorId(doctor.getId());
            if (doctor.getUser() != null) {
                audit.setDoctorName(doctor.getUser().getName());
            }
        }
        audit.setMedicalCaseId(caseId);

        com.example.Health_Data_Management.entity.CaseSuggestionAudit saved =
                caseService.logSuggestionAudit(audit);

        return org.springframework.http.ResponseEntity.ok(saved);
    }
}
