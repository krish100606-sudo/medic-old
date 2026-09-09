package com.example.Health_Data_Management.repository;



import com.example.Health_Data_Management.entity.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DoctorRepository extends JpaRepository<Doctor, Long> {

    // Find doctor using the linked User ID
    Optional<Doctor> findByUserId(Long userId);

    // Check whether a doctor profile already exists
    boolean existsByUserId(Long userId);

    // Find doctor using doctor ID
    Optional<Doctor> findByDoctorId(String doctorId);

    // Find doctor using license number
    Optional<Doctor> findByLicenseNumber(String licenseNumber);

    // Find doctors by specialization
    List<Doctor> findBySpecializationIgnoreCase(
            String specialization
    );

    // Find doctors by department
    List<Doctor> findByDepartmentIgnoreCase(
            String department
    );
}
