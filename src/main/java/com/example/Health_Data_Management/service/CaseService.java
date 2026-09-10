package com.example.Health_Data_Management.service;

import com.example.Health_Data_Management.entity.*;
import com.example.Health_Data_Management.repository.CaseAnswerRepository;
import com.example.Health_Data_Management.repository.ConversationMessageRepository;
import com.example.Health_Data_Management.repository.MedicalCaseRepository;
import com.example.Health_Data_Management.repository.MedicalDocumentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class CaseService {

    private final MedicalCaseRepository caseRepository;
    private final CaseAnswerRepository answerRepository;
    private final MedicalDocumentRepository documentRepository;
    private final ConversationMessageRepository conversationMessageRepository;
    private final RedFlagService redFlagService;
    private final SummaryService summaryService;
    private final OCRService ocrService;
    private final DashavidhaService dashavidhaService;
    private final DrugInteractionService drugInteractionService;
    private final com.example.Health_Data_Management.repository.CaseSuggestionAuditRepository suggestionAuditRepository;

    public CaseService(
            MedicalCaseRepository caseRepository,
            CaseAnswerRepository answerRepository,
            MedicalDocumentRepository documentRepository,
            ConversationMessageRepository conversationMessageRepository,
            RedFlagService redFlagService,
            SummaryService summaryService,
            OCRService ocrService,
            DashavidhaService dashavidhaService,
            DrugInteractionService drugInteractionService,
            com.example.Health_Data_Management.repository.CaseSuggestionAuditRepository suggestionAuditRepository) {
        this.caseRepository = caseRepository;
        this.answerRepository = answerRepository;
        this.documentRepository = documentRepository;
        this.conversationMessageRepository = conversationMessageRepository;
        this.redFlagService = redFlagService;
        this.summaryService = summaryService;
        this.ocrService = ocrService;
        this.dashavidhaService = dashavidhaService;
        this.drugInteractionService = drugInteractionService;
        this.suggestionAuditRepository = suggestionAuditRepository;
    }

    @Transactional
    public MedicalCase getOrCreateDraftCase(Patient patient) {
        Optional<MedicalCase> existingDraft = caseRepository.findFirstByPatientIdAndStatusOrderByCreatedAtDesc(
                patient.getId(), CaseStatus.DRAFT);
        if (existingDraft.isPresent()) {
            return existingDraft.get();
        }

        return createFreshDraftCase(patient);
    }

    @Transactional
    public MedicalCase startNewDraftCase(Patient patient) {
        // Clean up any existing unsubmitted drafts and their old messages
        List<MedicalCase> cases = caseRepository.findByPatientIdOrderByCreatedAtDesc(patient.getId());
        if (cases != null) {
            for (MedicalCase c : cases) {
                if (c.getStatus() == CaseStatus.DRAFT) {
                    conversationMessageRepository.deleteByMedicalCaseId(c.getId());
                    answerRepository.deleteByMedicalCaseId(c.getId());
                    caseRepository.delete(c);
                }
            }
        }

        return createFreshDraftCase(patient);
    }

    @Transactional
    public void clearConversationHistory(Long caseId) {
        conversationMessageRepository.deleteByMedicalCaseId(caseId);
    }

    private MedicalCase createFreshDraftCase(Patient patient) {
        MedicalCase newCase = new MedicalCase(patient);
        String uniqueNum = String.format("MK-%d-%04d", System.currentTimeMillis() % 1000000, (int)(Math.random() * 9000 + 1000));
        newCase.setCaseNumber(uniqueNum);
        return caseRepository.save(newCase);
    }

    @Transactional
    public CaseAnswer saveOrUpdateAnswer(Long caseId, String questionCode, String questionText, String answerText, InputType inputType) {
        MedicalCase medicalCase = caseRepository.findById(caseId)
                .orElseThrow(() -> new RuntimeException("Case not found: " + caseId));

        Optional<CaseAnswer> existingAnswer = answerRepository.findByMedicalCaseIdAndQuestionCode(caseId, questionCode);
        CaseAnswer answer;
        if (existingAnswer.isPresent()) {
            answer = existingAnswer.get();
            answer.setAnswerText(answerText);
            answer.setInputType(inputType != null ? inputType : InputType.TEXT);
        } else {
            answer = new CaseAnswer(medicalCase, questionCode, questionText, answerText, inputType);
        }

        CaseAnswer saved = answerRepository.save(answer);

        // Map answer to specific case fields
        mapAnswerToCaseField(medicalCase, questionCode, answerText);
        caseRepository.save(medicalCase);

        // Record to conversation history
        String inputMethodStr = inputType != null ? inputType.name() : "TEXT";
        String lang = medicalCase.getPatient() != null && medicalCase.getPatient().getPreferredLanguage() != null
                ? medicalCase.getPatient().getPreferredLanguage() : "English";
        conversationMessageRepository.save(new ConversationMessage(
                medicalCase, "PATIENT", answerText, "ANSWER", questionCode, lang, inputMethodStr));

        return saved;
    }

    private void mapAnswerToCaseField(MedicalCase c, String code, String text) {
        if (text == null) return;
        switch (code) {
            case "Q_CHIEF_COMPLAINT" -> c.setChiefComplaint(text);
            case "Q_STATEMENT" -> c.setPatientStatement(text);
            case "Q_ONSET" -> c.setOnset(text);
            case "Q_LOCATION" -> c.setLocation(text);
            case "Q_SEVERITY" -> c.setSeverity(text);
            case "Q_ASSOCIATED_SYMPTOMS" -> c.setAssociatedSymptoms(text);
            case "Q_ADAPTIVE_EXERTION" -> c.setAssociatedSymptoms((c.getAssociatedSymptoms() != null ? c.getAssociatedSymptoms() + "; " : "") + "Exertion: " + text);
            case "Q_ADAPTIVE_CARDIAC_RISK" -> c.setPastMedicalHistory((c.getPastMedicalHistory() != null ? c.getPastMedicalHistory() + "; " : "") + "Cardiac Risk: " + text);
            case "Q_ADAPTIVE_FEVER_PATTERN" -> c.setAssociatedSymptoms((c.getAssociatedSymptoms() != null ? c.getAssociatedSymptoms() + "; " : "") + "Fever Pattern: " + text);
            case "Q_ADAPTIVE_TRAVEL" -> c.setPersonalHistory((c.getPersonalHistory() != null ? c.getPersonalHistory() + "; " : "") + "Travel History: " + text);
            case "Q_ADAPTIVE_HEADACHE_TYPE" -> c.setAssociatedSymptoms((c.getAssociatedSymptoms() != null ? c.getAssociatedSymptoms() + "; " : "") + "Headache Type: " + text);
            case "Q_ADAPTIVE_ABDOMEN_RELATION" -> c.setAssociatedSymptoms((c.getAssociatedSymptoms() != null ? c.getAssociatedSymptoms() + "; " : "") + "Food Relation: " + text);
            case "Q_PAST_DISEASES" -> c.setPastMedicalHistory(text);
            case "Q_SURGERIES" -> c.setSurgicalHistory(text);
            case "Q_MEDICATIONS" -> c.setCurrentMedication(text);
            case "Q_ALLERGIES" -> c.setAllergies(text);
            case "Q_FAMILY_HISTORY" -> c.setFamilyHistory(text);
            case "Q_PERSONAL_HISTORY" -> c.setPersonalHistory(text);
            case "Q_INVESTIGATIONS" -> c.setInvestigations(text);
        }
    }

    @Transactional
    public ConversationMessage logConversationMessage(Long caseId, String sender, String text, String type, String questionCode, String language, String inputMethod) {
        MedicalCase medicalCase = caseRepository.findById(caseId).orElse(null);
        if (medicalCase == null) return null;
        ConversationMessage msg = new ConversationMessage(medicalCase, sender, text, type, questionCode, language, inputMethod);
        return conversationMessageRepository.save(msg);
    }

    public List<ConversationMessage> getConversationHistory(Long caseId) {
        return conversationMessageRepository.findByMedicalCaseIdOrderByCreatedAtAsc(caseId);
    }

    @Transactional
    public MedicalDocument addAndProcessDocument(Long caseId, Long patientId, String filename, String originalFilename, String fileType, DocumentType docType) {
        MedicalCase medicalCase = caseRepository.findById(caseId).orElse(null);
        Patient patient = medicalCase != null ? medicalCase.getPatient() : null;

        MedicalDocument doc = new MedicalDocument(patient, medicalCase, filename, originalFilename, fileType, docType);
        ocrService.applyOcrToDocument(doc);
        return documentRepository.save(doc);
    }

    @Transactional
    public MedicalCase generateSummaryAndEvaluateRedFlags(Long caseId) {
        MedicalCase medicalCase = caseRepository.findById(caseId)
                .orElseThrow(() -> new RuntimeException("Case not found: " + caseId));

        List<MedicalDocument> docs = documentRepository.findByMedicalCaseId(caseId);

        // Evaluate deterministic red-flags
        RedFlagService.RedFlagEvaluation evaluation = redFlagService.evaluate(medicalCase);
        medicalCase.setPriority(evaluation.getPriority());
        medicalCase.setRedFlagsDetected(evaluation.isRedFlagsDetected());
        medicalCase.setPriorityReason(evaluation.getReason());

        // Dashavidha Pariksha
        DashavidhaService.DashavidhaEvaluation ayush = dashavidhaService.evaluate(medicalCase);
        medicalCase.setPrakritiType(ayush.getPrakriti());
        medicalCase.setDoshaImbalance(ayush.getVikriti());
        medicalCase.setDashavidhaAssessment(ayush.getClinicalNotesAyush());

        // Drug-Drug Interactions
        List<DrugInteraction> interactions = drugInteractionService.checkInteractions(medicalCase.getCurrentMedication());
        if (!interactions.isEmpty()) {
            StringBuilder diSb = new StringBuilder();
            for (DrugInteraction di : interactions) {
                diSb.append("[").append(di.getSeverity()).append("] ").append(di.getDrugA()).append(" + ").append(di.getDrugB())
                    .append(": ").append(di.getEffect()).append(" (Rec: ").append(di.getClinicalRecommendation()).append(")\n");
            }
            medicalCase.setDrugInteractionsJson(diSb.toString());
        }

        // Generate structured summary & timeline
        String structuredSummary = summaryService.generateStructuredSummary(medicalCase, docs);
        String timeline = summaryService.generateMedicalTimeline(medicalCase, docs);

        medicalCase.setStructuredSummary(structuredSummary);
        medicalCase.setMedicalTimeline(timeline);

        return caseRepository.save(medicalCase);
    }

    @Transactional
    public MedicalCase submitCase(Long caseId) {
        MedicalCase medicalCase = generateSummaryAndEvaluateRedFlags(caseId);

        if (medicalCase.getTokenNumber() == null) {
            Integer maxToken = caseRepository.findMaxTokenNumber();
            medicalCase.setTokenNumber(maxToken != null ? maxToken + 1 : 104);
        }

        if (medicalCase.getCaseNumber() == null || medicalCase.getCaseNumber().startsWith("DRAFT-")) {
            medicalCase.setCaseNumber("MK-" + LocalDateTime.now().getYear() + "-" + (1000 + medicalCase.getTokenNumber()));
        }

        medicalCase.setStatus(CaseStatus.SUBMITTED);
        medicalCase.setSubmittedAt(LocalDateTime.now());
        return caseRepository.save(medicalCase);
    }

    @Transactional
    public MedicalCase doctorReviewCase(Long caseId, Doctor doctor) {
        MedicalCase medicalCase = caseRepository.findById(caseId)
                .orElseThrow(() -> new RuntimeException("Case not found: " + caseId));

        if (medicalCase.getStatus() == CaseStatus.SUBMITTED) {
            medicalCase.setStatus(CaseStatus.UNDER_REVIEW);
        }
        if (doctor != null && medicalCase.getDoctor() == null) {
            medicalCase.setDoctor(doctor);
        }
        return caseRepository.save(medicalCase);
    }

    @Transactional
    public MedicalCase doctorEditCase(Long caseId, String chiefComplaint, String history, String pastHistory,
                                       String medications, String allergies, String investigations,
                                       String doctorNotes, CasePriority priority) {
        return doctorEditCase(caseId, chiefComplaint, history, pastHistory, medications, allergies, investigations, doctorNotes, priority, null, null, null, null);
    }

    @Transactional
    public MedicalCase doctorEditCase(Long caseId, String chiefComplaint, String history, String pastHistory,
                                       String medications, String allergies, String investigations,
                                       String doctorNotes, CasePriority priority,
                                       String diagnosis, String treatment, String vitals, String outcome) {
        MedicalCase medicalCase = caseRepository.findById(caseId)
                .orElseThrow(() -> new RuntimeException("Case not found: " + caseId));

        if (chiefComplaint != null) medicalCase.setChiefComplaint(chiefComplaint);
        if (history != null) medicalCase.setPatientStatement(history);
        if (pastHistory != null) medicalCase.setPastMedicalHistory(pastHistory);
        if (medications != null) medicalCase.setCurrentMedication(medications);
        if (allergies != null) medicalCase.setAllergies(allergies);
        if (investigations != null) medicalCase.setInvestigations(investigations);
        if (doctorNotes != null) medicalCase.setDoctorClinicalNotes(doctorNotes);
        if (priority != null) medicalCase.setPriority(priority);
        if (diagnosis != null && !diagnosis.isBlank()) medicalCase.setDiagnosis(diagnosis);
        if (treatment != null && !treatment.isBlank()) medicalCase.setTreatment(treatment);
        if (vitals != null && !vitals.isBlank()) medicalCase.setVitals(vitals);
        if (outcome != null && !outcome.isBlank()) medicalCase.setOutcome(outcome);

        medicalCase.setDoctorEdited(true);

        List<MedicalDocument> docs = documentRepository.findByMedicalCaseId(caseId);
        medicalCase.setStructuredSummary(summaryService.generateStructuredSummary(medicalCase, docs));

        return caseRepository.save(medicalCase);
    }

    @Transactional
    public MedicalCase doctorVerifyCase(Long caseId, String doctorName, String doctorNotes) {
        return doctorVerifyCase(caseId, doctorName, doctorNotes, null, null, null, null);
    }

    @Transactional
    public MedicalCase doctorVerifyCase(Long caseId, String doctorName, String doctorNotes,
                                        String diagnosis, String treatment, String vitals, String outcome) {
        MedicalCase medicalCase = caseRepository.findById(caseId)
                .orElseThrow(() -> new RuntimeException("Case not found: " + caseId));

        medicalCase.setStatus(CaseStatus.VERIFIED);
        medicalCase.setVerifiedByDoctor(doctorName != null ? doctorName : "Dr. Ananya Roy, MD");
        medicalCase.setVerifiedAt(LocalDateTime.now());
        if (doctorNotes != null && !doctorNotes.isBlank()) {
            medicalCase.setDoctorClinicalNotes(doctorNotes);
        }
        if (diagnosis != null && !diagnosis.isBlank()) {
            medicalCase.setDiagnosis(diagnosis);
        }
        if (treatment != null && !treatment.isBlank()) {
            medicalCase.setTreatment(treatment);
        }
        if (vitals != null && !vitals.isBlank()) {
            medicalCase.setVitals(vitals);
        }
        if (outcome != null && !outcome.isBlank()) {
            medicalCase.setOutcome(outcome);
        }

        return caseRepository.save(medicalCase);
    }

    @Transactional
    public com.example.Health_Data_Management.entity.CaseSuggestionAudit logSuggestionAudit(com.example.Health_Data_Management.entity.CaseSuggestionAudit audit) {
        return suggestionAuditRepository.save(audit);
    }

    public MedicalCase getCaseById(Long id) {
        return caseRepository.findById(id).orElse(null);
    }

    @Transactional
    public MedicalCase saveCase(MedicalCase medicalCase) {
        return caseRepository.save(medicalCase);
    }

    public List<MedicalCase> getCasesForPatient(Long patientId) {
        return caseRepository.findByPatientIdOrderByCreatedAtDesc(patientId);
    }

    public List<MedicalCase> getAllCasesForQueue(String keyword, String priorityFilter, String statusFilter) {
        List<MedicalCase> list;
        if (keyword != null && !keyword.trim().isEmpty()) {
            list = caseRepository.searchCases(keyword.trim());
        } else {
            list = caseRepository.findAll();
        }

        return list.stream()
                .filter(c -> c.getStatus() != CaseStatus.DRAFT)
                .filter(c -> {
                    if (priorityFilter == null || priorityFilter.isEmpty() || "ALL".equalsIgnoreCase(priorityFilter)) {
                        return true;
                    }
                    return c.getPriority().name().equalsIgnoreCase(priorityFilter);
                })
                .filter(c -> {
                    if (statusFilter == null || statusFilter.isEmpty() || "ALL".equalsIgnoreCase(statusFilter)) {
                        return true;
                    }
                    return c.getStatus().name().equalsIgnoreCase(statusFilter);
                })
                .sorted((a, b) -> {
                    // Sort by priority (CRITICAL/HIGH first) then token number
                    int prioCompare = Integer.compare(getPrioRank(b.getPriority()), getPrioRank(a.getPriority()));
                    if (prioCompare != 0) return prioCompare;
                    Integer tokA = a.getTokenNumber() != null ? a.getTokenNumber() : Integer.valueOf(999999);
                    Integer tokB = b.getTokenNumber() != null ? b.getTokenNumber() : Integer.valueOf(999999);
                    return tokA.compareTo(tokB);
                })
                .toList();
    }

    private int getPrioRank(CasePriority p) {
        if (p == CasePriority.CRITICAL) return 3;
        if (p == CasePriority.HIGH) return 2;
        return 1;
    }

    public long getCountByStatus(CaseStatus status) {
        return caseRepository.countByStatus(status);
    }

    public long getCountByPriority(CasePriority priority) {
        return caseRepository.countByPriority(priority);
    }
}
