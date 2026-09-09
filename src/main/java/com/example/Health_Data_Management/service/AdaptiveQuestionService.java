package com.example.Health_Data_Management.service;

import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Adaptive Questioning Engine — rule-based clinical question branching.
 * Determines follow-up questions based on the patient's chief complaint
 * and previously answered questions.
 */
@Service
public class AdaptiveQuestionService {

    public static class AdaptiveQuestion {
        private final String questionCode;
        private final String questionTextEn;
        private final String questionTextHi;
        private final String category;
        private final List<String> touchOptions;
        private final boolean allowVoice;
        private final boolean allowFreeText;

        public AdaptiveQuestion(String questionCode, String questionTextEn, String questionTextHi,
                                String category, List<String> touchOptions, boolean allowVoice, boolean allowFreeText) {
            this.questionCode = questionCode;
            this.questionTextEn = questionTextEn;
            this.questionTextHi = questionTextHi;
            this.category = category;
            this.touchOptions = touchOptions != null ? touchOptions : new ArrayList<>();
            this.allowVoice = allowVoice;
            this.allowFreeText = allowFreeText;
        }

        public String getQuestionCode() { return questionCode; }
        public String getQuestionTextEn() { return questionTextEn; }
        public String getQuestionTextHi() { return questionTextHi; }
        public String getCategory() { return category; }
        public List<String> getTouchOptions() { return touchOptions; }
        public boolean isAllowVoice() { return allowVoice; }
        public boolean isAllowFreeText() { return allowFreeText; }
    }

    /**
     * Returns the list of questions for the case-taking flow,
     * dynamically adjusting based on the chief complaint and existing answers.
     */
    public List<AdaptiveQuestion> getQuestionsForCase(String chiefComplaint, Map<String, String> existingAnswers) {
        List<AdaptiveQuestion> questions = new ArrayList<>();

        // Step 1: Chief Complaint (always first)
        questions.add(new AdaptiveQuestion(
                "Q_CHIEF_COMPLAINT",
                "What is your main health problem?",
                "आपकी मुख्य स्वास्थ्य समस्या क्या है?",
                "CHIEF_COMPLAINT",
                List.of("Chest Pain", "Fever and Cough", "Shortness of Breath", "Severe Headache", "Abdominal Pain", "Joint Pain / Injury"),
                true, true
        ));

        // Step 2: Patient Statement (always)
        questions.add(new AdaptiveQuestion(
                "Q_STATEMENT",
                "Describe your symptoms in your own words",
                "कृपया अपनी भाषा में अपनी समस्या बताएं",
                "HISTORY",
                List.of(),
                true, true
        ));

        // Step 3: Onset (always)
        questions.add(new AdaptiveQuestion(
                "Q_ONSET",
                "When did the problem start?",
                "यह समस्या कब शुरू हुई?",
                "HISTORY",
                List.of("Today", "1–3 days", "4–7 days", "More than 1 week", "More than 1 month"),
                false, true
        ));

        // Step 4: Location (always)
        questions.add(new AdaptiveQuestion(
                "Q_LOCATION",
                "Where is the problem located?",
                "समस्या कहाँ है?",
                "HISTORY",
                List.of("Chest / Heart", "Throat / Neck", "Abdomen", "Head / Eyes", "Back / Spine", "Limbs / Joints"),
                false, true
        ));

        // Step 5: Severity (always)
        questions.add(new AdaptiveQuestion(
                "Q_SEVERITY",
                "How severe is the problem?",
                "समस्या कितनी गंभीर है?",
                "HISTORY",
                List.of("Mild (1-3/10)", "Moderate (4-6/10)", "Severe (8/10)", "Critical (9-10/10)"),
                false, false
        ));

        // Step 6: Associated Symptoms (always)
        questions.add(new AdaptiveQuestion(
                "Q_ASSOCIATED_SYMPTOMS",
                "Do you have any associated symptoms?",
                "क्या आपको कोई अन्य लक्षण हैं?",
                "SYMPTOMS",
                getAdaptiveSymptomOptions(chiefComplaint),
                true, true
        ));

        // ADAPTIVE: Complaint-specific follow-ups (inserted between symptoms and past history)
        List<AdaptiveQuestion> followUps = getComplaintSpecificFollowUps(chiefComplaint, existingAnswers);
        questions.addAll(followUps);

        // Step 7+: Past Medical History
        questions.add(new AdaptiveQuestion(
                "Q_PAST_DISEASES",
                "Previous medical conditions",
                "पिछली चिकित्सा स्थितियाँ",
                "MEDICAL_HISTORY",
                List.of("Diabetes", "Hypertension", "Asthma", "Thyroid Disorder", "Heart Disease", "None"),
                true, true
        ));

        // Surgical History
        questions.add(new AdaptiveQuestion(
                "Q_SURGERIES",
                "Previous surgeries or hospitalizations",
                "पिछली सर्जरी या अस्पताल में भर्ती",
                "MEDICAL_HISTORY",
                List.of("Appendectomy", "Gallbladder Surgery", "Cardiac Stent", "C-Section", "No Surgeries"),
                true, true
        ));

        // Medications
        questions.add(new AdaptiveQuestion(
                "Q_MEDICATIONS",
                "Current medications",
                "वर्तमान दवाइयाँ",
                "MEDICATIONS",
                List.of(),
                true, true
        ));

        // Allergies & Family History
        questions.add(new AdaptiveQuestion(
                "Q_ALLERGIES",
                "Allergies and Family History",
                "एलर्जी और पारिवारिक इतिहास",
                "ALLERGIES",
                List.of("No Known Allergies (NKDA)", "Penicillin Allergy", "Sulfa Drug Allergy", "Food Allergies"),
                true, true
        ));

        return questions;
    }

