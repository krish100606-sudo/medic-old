package com.example.Health_Data_Management.controller;

import com.example.Health_Data_Management.entity.MedicalCase;
import com.example.Health_Data_Management.service.AbdmIntegrationService;
import com.example.Health_Data_Management.service.CaseService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/abdm")
public class AbdmRestController {

    private final AbdmIntegrationService abdmIntegrationService;
    private final CaseService caseService;

    public AbdmRestController(AbdmIntegrationService abdmIntegrationService, CaseService caseService) {
        this.abdmIntegrationService = abdmIntegrationService;
        this.caseService = caseService;
    }

    /**
     * Returns FHIR R4 Bundle for an active case
     */
    @GetMapping("/case/{caseId}/fhir-bundle")
    public ResponseEntity<?> getFhirBundle(@PathVariable Long caseId) {
        MedicalCase medicalCase = caseService.getCaseById(caseId);
        if (medicalCase == null) {
            return ResponseEntity.notFound().build();
        }
        Map<String, Object> bundle = abdmIntegrationService.generateFhirBundle(medicalCase);
        return ResponseEntity.ok(bundle);
    }

    /**
     * Validates ABHA ID format
     */
    @GetMapping("/validate-abha")
    public ResponseEntity<?> validateAbha(@RequestParam String abhaId) {
        boolean valid = abdmIntegrationService.validateAbhaId(abhaId);
        return ResponseEntity.ok(Map.of("abhaId", abhaId, "valid", valid));
    }

    /**
     * Simulates pushing FHIR bundle to patient's ABDM Health Locker
     */
    @PostMapping("/case/{caseId}/push-health-locker")
    public ResponseEntity<?> pushToHealthLocker(@PathVariable Long caseId, @RequestParam(defaultValue = "patient@abdm") String abhaAddress) {
        MedicalCase medicalCase = caseService.getCaseById(caseId);
        if (medicalCase == null) {
            return ResponseEntity.notFound().build();
        }
        Map<String, Object> bundle = abdmIntegrationService.generateFhirBundle(medicalCase);
        Map<String, Object> result = abdmIntegrationService.pushToHealthLocker(abhaAddress, bundle);
        return ResponseEntity.ok(result);
    }
}
