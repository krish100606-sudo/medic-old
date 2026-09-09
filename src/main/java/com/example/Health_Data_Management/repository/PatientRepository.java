package com.example.Health_Data_Management.repository;



import com.example.Health_Data_Management.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PatientRepository extends JpaRepository<Patient, Long> {

    // Find patient using the linked User ID
    Optional<Patient> findByUserId(Long userId);

    // Check whether a patient profile already exists
    boolean existsByUserId(Long userId);

    // Find patients by blood group
    List<Patient> findByBloodGroup(String bloodGroup);

    // Find patients by gender
    List<Patient> findByGender(String gender);

    // Search patients by phone number
    Optional<Patient> findByPhone(String phone);

    // Search patient by patient ID
    Optional<Patient> findByPatientId(String patientId);
}
