package com.example.Health_Data_Management.controller;



import com.example.Health_Data_Management.entity.Patient;
import com.example.Health_Data_Management.service.PatientService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/patients")
public class PatientController {

    private final PatientService patientService;

    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    // ---------------------------------------------------------
    // CREATE PATIENT PROFILE
    // POST /api/patients/user/{userId}
    // ---------------------------------------------------------

    @PostMapping("/user/{userId}")
    public ResponseEntity<Patient> createPatient(
            @PathVariable Long userId) {

        Patient patient = patientService.createPatient(userId);

        return ResponseEntity.ok(patient);
    }


    // ---------------------------------------------------------
    // GET PATIENT BY ID
    // GET /api/patients/{id}
    // ---------------------------------------------------------

    @GetMapping("/{id}")
    public ResponseEntity<Patient> getPatient(
            @PathVariable Long id) {

        return patientService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }


    // ---------------------------------------------------------
    // GET PATIENT BY USER ID
    // GET /api/patients/user/{userId}
    // ---------------------------------------------------------

    @GetMapping("/user/{userId}")
    public ResponseEntity<Patient> getPatientByUserId(
            @PathVariable Long userId) {

        return patientService.findByUserId(userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }


    // ---------------------------------------------------------
    // GET ALL PATIENTS
    // GET /api/patients
    // ---------------------------------------------------------

    @GetMapping
    public ResponseEntity<List<Patient>> getAllPatients() {

        return ResponseEntity.ok(
                patientService.getAllPatients()
        );
    }


    // ---------------------------------------------------------
    // UPDATE PATIENT
    // PUT /api/patients/{id}
    // ---------------------------------------------------------

    @PutMapping("/{id}")
    public ResponseEntity<Patient> updatePatient(
            @PathVariable Long id,
            @RequestBody Patient patient) {

        patient.setId(id);

        Patient updatedPatient =
                patientService.updatePatient(patient);

        return ResponseEntity.ok(updatedPatient);
    }


    // ---------------------------------------------------------
    // DELETE PATIENT
    // DELETE /api/patients/{id}
    // ---------------------------------------------------------

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deletePatient(
            @PathVariable Long id) {

        patientService.deletePatient(id);

        return ResponseEntity.ok(
                "Patient deleted successfully"
        );
    }


    // ---------------------------------------------------------
    // FIND BY BLOOD GROUP
    // GET /api/patients/blood-group/{bloodGroup}
    // ---------------------------------------------------------

    @GetMapping("/blood-group/{bloodGroup}")
    public ResponseEntity<List<Patient>> findByBloodGroup(
            @PathVariable String bloodGroup) {

        return ResponseEntity.ok(
                patientService.findByBloodGroup(bloodGroup)
        );
    }


    // ---------------------------------------------------------
    // FIND BY GENDER
    // GET /api/patients/gender/{gender}
    // ---------------------------------------------------------

    @GetMapping("/gender/{gender}")
    public ResponseEntity<List<Patient>> findByGender(
            @PathVariable String gender) {

        return ResponseEntity.ok(
                patientService.findByGender(gender)
        );
    }
}
