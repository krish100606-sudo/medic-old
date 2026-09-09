package com.example.Health_Data_Management.repository;

import com.example.Health_Data_Management.entity.CaseAnswer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CaseAnswerRepository extends JpaRepository<CaseAnswer, Long> {

    List<CaseAnswer> findByMedicalCaseIdOrderByCreatedAtAsc(Long medicalCaseId);

    Optional<CaseAnswer> findByMedicalCaseIdAndQuestionCode(Long medicalCaseId, String questionCode);

    void deleteByMedicalCaseId(Long medicalCaseId);
}
