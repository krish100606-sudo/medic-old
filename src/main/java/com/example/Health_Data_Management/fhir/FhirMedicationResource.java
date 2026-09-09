package com.example.Health_Data_Management.fhir;

import java.util.*;

public class FhirMedicationResource {
    private String resourceType = "MedicationRequest";
    private String id;
    private String status = "active";
    private Map<String, Object> medicationCodeableConcept = new HashMap<>();
    private Map<String, Object> subject = new HashMap<>();

    public static FhirMedicationResource create(String id, String drugName, String patientId) {
        FhirMedicationResource m = new FhirMedicationResource();
        m.id = id;
        m.medicationCodeableConcept.put("text", drugName);
        m.subject.put("reference", "Patient/" + patientId);
        return m;
    }

    public String getResourceType() { return resourceType; }
    public String getId() { return id; }
    public String getStatus() { return status; }
    public Map<String, Object> getMedicationCodeableConcept() { return medicationCodeableConcept; }
    public Map<String, Object> getSubject() { return subject; }
}