    /**
     * Returns adaptive symptom options based on the chief complaint
     */
    private List<String> getAdaptiveSymptomOptions(String chiefComplaint) {
        if (chiefComplaint == null) return getDefaultSymptomOptions();

        String cc = chiefComplaint.toLowerCase();

        if (cc.contains("chest") || cc.contains("heart") || cc.contains("chhati")) {
            return List.of(
                    "Breathing difficulty / Saans mein takleef",
                    "Sweating / Perspiration / Paseena",
                    "Nausea / Ulti jaisi feeling",
                    "Pain radiating to left arm / jaw",
                    "Palpitations / Dhadkan tez",
                    "Dizziness / Chakkar aana"
            );
        } else if (cc.contains("fever") || cc.contains("cough") || cc.contains("bukhar")) {
            return List.of(
                    "Body aches / Badan dard",
                    "Sore throat / Gala kharab",
                    "Runny nose / Naak behna",
                    "Chills / Thandi lagna",
                    "Loss of appetite / Bhookh na lagna",
                    "Rash / Daane"
            );
        } else if (cc.contains("headache") || cc.contains("sir dard")) {
            return List.of(
                    "Nausea / Vomiting / Ulti",
                    "Light sensitivity / Roshni se taklif",
                    "Neck stiffness / Gardan akadna",
                    "Visual disturbance / Nazar dhundhli",
                    "Dizziness / Chakkar",
                    "Fever / Bukhar"
            );
        } else if (cc.contains("abdominal") || cc.contains("stomach") || cc.contains("pet")) {
            return List.of(
                    "Nausea / Vomiting / Ulti",
                    "Diarrhea / Dast",
                    "Constipation / Kabz",
                    "Bloating / Pet phulna",
                    "Blood in stool / Khoon aana",
                    "Loss of appetite / Bhookh na lagna"
            );
        } else if (cc.contains("breath") || cc.contains("saans")) {
            return List.of(
                    "Wheezing / Seethi bajti hai",
                    "Cough / Khansi",
                    "Chest tightness / Chhati mein jakdan",
                    "Bluish lips / Hoth neele",
                    "Fever / Bukhar",
                    "Fatigue / Thakaan"
            );
        }

        return getDefaultSymptomOptions();
    }

    private List<String> getDefaultSymptomOptions() {
        return List.of(
                "Breathing difficulty",
                "Mild perspiration",
                "Nausea / Dizziness",
                "Fatigue / Weakness",
                "Fever",
                "Loss of appetite"
        );
    }

