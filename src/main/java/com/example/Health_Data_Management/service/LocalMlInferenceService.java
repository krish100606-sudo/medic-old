package com.example.Health_Data_Management.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;

/**
 * Service to execute local Random Forest ML disease inference from mainmodel.
 */
@Service
public class LocalMlInferenceService {

    private static final Logger log = LoggerFactory.getLogger(LocalMlInferenceService.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    public static class DiseasePrediction {
        private String disease;
        private double probability;

        public DiseasePrediction() {}
        public DiseasePrediction(String disease, double probability) {
            this.disease = disease;
            this.probability = probability;
        }

        public String getDisease() { return disease; }
        public void setDisease(String disease) { this.disease = disease; }
        public double getProbability() { return probability; }
        public void setProbability(double probability) { this.probability = probability; }
    }

    public static class MlInferenceResult {
        @JsonProperty("top_disease")
        private String topDisease;

        @JsonProperty("top_probability")
        private double topProbability;

        @JsonProperty("recognized_symptoms")
        private List<String> recognizedSymptoms = new ArrayList<>();

        @JsonProperty("predictions")
        private List<DiseasePrediction> predictions = new ArrayList<>();

        public MlInferenceResult() {}

        public MlInferenceResult(String topDisease, double topProbability, List<String> recognizedSymptoms, List<DiseasePrediction> predictions) {
            this.topDisease = topDisease;
            this.topProbability = topProbability;
            this.recognizedSymptoms = recognizedSymptoms;
            this.predictions = predictions;
        }

        public String getTopDisease() { return topDisease; }
        public void setTopDisease(String topDisease) { this.topDisease = topDisease; }
        public double getTopProbability() { return topProbability; }
        public void setTopProbability(double topProbability) { this.topProbability = topProbability; }
        public List<String> getRecognizedSymptoms() { return recognizedSymptoms; }
        public void setRecognizedSymptoms(List<String> recognizedSymptoms) { this.recognizedSymptoms = recognizedSymptoms; }
        public List<DiseasePrediction> getPredictions() { return predictions; }
        public void setPredictions(List<DiseasePrediction> predictions) { this.predictions = predictions; }
    }

    /**
     * Executes ML prediction using mainmodel/predict.py or rule-based fallback.
     */
    public MlInferenceResult predict(int age, String gender, List<String> symptoms) {
        File script = new File("mainmodel/predict.py");
        if (!script.exists()) {
            return fallbackRulePrediction(symptoms);
        }

        String symptomList = String.join(",", symptoms != null ? symptoms : List.of());

        try {
            ProcessBuilder pb = new ProcessBuilder(
                    "python3",
                    script.getAbsolutePath(),
                    "--age", String.valueOf(age > 0 ? age : 35),
                    "--gender", (gender != null && !gender.isBlank()) ? gender : "Male",
                    "--symptoms", symptomList
            );
            pb.redirectErrorStream(true);

            Process process = pb.start();

            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line);
                }
            }

            boolean finished = process.waitFor(12, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                log.warn("Local ML inference timed out, using fallback");
                return fallbackRulePrediction(symptoms);
            }

            String json = output.toString().trim();
            // If output contains multiple lines, find json object
            int jsonStart = json.indexOf('{');
            if (jsonStart >= 0) {
                json = json.substring(jsonStart);
                return objectMapper.readValue(json, MlInferenceResult.class);
            }

        } catch (Exception e) {
            log.warn("Failed to run local ML model: {}", e.getMessage());
        }

        return fallbackRulePrediction(symptoms);
    }

    private MlInferenceResult fallbackRulePrediction(List<String> symptoms) {
        String top = "Clinical Evaluation Recommended";
        double prob = 0.50;
        List<DiseasePrediction> list = new ArrayList<>();

        String combined = symptoms != null ? String.join(" ", symptoms).toLowerCase() : "";

        if (combined.contains("chest") || combined.contains("heart")) {
            top = "Cardiovascular Risk / Angina";
            prob = 0.82;
            list.add(new DiseasePrediction("Heart Disease", 0.82));
            list.add(new DiseasePrediction("Hypertension", 0.12));
        } else if (combined.contains("fever") || combined.contains("cough")) {
            top = "Respiratory Infection / Bronchitis";
            prob = 0.78;
            list.add(new DiseasePrediction("Bronchitis", 0.78));
            list.add(new DiseasePrediction("Pneumonia", 0.15));
        } else if (combined.contains("headache")) {
            top = "Migraine / Tension Headache";
            prob = 0.75;
            list.add(new DiseasePrediction("Migraine", 0.75));
        } else {
            list.add(new DiseasePrediction("General Consultation", 0.60));
        }

        return new MlInferenceResult(top, prob, symptoms != null ? symptoms : List.of(), list);
    }
}
