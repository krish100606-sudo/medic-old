package com.example.Health_Data_Management.controller;



import com.example.Health_Data_Management.entity.Doctor;
import com.example.Health_Data_Management.service.DoctorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/doctors")
public class DoctorController {

    private final DoctorService doctorService;

    public DoctorController(DoctorService doctorService) {
        this.doctorService = doctorService;
    }

    // ---------------------------------------------------------
    // CREATE DOCTOR PROFILE
    // POST /api/doctors/user/{userId}
    // ---------------------------------------------------------

    @PostMapping("/user/{userId}")
    public ResponseEntity<Doctor> createDoctor(
            @PathVariable Long userId) {

        Doctor doctor = doctorService.createDoctor(userId);

        return ResponseEntity.ok(doctor);
    }


    // ---------------------------------------------------------
    // GET ALL DOCTORS
    // GET /api/doctors
    // ---------------------------------------------------------

    @GetMapping
    public ResponseEntity<List<Doctor>> getAllDoctors() {

        return ResponseEntity.ok(
                doctorService.getAllDoctors()
        );
    }


    // ---------------------------------------------------------
    // GET DOCTOR BY ID
    // GET /api/doctors/{id}
    // ---------------------------------------------------------

    @GetMapping("/{id}")
    public ResponseEntity<Doctor> getDoctor(
            @PathVariable Long id) {

        return doctorService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }


    // ---------------------------------------------------------
    // GET DOCTOR BY USER ID
    // GET /api/doctors/user/{userId}
    // ---------------------------------------------------------

    @GetMapping("/user/{userId}")
    public ResponseEntity<Doctor> getDoctorByUserId(
            @PathVariable Long userId) {

        return doctorService.findByUserId(userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }


    // ---------------------------------------------------------
    // GET DOCTOR BY DOCTOR ID
    // GET /api/doctors/doctor-id/{doctorId}
    // ---------------------------------------------------------

    @GetMapping("/doctor-id/{doctorId}")
    public ResponseEntity<Doctor> getDoctorByDoctorId(
            @PathVariable String doctorId) {

        return doctorService.findByDoctorId(doctorId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }


    // ---------------------------------------------------------
    // FIND DOCTOR BY SPECIALIZATION
    // GET /api/doctors/specialization/{specialization}
    // ---------------------------------------------------------

    @GetMapping("/specialization/{specialization}")
    public ResponseEntity<List<Doctor>> findBySpecialization(
            @PathVariable String specialization) {

        return ResponseEntity.ok(
                doctorService.findBySpecialization(
                        specialization
                )
        );
    }


    // ---------------------------------------------------------
    // FIND DOCTOR BY DEPARTMENT
    // GET /api/doctors/department/{department}
    // ---------------------------------------------------------

    @GetMapping("/department/{department}")
    public ResponseEntity<List<Doctor>> findByDepartment(
            @PathVariable String department) {

        return ResponseEntity.ok(
                doctorService.findByDepartment(
                        department
                )
        );
    }


    // ---------------------------------------------------------
    // UPDATE DOCTOR
    // PUT /api/doctors/{id}
    // ---------------------------------------------------------

    @PutMapping("/{id}")
    public ResponseEntity<Doctor> updateDoctor(
            @PathVariable Long id,
            @RequestBody Doctor doctor) {

        doctor.setId(id);

        Doctor updatedDoctor =
                doctorService.updateDoctor(doctor);

        return ResponseEntity.ok(updatedDoctor);
    }


    // ---------------------------------------------------------
    // DELETE DOCTOR
    // DELETE /api/doctors/{id}
    // ---------------------------------------------------------

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteDoctor(
            @PathVariable Long id) {

        doctorService.deleteDoctor(id);

        return ResponseEntity.ok(
                "Doctor deleted successfully"
        );
    }
}