    /**
     * Returns complaint-specific follow-up questions for adaptive questioning
     */
    private List<AdaptiveQuestion> getComplaintSpecificFollowUps(String chiefComplaint, Map<String, String> existingAnswers) {
        List<AdaptiveQuestion> followUps = new ArrayList<>();
        if (chiefComplaint == null) return followUps;

        String cc = chiefComplaint.toLowerCase();

        if (cc.contains("chest") || cc.contains("heart") || cc.contains("chhati")) {
            followUps.add(new AdaptiveQuestion(
                    "Q_ADAPTIVE_EXERTION",
                    "Does the pain increase with physical activity or exertion?",
                    "क्या शारीरिक गतिविधि से दर्द बढ़ता है?",
                    "ADAPTIVE_CARDIAC",
                    List.of("Yes, on exertion / Haan, mehnat se", "At rest / Aaram mein bhi", "Both / Dono", "Not sure / Pata nahi"),
                    true, true
            ));
            followUps.add(new AdaptiveQuestion(
                    "Q_ADAPTIVE_CARDIAC_RISK",
                    "Do you smoke or have a family history of heart disease?",
                    "क्या आप धूम्रपान करते हैं या परिवार में हृदय रोग का इतिहास है?",
                    "ADAPTIVE_CARDIAC",
                    List.of("Smoker / Dhumrapan", "Family history of heart disease / Parivar mein dil ki bimari",
                            "Both / Dono", "Neither / Koi nahi"),
                    true, true
            ));
        } else if (cc.contains("fever") || cc.contains("bukhar")) {
            followUps.add(new AdaptiveQuestion(
                    "Q_ADAPTIVE_FEVER_PATTERN",
                    "What is the pattern of your fever?",
                    "बुखार का पैटर्न कैसा है?",
                    "ADAPTIVE_FEVER",
                    List.of("Continuous / Lagatar", "Intermittent / Aata-jaata", "Evening rise / Shaam ko", "With chills / Kamp ke saath"),
                    true, true
            ));
            followUps.add(new AdaptiveQuestion(
                    "Q_ADAPTIVE_TRAVEL",
                    "Have you traveled to any rural or malaria-endemic area recently?",
                    "क्या हाल ही में किसी ग्रामीण या मलेरिया प्रभावित क्षेत्र में गए हैं?",
                    "ADAPTIVE_FEVER",
                    List.of("Yes / Haan", "No / Nahi", "Not sure / Pata nahi"),
                    true, true
            ));
        } else if (cc.contains("headache") || cc.contains("sir dard")) {
            followUps.add(new AdaptiveQuestion(
                    "Q_ADAPTIVE_HEADACHE_TYPE",
                    "What type of headache do you experience?",
                    "सिरदर्द किस प्रकार का है?",
                    "ADAPTIVE_NEURO",
                    List.of("Throbbing / Dhadkane wala", "Pressure / Dabav wala", "Sharp / Tez",
                            "Band-like / Patti jaisa", "One side / Ek taraf"),
                    true, true
            ));
        } else if (cc.contains("abdominal") || cc.contains("pet")) {
            followUps.add(new AdaptiveQuestion(
                    "Q_ADAPTIVE_ABDOMEN_RELATION",
                    "Is the pain related to eating food?",
                    "क्या दर्द खाना खाने से संबंधित है?",
                    "ADAPTIVE_GI",
                    List.of("Worse after eating / Khane ke baad", "Better after eating / Khane se rahat",
                            "No relation / Koi sambandh nahi", "Worse when hungry / Bhookh lagne par"),
                    true, true
            ));
        }

        return followUps;
    }

    /**
     * Returns the total number of questions for a given complaint
     */
    public int getTotalQuestionCount(String chiefComplaint, Map<String, String> existingAnswers) {
        return getQuestionsForCase(chiefComplaint, existingAnswers).size();
    }

    /**
     * Returns a specific question by step index (0-based internally, 1-based for UI)
     */
    public AdaptiveQuestion getQuestionByStep(String chiefComplaint, Map<String, String> existingAnswers, int step) {
        List<AdaptiveQuestion> questions = getQuestionsForCase(chiefComplaint, existingAnswers);
        int index = step - 1; // Convert 1-based step to 0-based index
        if (index < 0 || index >= questions.size()) return null;
        return questions.get(index);
    }
}
