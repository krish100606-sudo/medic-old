package com.example.Health_Data_Management.repository;

import com.example.Health_Data_Management.entity.ConversationMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ConversationMessageRepository extends JpaRepository<ConversationMessage, Long> {

    List<ConversationMessage> findByMedicalCaseIdOrderByCreatedAtAsc(Long medicalCaseId);

    List<ConversationMessage> findByMedicalCaseIdAndQuestionCodeOrderByCreatedAtAsc(Long medicalCaseId, String questionCode);

    long countByMedicalCaseId(Long medicalCaseId);
}
