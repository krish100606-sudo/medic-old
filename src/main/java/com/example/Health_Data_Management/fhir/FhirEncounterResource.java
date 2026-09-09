package com.example.Health_Data_Management.fhir;

import com.example.Health_Data_Management.entity.MedicalCase;
import java.util.*;

public class FhirEncounterResource {
    private String resourceType = "Encounter";
    private String id;
    private String status; // planned | arrived | triaged | in-progress | finished
    private Map<String, Object> subject = new HashMap<>();
    private List<Map<String, Object>> reasonCode = new ArrayList<>();
    private String priority;

    public static FhirEncounterResource fromMedicalCase(MedicalCase c) {
        FhirEncounterResource enc = new FhirEncounterResource();
        if (c == null) return enc;

        enc.id = "enc-" + c.getId();
        enc.status = c.getStatus() != null ? c.getStatus().name().toLowerCase() : "in-progress";
        enc.priority = c.getPriority() != null ? c.getPriority().name() : "NORMAL";

        if (c.getPatient() != null) {
            enc.subject.put("reference", "Patient/pat-" + c.getPatient().getId());
            enc.subject.put("display", c.getPatient().getName());
        }

        if (c.getChiefComplaint() != null) {
            Map<String, Object> reason = new HashMap<>();
            reason.put("text", c.getChiefComplaint());
            enc.reasonCode.add(reason);
        }

        return enc;
    }

    public String getResourceType() { return resourceType; }
    public String getId() { return id; }
    public String getStatus() { return status; }
    public Map<String, Object> getSubject() { return subject; }
    public List<Map<String, Object>> getReasonCode() { return reasonCode; }
    public String getPriority() { return priority; }
}
