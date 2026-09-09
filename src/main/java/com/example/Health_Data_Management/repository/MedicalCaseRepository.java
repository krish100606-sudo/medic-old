package com.example.Health_Data_Management.repository;

import com.example.Health_Data_Management.entity.CasePriority;
import com.example.Health_Data_Management.entity.CaseStatus;
import com.example.Health_Data_Management.entity.MedicalCase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MedicalCaseRepository extends JpaRepository<MedicalCase, Long> {

    Optional<MedicalCase> findByCaseNumber(String caseNumber);

    List<MedicalCase> findByPatientIdOrderByCreatedAtDesc(Long patientId);

    Optional<MedicalCase> findFirstByPatientIdAndStatusOrderByCreatedAtDesc(Long patientId, CaseStatus status);

    List<MedicalCase> findByStatusOrderByCreatedAtDesc(CaseStatus status);

    List<MedicalCase> findByStatusInOrderByTokenNumberAscCreatedAtDesc(List<CaseStatus> statuses);

    List<MedicalCase> findByPriority(CasePriority priority);

    @Query("SELECT c FROM MedicalCase c WHERE " +
           "LOWER(c.patient.user.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(c.caseNumber) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(c.patient.department) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(c.chiefComplaint) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<MedicalCase> searchCases(@Param("keyword") String keyword);

    long countByStatus(CaseStatus status);

    long countByPriority(CasePriority priority);

    @Query("SELECT MAX(c.tokenNumber) FROM MedicalCase c")
    Integer findMaxTokenNumber();
}
