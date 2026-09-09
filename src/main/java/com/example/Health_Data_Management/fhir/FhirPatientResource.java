package com.example.Health_Data_Management.fhir;

import com.example.Health_Data_Management.entity.Patient;
import java.util.*;

/**
 * FHIR R4 Patient Resource representation conforming to ABDM (Ayushman Bharat Digital Mission) standards.
 */
public class FhirPatientResource {
    private String resourceType = "Patient";
    private String id;
    private List<Map<String, Object>> identifier = new ArrayList<>();
    private List<Map<String, Object>> name = new ArrayList<>();
    private String gender;
    private String birthDate;
    private List<Map<String, Object>> telecom = new ArrayList<>();
    private List<Map<String, Object>> address = new ArrayList<>();

    public static FhirPatientResource fromPatient(Patient p) {
        FhirPatientResource res = new FhirPatientResource();
        if (p == null) return res;

        res.id = "pat-" + p.getId();
        res.gender = p.getGender() != null ? p.getGender().toLowerCase() : "unknown";

        // ABHA Identifier
        Map<String, Object> abhaId = new HashMap<>();
        abhaId.put("system", "https://healthid.ndhm.gov.in");
        abhaId.put("type", Map.of("coding", List.of(Map.of("system", "http://terminology.hl7.org/CodeSystem/v2-0203", "code", "MR", "display", "ABHA Number"))));
        abhaId.put("value", p.getAbhaId() != null ? p.getAbhaId() : "91-2026-" + p.getId() + "-8899");
        res.identifier.add(abhaId);

        // Name
        String fullName = p.getUser() != null && p.getUser().getName() != null ? p.getUser().getName() : "Patient";
        Map<String, Object> nameMap = new HashMap<>();
        nameMap.put("use", "official");
        nameMap.put("text", fullName);
        nameMap.put("family", fullName.contains(" ") ? fullName.substring(fullName.lastIndexOf(" ") + 1) : "");
        nameMap.put("given", List.of(fullName.contains(" ") ? fullName.substring(0, fullName.indexOf(" ")) : fullName));
        res.name.add(nameMap);

        // Telecom
        if (p.getPhone() != null) {
            Map<String, Object> phoneMap = new HashMap<>();
            phoneMap.put("system", "phone");
            phoneMap.put("value", p.getPhone());
            phoneMap.put("use", "mobile");
            res.telecom.add(phoneMap);
        }

        // Address
        if (p.getAddress() != null) {
            Map<String, Object> addrMap = new HashMap<>();
            addrMap.put("use", "home");
            addrMap.put("text", p.getAddress());
            addrMap.put("country", "IND");
            res.address.add(addrMap);
        }

        return res;
    }

    public String getResourceType() { return resourceType; }
    public String getId() { return id; }
    public List<Map<String, Object>> getIdentifier() { return identifier; }
    public List<Map<String, Object>> getName() { return name; }
    public String getGender() { return gender; }
    public String getBirthDate() { return birthDate; }
    public List<Map<String, Object>> getTelecom() { return telecom; }
    public List<Map<String, Object>> getAddress() { return address; }
}
