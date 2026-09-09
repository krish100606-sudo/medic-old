package com.example.Health_Data_Management.service;



import com.example.Health_Data_Management.entity.Patient;
import com.example.Health_Data_Management.entity.User;
import com.example.Health_Data_Management.repository.PatientRepository;
import com.example.Health_Data_Management.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PatientService {

    private final PatientRepository patientRepository;
    private final UserRepository userRepository;

    public PatientService(PatientRepository patientRepository,
                          UserRepository userRepository) {

        this.patientRepository = patientRepository;
        this.userRepository = userRepository;
    }

    // ---------------------------------------------------------
    // CREATE PATIENT PROFILE
    // ---------------------------------------------------------

    public Patient createPatient(Long userId) {

        if (patientRepository.existsByUserId(userId)) {
            throw new RuntimeException(
                    "Patient profile already exists"
            );
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "User not found"
                        )
                );

        Patient patient = new Patient();
        patient.setUser(user);

        return patientRepository.save(patient);
    }


    // ---------------------------------------------------------
    // FIND PATIENT BY ID
    // ---------------------------------------------------------

    public Optional<Patient> findById(Long id) {

        return patientRepository.findById(id);
    }


    // ---------------------------------------------------------
    // FIND PATIENT BY USER ID
    // ---------------------------------------------------------

    public Optional<Patient> findByUserId(Long userId) {

        return patientRepository.findByUserId(userId);
    }


    // ---------------------------------------------------------
    // GET ALL PATIENTS
    // ---------------------------------------------------------

    public List<Patient> getAllPatients() {

        return patientRepository.findAll();
    }


    // ---------------------------------------------------------
    // UPDATE PATIENT
    // ---------------------------------------------------------

    public Patient updatePatient(Patient updatedPatient) {

        Patient patient = patientRepository.findById(
                updatedPatient.getId()
        ).orElseThrow(() ->
                new RuntimeException(
                        "Patient not found"
                )
        );

        patient.setDateOfBirth(
                updatedPatient.getDateOfBirth()
        );

        patient.setGender(
                updatedPatient.getGender()
        );

        patient.setBloodGroup(
                updatedPatient.getBloodGroup()
        );

        patient.setPhone(
                updatedPatient.getPhone()
        );

        patient.setAddress(
                updatedPatient.getAddress()
        );

        patient.setEmergencyContactName(
                updatedPatient.getEmergencyContactName()
        );

        patient.setEmergencyContactPhone(
                updatedPatient.getEmergencyContactPhone()
        );

        patient.setAllergies(
                updatedPatient.getAllergies()
        );

        patient.setMedicalHistory(
                updatedPatient.getMedicalHistory()
        );

        return patientRepository.save(patient);
    }


    // ---------------------------------------------------------
    // DELETE PATIENT
    // ---------------------------------------------------------

    public void deletePatient(Long id) {

        if (!patientRepository.existsById(id)) {
            throw new RuntimeException(
                    "Patient not found"
            );
        }

        patientRepository.deleteById(id);
    }


    // ---------------------------------------------------------
    // FIND PATIENTS BY BLOOD GROUP
    // ---------------------------------------------------------

    public List<Patient> findByBloodGroup(
            String bloodGroup) {

        return patientRepository.findByBloodGroup(
                bloodGroup
        );
    }


    // ---------------------------------------------------------
    // FIND PATIENTS BY GENDER
    // ---------------------------------------------------------

    public List<Patient> findByGender(
            String gender) {

        return patientRepository.findByGender(
                gender
        );
    }


    // ---------------------------------------------------------
    // FIND PATIENT BY PHONE
    // ---------------------------------------------------------

    public Optional<Patient> findByPhone(
            String phone) {

        return patientRepository.findByPhone(phone);
    }
}