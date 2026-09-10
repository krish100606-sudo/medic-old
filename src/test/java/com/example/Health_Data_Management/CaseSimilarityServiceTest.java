package com.example.Health_Data_Management;

import com.example.Health_Data_Management.dto.SimilarCaseDto;
import com.example.Health_Data_Management.entity.CaseStatus;
import com.example.Health_Data_Management.entity.MedicalCase;
import com.example.Health_Data_Management.entity.Patient;
import com.example.Health_Data_Management.repository.MedicalCaseRepository;
import com.example.Health_Data_Management.service.CaseSimilarityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class CaseSimilarityServiceTest {

    private MedicalCaseRepository caseRepository;
    private CaseSimilarityService similarityService;

    @BeforeEach
    void setUp() {
        caseRepository = Mockito.mock(MedicalCaseRepository.class);
        similarityService = new CaseSimilarityService(caseRepository);
    }

    @Test
    void testFindSimilarCasesReturnsTopMatch() {
        // Target case: 45 yo male with acute chest pain and shortness of breath
        Patient targetPatient = new Patient();
        targetPatient.setAge(45);
        targetPatient.setGender("Male");

        MedicalCase targetCase = new MedicalCase();
        targetCase.setId(101L);
        targetCase.setCaseNumber("MK-TARGET");
        targetCase.setPatient(targetPatient);
        targetCase.setChiefComplaint("Acute chest pain and shortness of breath");
        targetCase.setSymptoms("Chest pain, dyspnea, sweating");
        targetCase.setPastMedicalHistory("Type 2 Diabetes");

        // Candidate 1: 52 yo male with ACS (High similarity)
        Patient cand1Patient = new Patient();
        cand1Patient.setAge(52);
        cand1Patient.setGender("Male");

        MedicalCase cand1 = new MedicalCase();
        cand1.setId(201L);
        cand1.setCaseNumber("MK-BENCH-01");
        cand1.setPatient(cand1Patient);
        cand1.setStatus(CaseStatus.VERIFIED);
        cand1.setChiefComplaint("Severe retrosternal chest pain");
        cand1.setSymptoms("Substernal chest pain, breathlessness, diaphoresis, chest tightness");
        cand1.setDiagnosis("Acute Coronary Syndrome");
        cand1.setTreatment("Aspirin 325mg STAT, Clopidogrel 300mg STAT");
        cand1.setVitals("BP: 150/90, HR: 95");
        cand1.setOutcome("Stabilized post-PCI");
        cand1.setPastMedicalHistory("Diabetes Mellitus");

        // Candidate 2: 30 yo female with Fever (Low/irrelevant similarity)
        Patient cand2Patient = new Patient();
        cand2Patient.setAge(30);
        cand2Patient.setGender("Female");

        MedicalCase cand2 = new MedicalCase();
        cand2.setId(202L);
        cand2.setCaseNumber("MK-BENCH-02");
        cand2.setPatient(cand2Patient);
        cand2.setStatus(CaseStatus.VERIFIED);
        cand2.setChiefComplaint("High fever and body ache");
        cand2.setSymptoms("High fever, chills, body ache");
        cand2.setDiagnosis("Viral Fever");
        cand2.setTreatment("Paracetamol 650mg");
        cand2.setOutcome("Recovered");

        when(caseRepository.findById(101L)).thenReturn(Optional.of(targetCase));
        when(caseRepository.findAll()).thenReturn(List.of(targetCase, cand1, cand2));

        List<SimilarCaseDto> results = similarityService.findSimilarCases(101L, 5);

        assertNotNull(results);
        assertFalse(results.isEmpty());
        // The target case itself must be excluded
        assertTrue(results.stream().noneMatch(c -> c.getCaseId().equals(101L)));

        // Candidate 1 (ACS) should be the top match with high similarity score
        SimilarCaseDto topMatch = results.get(0);
        assertEquals(201L, topMatch.getCaseId());
        assertEquals("Acute Coronary Syndrome", topMatch.getDiagnosis());
        assertTrue(topMatch.getSimilarityScore() >= 60, "Expected score >= 60%, got: " + topMatch.getSimilarityScore());
        assertEquals("HIGH", topMatch.getMatchConfidence());
        assertFalse(topMatch.getMatchReasons().isEmpty());
    }

    @Test
    void testTargetCaseNotFoundReturnsEmpty() {
        when(caseRepository.findById(999L)).thenReturn(Optional.empty());
        List<SimilarCaseDto> results = similarityService.findSimilarCases(999L, 5);
        assertTrue(results.isEmpty());
    }
}
