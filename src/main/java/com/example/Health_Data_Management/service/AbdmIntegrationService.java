package com.example.Health_Data_Management.service;

import com.example.Health_Data_Management.entity.MedicalCase;
import com.example.Health_Data_Management.fhir.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

/**
 * ABDM (Ayushman Bharat Digital Mission) Integration Service Stub.
 * Generates FHIR R4 Bundles for ABDM Health Information Provider (HIP) exchange.
 */
@Service
public class AbdmIntegrationService {

    public Map<String, Object> generateFhirBundle(MedicalCase medicalCase) {
        Map<String, Object> bundle = new HashMap<>();
        bundle.put("resourceType", "Bundle");
        bundle.put("type", "document");
        bundle.put("timestamp", LocalDateTime.now().toString());

        List<Map<String, Object>> entries = new ArrayList<>();

        if (medicalCase != null && medicalCase.getPatient() != null) {
            FhirPatientResource patRes = FhirPatientResource.fromPatient(medicalCase.getPatient());
            entries.add(Map.of("resource", patRes));
        }

        if (medicalCase != null) {
            FhirEncounterResource encRes = FhirEncounterResource.fromMedicalCase(medicalCase);
            entries.add(Map.of("resource", encRes));

            if (medicalCase.getChiefComplaint() != null) {
                FhirConditionResource cond = FhirConditionResource.create(
                        "cond-1",
                        medicalCase.getChiefComplaint(),
                        medicalCase.getPatient() != null ? "pat-" + medicalCase.getPatient().getId() : "pat-0",
                        medicalCase.getPatient() != null ? medicalCase.getPatient().getName() : "Unknown"
                );
                entries.add(Map.of("resource", cond));
            }

            if (medicalCase.getCurrentMedication() != null && !medicalCase.getCurrentMedication().isBlank()) {
                String patientRef = medicalCase.getPatient() != null ? "pat-" + medicalCase.getPatient().getId() : "pat-0";
                FhirMedicationResource medRes = FhirMedicationResource.create("med-1", medicalCase.getCurrentMedication(), patientRef);
                entries.add(Map.of("resource", medRes));
            }
        }

        bundle.put("entry", entries);
        bundle.put("abdmStatus", "READY_FOR_DISPATCH");
        bundle.put("hipId", "IN010000012_AAM_DELHI");
        return bundle;
    }

    public Map<String, Object> pushToHealthLocker(String abhaAddress, Map<String, Object> bundle) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "SUCCESS");
        response.put("transactionId", "TXN-" + UUID.randomUUID());
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("message", "Health Record successfully queued for ABDM Health Locker delivery to " + abhaAddress);
        return response;
    }

    public boolean validateAbhaId(String abhaId) {
        if (abhaId == null || abhaId.isBlank()) return false;
        // Standard ABHA format: 14-digit number or username@abdm
        return abhaId.matches("^(\\d{2}-\\d{4}-\\d{4}-\\d{4}|\\w+@abdm|\\w+@sbx)$");
    }
}
