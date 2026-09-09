package com.example.Health_Data_Management.fhir;

import java.util.*;

public class FhirConditionResource {
    private String resourceType = "Condition";
    private String id;
    private Map<String, Object> clinicalStatus = new HashMap<>();
    private Map<String, Object> code = new HashMap<>();
    private Map<String, Object> subject = new HashMap<>();

    public static FhirConditionResource create(String id, String conditionName, String patientId, String patientName) {
        FhirConditionResource c = new FhirConditionResource();
        c.id = id;
        c.clinicalStatus.put("coding", List.of(Map.of("code", "active", "system", "http://terminology.hl7.org/CodeSystem/condition-clinical")));
        c.code.put("text", conditionName);
        c.subject.put("reference", "Patient/" + patientId);
        c.subject.put("display", patientName);
        return c;
    }

    public String getResourceType() { return resourceType; }
    public String getId() { return id; }
    public Map<String, Object> getClinicalStatus() { return clinicalStatus; }
    public Map<String, Object> getCode() { return code; }
    public Map<String, Object> getSubject() { return subject; }
}
