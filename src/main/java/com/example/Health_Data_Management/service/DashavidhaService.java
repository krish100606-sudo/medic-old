package com.example.Health_Data_Management.service;

import com.example.Health_Data_Management.entity.MedicalCase;
import com.example.Health_Data_Management.entity.Patient;
import org.springframework.stereotype.Service;

@Service
public class DashavidhaService {

    public static class DashavidhaEvaluation {
        private String prakriti; // Vata, Pitta, Kapha, Vata-Pitta, etc.
        private String vikriti;  // Dosha imbalance
        private String sara;     // Tissue vitality
        private String samhanana;// Compactness
        private String pramana;  // Proportions
        private String satmya;   // Adaptability
        private String satva;    // Mental strength
        private String aharaShakti;  // Digestive strength
        private String vyayamaShakti;// Exercise tolerance
        private String vaya;     // Life stage
        private String clinicalNotesAyush;

        public String getPrakriti() { return prakriti; }
        public void setPrakriti(String prakriti) { this.prakriti = prakriti; }

        public String getVikriti() { return vikriti; }
        public void setVikriti(String vikriti) { this.vikriti = vikriti; }

        public String getSara() { return sara; }
        public void setSara(String sara) { this.sara = sara; }

        public String getSamhanana() { return samhanana; }
        public void setSamhanana(String samhanana) { this.samhanana = samhanana; }

        public String getPramana() { return pramana; }
        public void setPramana(String pramana) { this.pramana = pramana; }

        public String getSatmya() { return satmya; }
        public void setSatmya(String satmya) { this.satmya = satmya; }

        public String getSatva() { return satva; }
        public void setSatva(String satva) { this.satva = satva; }

        public String getAharaShakti() { return aharaShakti; }
        public void setAharaShakti(String aharaShakti) { this.aharaShakti = aharaShakti; }

        public String getVyayamaShakti() { return vyayamaShakti; }
        public void setVyayamaShakti(String vyayamaShakti) { this.vyayamaShakti = vyayamaShakti; }

        public String getVaya() { return vaya; }
        public void setVaya(String vaya) { this.vaya = vaya; }

        public String getClinicalNotesAyush() { return clinicalNotesAyush; }
        public void setClinicalNotesAyush(String clinicalNotesAyush) { this.clinicalNotesAyush = clinicalNotesAyush; }
    }

    public DashavidhaEvaluation evaluate(MedicalCase medicalCase) {
        DashavidhaEvaluation eval = new DashavidhaEvaluation();
        Patient patient = medicalCase != null ? medicalCase.getPatient() : null;

        // 1. Vaya (Age stage)
        int age = 35;
        if (patient != null && patient.getAge() != null) {
            age = patient.getAge();
        }
        if (age < 16) {
            eval.setVaya("Balyavastha (Childhood / Kapha Predominant)");
        } else if (age <= 60) {
            eval.setVaya("Madhyamavastha (Adult / Pitta Predominant)");
        } else {
            eval.setVaya("Vriddhavastha (Elderly / Vata Predominant)");
        }

        // 2. Vikriti & Prakriti analysis based on symptoms & chief complaint
        String cc = medicalCase != null && medicalCase.getChiefComplaint() != null ? medicalCase.getChiefComplaint().toLowerCase() : "";
        String statement = medicalCase != null && medicalCase.getPatientStatement() != null ? medicalCase.getPatientStatement().toLowerCase() : "";
        String symptoms = medicalCase != null && medicalCase.getAssociatedSymptoms() != null ? medicalCase.getAssociatedSymptoms().toLowerCase() : "";
        String all = cc + " " + statement + " " + symptoms;

        if (all.contains("chest") || all.contains("breath") || all.contains("cough") || all.contains("cold") || all.contains("phlegm")) {
            eval.setPrakriti("Kapha-Vataja");
            eval.setVikriti("Pranavaha Srotodushti with Kapha-Vata Dushti");
        } else if (all.contains("fever") || all.contains("burning") || all.contains("acid") || all.contains("rash") || all.contains("heat")) {
            eval.setPrakriti("Pittaja");
            eval.setVikriti("Pittadhika Jwara / Ushna Guna Vriddhi");
        } else if (all.contains("pain") || all.contains("joint") || all.contains("headache") || all.contains("radiating") || all.contains("shivering")) {
            eval.setPrakriti("Vataja");
            eval.setVikriti("Vata Prakopa (Shula Pradhana)");
        } else {
            eval.setPrakriti("Sama Prakriti (Tridoshic Balance)");
            eval.setVikriti("Alpa Dosha Vaishamya");
        }

        // 3. Sara (Tissue vitality)
        eval.setSara("Madhyama Sara (Moderate tissue vitality)");

        // 4. Samhanana (Compactness)
        eval.setSamhanana("Madhyama Samhanana (Normal skeletal muscular build)");

        // 5. Pramana (Anthropometric proportions)
        eval.setPramana("Pramana Yukta (Normal anthropometric balance)");

        // 6. Satmya (Adaptability)
        eval.setSatmya("Oka Satmya (Habituated to mixed Indian diet)");

        // 7. Satva (Mental strength)
        if (all.contains("severe") || all.contains("anxiety") || all.contains("fear")) {
            eval.setSatva("Avara Satva (Low psychic tolerance under acute stress)");
        } else {
            eval.setSatva("Madhyama Satva (Moderate mental fortitude)");
        }

        // 8. Ahara Shakti (Digestive capacity / Agni)
        if (all.contains("nausea") || all.contains("vomiting") || all.contains("appetite") || all.contains("stomach")) {
            eval.setAharaShakti("Manda Agni (Sluggish digestion / poor appetite)");
        } else {
            eval.setAharaShakti("Sama Agni (Balanced digestive fire)");
        }

        // 9. Vyayama Shakti (Physical endurance)
        if (all.contains("breath") || all.contains("fatigue") || all.contains("chest")) {
            eval.setVyayamaShakti("Heena Shakti (Severely reduced exercise tolerance)");
        } else {
            eval.setVyayamaShakti("Madhyama Shakti (Moderate physical tolerance)");
        }

        // 10. Summary clinical note
        eval.setClinicalNotesAyush(String.format("Dashavidha Pariksha suggests %s with primary %s. Focus on Vata-Shamana and Agni-Deepana therapy.",
                eval.getPrakriti(), eval.getVikriti()));

        return eval;
    }
}
