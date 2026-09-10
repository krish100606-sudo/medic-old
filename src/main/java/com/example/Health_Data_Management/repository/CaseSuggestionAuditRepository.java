package com.example.Health_Data_Management.repository;

import com.example.Health_Data_Management.entity.CaseSuggestionAudit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CaseSuggestionAuditRepository extends JpaRepository<CaseSuggestionAudit, Long> {
    List<CaseSuggestionAudit> findByMedicalCaseIdOrderByCreatedAtDesc(Long medicalCaseId);
}
