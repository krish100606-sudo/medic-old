package com.example.Health_Data_Management.controller;

import com.example.Health_Data_Management.dto.SimilarCaseDto;
import com.example.Health_Data_Management.entity.MedicalCase;
import com.example.Health_Data_Management.service.CaseService;
import com.example.Health_Data_Management.service.CaseSimilarityService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cases")
public class CaseSimilarityRestController {

    private final CaseSimilarityService caseSimilarityService;
    private final CaseService caseService;
    private final com.example.Health_Data_Management.service.GeminiAiService geminiAiService;

    public CaseSimilarityRestController(CaseSimilarityService caseSimilarityService,
                                        CaseService caseService,
                                        com.example.Health_Data_Management.service.GeminiAiService geminiAiService) {
        this.caseSimilarityService = caseSimilarityService;
        this.caseService = caseService;
        this.geminiAiService = geminiAiService;
    }

    /**
     * Stage 1: Returns top N similar past cases with a similarity score and clinical history.
     * GET /api/cases/{caseId}/similar?limit=5
     */
    @GetMapping("/{caseId}/similar")
    public ResponseEntity<?> getSimilarCases(
            @PathVariable("caseId") Long caseId,
            @RequestParam(value = "limit", defaultValue = "5") int limit) {

        MedicalCase currentCase = caseService.getCaseById(caseId);
        if (currentCase == null) {
            return ResponseEntity.notFound().build();
        }

        List<SimilarCaseDto> similarCases = caseSimilarityService.findSimilarCases(caseId, limit);
        return ResponseEntity.ok(similarCases);
    }

    /**
     * Stage 2: Generates an AI-drafted decision support suggestion based on current case + similar cases.
     * GET /api/cases/{caseId}/ai-suggestion
     */
    @GetMapping("/{caseId}/ai-suggestion")
    public ResponseEntity<?> getAiSuggestion(
            @PathVariable("caseId") Long caseId,
            @RequestParam(value = "limit", defaultValue = "4") int limit) {

        MedicalCase currentCase = caseService.getCaseById(caseId);
        if (currentCase == null) {
            return ResponseEntity.notFound().build();
        }

        List<SimilarCaseDto> similarCases = caseSimilarityService.findSimilarCases(caseId, limit);
        com.example.Health_Data_Management.dto.AiSuggestionResponse suggestion =
                geminiAiService.generateStructuredSuggestion(currentCase, similarCases);
        return ResponseEntity.ok(suggestion);
    }
}
