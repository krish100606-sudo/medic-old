package com.example.Health_Data_Management.service;



import com.example.Health_Data_Management.entity.Doctor;
import com.example.Health_Data_Management.entity.User;
import com.example.Health_Data_Management.repository.DoctorRepository;
import com.example.Health_Data_Management.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final UserRepository userRepository;

    public DoctorService(DoctorRepository doctorRepository,
                         UserRepository userRepository) {

        this.doctorRepository = doctorRepository;
        this.userRepository = userRepository;
    }

    // ---------------------------------------------------------
    // CREATE DOCTOR PROFILE
    // ---------------------------------------------------------

    public Doctor createDoctor(Long userId) {

        if (doctorRepository.existsByUserId(userId)) {
            throw new RuntimeException(
                    "Doctor profile already exists"
            );
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "User not found"
                        )
                );

        Doctor doctor = new Doctor();

        doctor.setUser(user);

        return doctorRepository.save(doctor);
    }


    // ---------------------------------------------------------
    // FIND DOCTOR BY ID
    // ---------------------------------------------------------

    public Optional<Doctor> findById(Long id) {

        return doctorRepository.findById(id);
    }


    // ---------------------------------------------------------
    // FIND DOCTOR BY USER ID
    // ---------------------------------------------------------

    public Optional<Doctor> findByUserId(Long userId) {

        return doctorRepository.findByUserId(userId);
    }


    // ---------------------------------------------------------
    // FIND DOCTOR BY DOCTOR ID
    // ---------------------------------------------------------

    public Optional<Doctor> findByDoctorId(
            String doctorId) {

        return doctorRepository.findByDoctorId(doctorId);
    }


    // ---------------------------------------------------------
    // FIND DOCTOR BY LICENSE NUMBER
    // ---------------------------------------------------------

    public Optional<Doctor> findByLicenseNumber(
            String licenseNumber) {

        return doctorRepository.findByLicenseNumber(
                licenseNumber
        );
    }


    // ---------------------------------------------------------
    // GET ALL DOCTORS
    // ---------------------------------------------------------

    public List<Doctor> getAllDoctors() {

        return doctorRepository.findAll();
    }


    // ---------------------------------------------------------
    // FIND BY SPECIALIZATION
    // ---------------------------------------------------------

    public List<Doctor> findBySpecialization(
            String specialization) {

        return doctorRepository
                .findBySpecializationIgnoreCase(
                        specialization
                );
    }


    // ---------------------------------------------------------
    // FIND BY DEPARTMENT
    // ---------------------------------------------------------

    public List<Doctor> findByDepartment(
            String department) {

        return doctorRepository
                .findByDepartmentIgnoreCase(
                        department
                );
    }


    // ---------------------------------------------------------
    // UPDATE DOCTOR
    // ---------------------------------------------------------

    public Doctor updateDoctor(
            Doctor updatedDoctor) {

        Doctor doctor = doctorRepository.findById(
                updatedDoctor.getId()
        ).orElseThrow(() ->
                new RuntimeException(
                        "Doctor not found"
                )
        );

        doctor.setDoctorId(
                updatedDoctor.getDoctorId()
        );

        doctor.setSpecialization(
                updatedDoctor.getSpecialization()
        );

        doctor.setQualification(
                updatedDoctor.getQualification()
        );

        doctor.setLicenseNumber(
                updatedDoctor.getLicenseNumber()
        );

        doctor.setDepartment(
                updatedDoctor.getDepartment()
        );

        doctor.setPhone(
                updatedDoctor.getPhone()
        );

        doctor.setAddress(
                updatedDoctor.getAddress()
        );

        doctor.setYearsOfExperience(
                updatedDoctor.getYearsOfExperience()
        );

        return doctorRepository.save(doctor);
    }


    // ---------------------------------------------------------
    // DELETE DOCTOR
    // ---------------------------------------------------------

    public void deleteDoctor(Long id) {

        if (!doctorRepository.existsById(id)) {
            throw new RuntimeException(
                    "Doctor not found"
            );
        }

        doctorRepository.deleteById(id);
    }
}
