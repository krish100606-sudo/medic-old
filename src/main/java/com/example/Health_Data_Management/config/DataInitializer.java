package com.example.Health_Data_Management.config;

import com.example.Health_Data_Management.entity.*;
import com.example.Health_Data_Management.repository.*;
import com.example.Health_Data_Management.service.SummaryService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final MedicalCaseRepository caseRepository;
    private final MedicalDocumentRepository documentRepository;
    private final CaseAnswerRepository answerRepository;
    private final PasswordEncoder passwordEncoder;
    private final SummaryService summaryService;

    private final javax.sql.DataSource dataSource;

    public DataInitializer(
            UserRepository userRepository,
            PatientRepository patientRepository,
            DoctorRepository doctorRepository,
            MedicalCaseRepository caseRepository,
            MedicalDocumentRepository documentRepository,
            CaseAnswerRepository answerRepository,
            PasswordEncoder passwordEncoder,
            SummaryService summaryService,
            javax.sql.DataSource dataSource) {
        this.userRepository = userRepository;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
        this.caseRepository = caseRepository;
        this.documentRepository = documentRepository;
        this.answerRepository = answerRepository;
        this.passwordEncoder = passwordEncoder;
        this.summaryService = summaryService;
        this.dataSource = dataSource;
    }

    @Override
    public void run(String... args) {
        // Upgrade and ensure columns exist in database
        try (java.sql.Connection conn = dataSource.getConnection();
             java.sql.Statement stmt = conn.createStatement()) {
            try { stmt.execute("ALTER TABLE medical_cases ADD COLUMN IF NOT EXISTS symptoms TEXT"); } catch (SQLException ignored) {}
            try { stmt.execute("ALTER TABLE medical_cases ADD COLUMN IF NOT EXISTS diagnosis VARCHAR(500)"); } catch (SQLException ignored) {}
            try { stmt.execute("ALTER TABLE medical_cases ADD COLUMN IF NOT EXISTS treatment TEXT"); } catch (SQLException ignored) {}
            try { stmt.execute("ALTER TABLE medical_cases ADD COLUMN IF NOT EXISTS vitals VARCHAR(255)"); } catch (SQLException ignored) {}
            try { stmt.execute("ALTER TABLE medical_cases ADD COLUMN IF NOT EXISTS outcome VARCHAR(100)"); } catch (SQLException ignored) {}

            String[] cols = {
                "digital_signature", "drug_interactions_json", "dashavidha_assessment",
                "doctor_clinical_notes", "structured_summary", "medical_timeline",
                "priority_reason", "red_flags_details", "patient_statement",
                "associated_symptoms", "past_medical_history", "current_medication", "investigations",
                "symptoms", "treatment"
            };
            for (String col : cols) {
                try {
                    stmt.execute("ALTER TABLE medical_cases ALTER COLUMN " + col + " TYPE TEXT");
                } catch (SQLException ignored) {}
            }
        } catch (SQLException ignored) {}

        // Check if demo doctor already exists
        if (userRepository.findByEmail("doctor@medikiosk.com").isPresent()) {
            Doctor existingDoctor = doctorRepository.findAll().stream().findFirst().orElse(null);
            seedPastBenchmarkCases(existingDoctor);
            updateExistingCasesIfEmpty();
            return;
        }

        // 1. Seed Doctor (Dr. Ananya Roy, MD)
        User docUser = new User("Dr. Ananya Roy", "doctor@medikiosk.com", passwordEncoder.encode("doctor123"), Role.DOCTOR);
        userRepository.save(docUser);

        Doctor doctor = new Doctor(docUser);
        doctor.setDoctorId("DOC-101");
        doctor.setDepartment("General Medicine");
        doctor.setSpecialization("Internal Medicine & Critical Care");
        doctor.setQualification("MBBS, MD (General Medicine)");
        doctor.setLicenseNumber("MCI-2018-98421");
        doctor.setYearsOfExperience(12);
        doctor.setPhone("9876500001");
        doctorRepository.save(doctor);

        // 2. Seed Admin
        User adminUser = new User("MediKiosk Admin", "admin@medikiosk.com", passwordEncoder.encode("admin123"), Role.ADMIN);
        userRepository.save(adminUser);

        // 3. Seed Primary Demo Patient (Rahul Sharma, 42, General Medicine)
        User rahulUser = new User("Rahul Sharma", "rahul@gmail.com", passwordEncoder.encode("patient123"), Role.PATIENT);
        userRepository.save(rahulUser);

        Patient rahulPatient = new Patient(rahulUser, "P-10042", 42, "Male", "9876543210", "General Medicine", "English");
        rahulPatient.setAbhaId("14-1234-5678-9012");
        rahulPatient.setBloodGroup("B+");
        rahulPatient.setAddress("42, Sector 5, Central Enclave, New Delhi");
        rahulPatient.setEmergencyContactName("Sunita Sharma");
        rahulPatient.setEmergencyContactPhone("9876543211");
        rahulPatient.setConsentAccepted(true);
        rahulPatient.setConsentAcceptedAt(LocalDateTime.now().minusHours(2));
        patientRepository.save(rahulPatient);

        // 4. Seed Rahul's High-Priority Case (Token 104)
        MedicalCase case104 = new MedicalCase(rahulPatient);
        case104.setCaseNumber("MK-2026-104");
        case104.setTokenNumber(104);
        case104.setStatus(CaseStatus.SUBMITTED);
        case104.setPriority(CasePriority.HIGH);
        case104.setPriorityReason("Chest pain with breathing difficulty (Cardiovascular / Respiratory Red Flag)");
        case104.setRedFlagsDetected(true);
        case104.setRedFlagsDetails("Patient reported acute chest discomfort radiating to left arm accompanied by shortness of breath.");

        case104.setChiefComplaint("Chest Pain");
        case104.setPatientStatement("Mujhe do ghante se chest mein pain ho raha hai.");
        case104.setOnset("Today (approx. 2 hours ago)");
        case104.setLocation("Substernal chest region radiating to left shoulder");
        case104.setSeverity("Severe (8/10)");
        case104.setAssociatedSymptoms("Breathing difficulty, mild perspiration, chest tightness");
        case104.setPastMedicalHistory("Type 2 Diabetes Mellitus (diagnosed in 2024)");
        case104.setSurgicalHistory("Laparoscopic Appendectomy (2023), uneventful recovery");
        case104.setCurrentMedication("Tab. Metformin 500 mg BD (after meals)");
        case104.setAllergies("No known drug or food allergies (NKDA)");
        case104.setFamilyHistory("Father had Coronary Artery Disease (CAD) at age 55");
        case104.setPersonalHistory("Non-smoker, non-alcoholic, desk job, sedentary lifestyle");
        case104.setInvestigations("HbA1c: 7.8% (Sub-optimally controlled), Fasting Blood Sugar: 154 mg/dL");
        case104.setSubmittedAt(LocalDateTime.now().minusMinutes(45));
        caseRepository.save(case104);

        // Rahul's Documents
        MedicalDocument docPrescription = new MedicalDocument(rahulPatient, case104, "previous-prescription.jpg", "previous-prescription.jpg", "image/jpeg", DocumentType.PRESCRIPTION);
        docPrescription.setExtractedText("CLINICAL PRESCRIPTION\nDate: 12-Nov-2025\nDiagnosis: Type 2 Diabetes Mellitus\nRx: Tab. Metformin 500 mg BD\nAdvice: Regular exercise and glycemic monitoring.");
        docPrescription.setExtractedDiagnosis("Diabetes Mellitus (Type 2)");
        docPrescription.setExtractedMedications("Metformin 500 mg (1-0-1)");
        docPrescription.setExtractedInvestigations("Advised 3-monthly HbA1c test");
        docPrescription.setOcrStatus(OcrStatus.PROCESSED);
        documentRepository.save(docPrescription);

        MedicalDocument docBloodReport = new MedicalDocument(rahulPatient, case104, "blood-report.jpg", "blood-report.jpg", "image/jpeg", DocumentType.BLOOD_REPORT);
        docBloodReport.setExtractedText("PATHOLOGY REPORT\nDate: 15-Jan-2026\nGlycated Hemoglobin (HbA1c): 7.8 %\nFasting Plasma Glucose: 154 mg/dL\nSerum Creatinine: 0.9 mg/dL");
        docBloodReport.setExtractedDiagnosis("Sub-optimally controlled Glycemia");
        docBloodReport.setExtractedMedications("Metformin 500 mg (continued)");
        docBloodReport.setExtractedInvestigations("HbA1c — 7.8%, Fasting Blood Sugar — 154 mg/dL");
        docBloodReport.setOcrStatus(OcrStatus.PROCESSED);
        documentRepository.save(docBloodReport);

        // Generate Structured Summary & Timeline for Case 104
        List<MedicalDocument> rahulDocs = List.of(docPrescription, docBloodReport);
        case104.setStructuredSummary(summaryService.generateStructuredSummary(case104, rahulDocs));
        case104.setMedicalTimeline(summaryService.generateMedicalTimeline(case104, rahulDocs));
        caseRepository.save(case104);

        // Rahul's Case Answers
        answerRepository.save(new CaseAnswer(case104, "Q_CHIEF_COMPLAINT", "What is your main health problem?", "Chest Pain", InputType.TOUCH));
        answerRepository.save(new CaseAnswer(case104, "Q_STATEMENT", "Please describe your symptoms in your own words", "Mujhe do ghante se chest mein pain ho raha hai.", InputType.VOICE));
        answerRepository.save(new CaseAnswer(case104, "Q_ONSET", "When did the problem start?", "Today", InputType.TOUCH));
        answerRepository.save(new CaseAnswer(case104, "Q_LOCATION", "Where is the problem located?", "Substernal chest region radiating to left shoulder", InputType.TEXT));
        answerRepository.save(new CaseAnswer(case104, "Q_SEVERITY", "How severe is the problem?", "Severe (8/10)", InputType.TOUCH));
        answerRepository.save(new CaseAnswer(case104, "Q_ASSOCIATED_SYMPTOMS", "Do you have any associated symptoms?", "Breathing difficulty, mild perspiration", InputType.TEXT));
        answerRepository.save(new CaseAnswer(case104, "Q_PAST_DISEASES", "Previous medical conditions", "Diabetes Mellitus", InputType.TOUCH));
        answerRepository.save(new CaseAnswer(case104, "Q_MEDICATIONS", "Current medications", "Metformin 500 mg", InputType.TEXT));

        // 5. Seed Secondary Demo Patient: Priya Singh (Token 105, NORMAL)
        User priyaUser = new User("Priya Singh", "priya@gmail.com", passwordEncoder.encode("patient123"), Role.PATIENT);
        userRepository.save(priyaUser);
        Patient priyaPatient = new Patient(priyaUser, "P-10043", 31, "Female", "9876543212", "General Medicine", "English");
        priyaPatient.setConsentAccepted(true);
        patientRepository.save(priyaPatient);

        MedicalCase case105 = new MedicalCase(priyaPatient);
        case105.setCaseNumber("MK-2026-105");
        case105.setTokenNumber(105);
        case105.setStatus(CaseStatus.SUBMITTED);
        case105.setPriority(CasePriority.NORMAL);
        case105.setPriorityReason("Standard clinical intake. No acute predefined red flags detected.");
        case105.setChiefComplaint("Fever and mild dry cough for 3 days");
        case105.setPatientStatement("Having low grade fever since Thursday with mild throat irritation.");
        case105.setOnset("4–7 days (3 days)");
        case105.setLocation("Throat and systemic");
        case105.setSeverity("Moderate (4/10)");
        case105.setAssociatedSymptoms("Mild headache, nasal congestion");
        case105.setPastMedicalHistory("None");
        case105.setCurrentMedication("Paracetamol 650 mg SOS");
        case105.setAllergies("NKDA");
        case105.setSubmittedAt(LocalDateTime.now().minusMinutes(25));
        case105.setStructuredSummary(summaryService.generateStructuredSummary(case105, List.of()));
        case105.setMedicalTimeline("2026 (Today) | MediKiosk Intake: Presented with Fever and mild dry cough for 3 days");
        caseRepository.save(case105);

        // 6. Seed Tertiary Demo Patient: Amit Patel (Token 106, HIGH)
        User amitUser = new User("Amit Patel", "amit@gmail.com", passwordEncoder.encode("patient123"), Role.PATIENT);
        userRepository.save(amitUser);
        Patient amitPatient = new Patient(amitUser, "P-10044", 58, "Male", "9876543213", "Cardiology", "Hindi");
        amitPatient.setConsentAccepted(true);
        patientRepository.save(amitPatient);

        MedicalCase case106 = new MedicalCase(amitPatient);
        case106.setCaseNumber("MK-2026-106");
        case106.setTokenNumber(106);
        case106.setStatus(CaseStatus.SUBMITTED);
        case106.setPriority(CasePriority.HIGH);
        case106.setPriorityReason("Shortness of breath on mild exertion with known cardiovascular risk history");
        case106.setRedFlagsDetected(true);
        case106.setChiefComplaint("Shortness of breath on exertion and pedal edema");
        case106.setPatientStatement("Saans lene mein takleef ho rahi hai jab chalte hain.");
        case106.setOnset("1–3 days");
        case106.setLocation("Cardiorespiratory / Lower limbs");
        case106.setSeverity("Severe (7/10)");
        case106.setAssociatedSymptoms("Bilateral pedal swelling, orthopnea");
        case106.setPastMedicalHistory("Hypertension (10 years), Dyslipidemia");
        case106.setCurrentMedication("Telmisartan 40 mg, Atorvastatin 20 mg");
        case106.setAllergies("NKDA");
        case106.setSubmittedAt(LocalDateTime.now().minusMinutes(10));
        case106.setStructuredSummary(summaryService.generateStructuredSummary(case106, List.of()));
        case106.setMedicalTimeline("2016 | Essential Hypertension diagnosed\n2026 (Today) | MediKiosk Intake: Presented with Shortness of breath on exertion");
        caseRepository.save(case106);

        // 7. Seed Verified Case: Sunita Verma (Token 103, VERIFIED)
        User sunitaUser = new User("Sunita Verma", "sunita@gmail.com", passwordEncoder.encode("patient123"), Role.PATIENT);
        userRepository.save(sunitaUser);
        Patient sunitaPatient = new Patient(sunitaUser, "P-10041", 45, "Female", "9876543214", "General Medicine", "English");
        sunitaPatient.setConsentAccepted(true);
        patientRepository.save(sunitaPatient);

        MedicalCase case103 = new MedicalCase(sunitaPatient);
        case103.setCaseNumber("MK-2026-103");
        case103.setTokenNumber(103);
        case103.setStatus(CaseStatus.VERIFIED);
        case103.setPriority(CasePriority.NORMAL);
        case103.setPriorityReason("Standard clinical review");
        case103.setChiefComplaint("Routine Hypertension follow-up");
        case103.setPatientStatement("Came for monthly BP check and medicine renewal.");
        case103.setOnset("More than 1 week");
        case103.setSeverity("Mild (2/10)");
        case103.setPastMedicalHistory("Primary Hypertension");
        case103.setCurrentMedication("Amlodipine 5 mg OD");
        case103.setAllergies("NKDA");
        case103.setSubmittedAt(LocalDateTime.now().minusHours(3));
        case103.setDoctor(doctor);
        case103.setVerifiedByDoctor("Dr. Ananya Roy, MD");
        case103.setVerifiedAt(LocalDateTime.now().minusHours(1));
        case103.setDoctorClinicalNotes("BP stable at 124/80 mmHg. Advised continuation of Amlodipine 5 mg OD and salt restriction. Next follow-up in 3 months.");
        case103.setStructuredSummary(summaryService.generateStructuredSummary(case103, List.of()));
        case103.setSymptoms("Hypertension follow-up, occasional mild dizziness");
        case103.setDiagnosis("Essential Primary Hypertension (Stage 1)");
        case103.setTreatment("Tab. Amlodipine 5 mg OD, DASH diet, sodium restriction < 2g/day");
        case103.setVitals("BP: 124/80 mmHg, HR: 74 bpm, Temp: 98.6°F, SpO2: 99%");
        case103.setOutcome("Well controlled BP, asymptomatic, routine review in 3 months");
        caseRepository.save(case103);

        seedPastBenchmarkCases(doctor);
    }

    private void seedPastBenchmarkCases(Doctor doctor) {
        if (caseRepository.findByCaseNumber("MK-BENCH-01").isPresent()) {
            return;
        }

        // Benchmark Case 1: Acute Coronary Syndrome (Unstable Angina)
        User u1 = userRepository.findByEmail("vikram.sen@example.com").orElseGet(() -> {
            User u = new User("Vikram Sen", "vikram.sen@example.com", passwordEncoder.encode("patient123"), Role.PATIENT);
            return userRepository.save(u);
        });
        Patient p1 = patientRepository.findByUserId(u1.getId()).orElseGet(() -> {
            Patient p = new Patient(u1, "P-10081", 52, "Male", "9876540001", "Cardiology", "English");
            p.setBloodGroup("O+");
            p.setConsentAccepted(true);
            return patientRepository.save(p);
        });

        MedicalCase c1 = new MedicalCase(p1);
        c1.setCaseNumber("MK-BENCH-01");
        c1.setTokenNumber(201);
        c1.setStatus(CaseStatus.VERIFIED);
        c1.setPriority(CasePriority.CRITICAL);
        c1.setChiefComplaint("Severe retrosternal chest pain with left arm radiation");
        c1.setSymptoms("Chest pain, substernal chest discomfort, breathlessness, dyspnea, sweating, diaphoresis");
        c1.setAssociatedSymptoms("Shortness of breath, diaphoresis, chest tightness");
        c1.setPastMedicalHistory("Type 2 Diabetes Mellitus (5 yrs), Hyperlipidemia");
        c1.setCurrentMedication("Metformin 500 mg BD");
        c1.setDiagnosis("Acute Coronary Syndrome (Unstable Angina)");
        c1.setTreatment("Tab. Aspirin 325 mg STAT, Clopidogrel 300 mg STAT, Atorvastatin 80 mg, Sublingual Nitroglycerin 0.4 mg PRN, urgent coronary angiography");
        c1.setVitals("BP: 154/96 mmHg, HR: 98 bpm, Temp: 98.2°F, SpO2: 94%");
        c1.setOutcome("Stabilized post-PCI with drug-eluting stent to LAD, symptom-free at 30-day review");
        c1.setDoctor(doctor);
        c1.setVerifiedByDoctor("Dr. Ananya Roy, MD");
        c1.setVerifiedAt(LocalDateTime.now().minusDays(12));
        c1.setDoctorClinicalNotes("Coronary angiography showed 90% stenosis in proximal LAD. Drug eluting stent successfully placed. Advised dual antiplatelet therapy for 12 months.");
        caseRepository.save(c1);

        // Benchmark Case 2: Gastroesophageal Reflux Disease (GERD)
        User u2 = userRepository.findByEmail("meera.rao@example.com").orElseGet(() -> {
            User u = new User("Meera Rao", "meera.rao@example.com", passwordEncoder.encode("patient123"), Role.PATIENT);
            return userRepository.save(u);
        });
        Patient p2 = patientRepository.findByUserId(u2.getId()).orElseGet(() -> {
            Patient p = new Patient(u2, "P-10082", 44, "Female", "9876540002", "Gastroenterology", "English");
            p.setBloodGroup("A+");
            p.setConsentAccepted(true);
            return patientRepository.save(p);
        });

        MedicalCase c2 = new MedicalCase(p2);
        c2.setCaseNumber("MK-BENCH-02");
        c2.setTokenNumber(202);
        c2.setStatus(CaseStatus.VERIFIED);
        c2.setPriority(CasePriority.NORMAL);
        c2.setChiefComplaint("Burning chest pain and acid taste in mouth");
        c2.setSymptoms("Burning chest pain, heartburn, acid regurgitation, epigastric discomfort, nausea");
        c2.setAssociatedSymptoms("Belching, discomfort worsens on lying flat after meals");
        c2.setPastMedicalHistory("None");
        c2.setCurrentMedication("Antacid gel SOS");
        c2.setDiagnosis("Gastroesophageal Reflux Disease (GERD) with Reflux Esophagitis");
        c2.setTreatment("Cap. Pantoprazole 40 mg OD before breakfast for 4 weeks, Sucralfate suspension 10 ml TDS, head-end bed elevation, avoid late dinners");
        c2.setVitals("BP: 118/76 mmHg, HR: 72 bpm, Temp: 98.6°F, SpO2: 99%");
        c2.setOutcome("Complete symptom relief within 7 days. Endoscopy normal, maintenance as needed");
        c2.setDoctor(doctor);
        c2.setVerifiedByDoctor("Dr. Ananya Roy, MD");
        c2.setVerifiedAt(LocalDateTime.now().minusDays(8));
        c2.setDoctorClinicalNotes("Cardiac causes ruled out via normal ECG and negative Troponin. Classic reflux presentation responsive to PPI therapy.");
        caseRepository.save(c2);

        // Benchmark Case 3: Acute Bronchial Asthma Exacerbation
        User u3 = userRepository.findByEmail("rajesh.nair@example.com").orElseGet(() -> {
            User u = new User("Rajesh Nair", "rajesh.nair@example.com", passwordEncoder.encode("patient123"), Role.PATIENT);
            return userRepository.save(u);
        });
        Patient p3 = patientRepository.findByUserId(u3.getId()).orElseGet(() -> {
            Patient p = new Patient(u3, "P-10083", 36, "Male", "9876540003", "Pulmonology", "English");
            p.setBloodGroup("B+");
            p.setConsentAccepted(true);
            return patientRepository.save(p);
        });

        MedicalCase c3 = new MedicalCase(p3);
        c3.setCaseNumber("MK-BENCH-03");
        c3.setTokenNumber(203);
        c3.setStatus(CaseStatus.VERIFIED);
        c3.setPriority(CasePriority.HIGH);
        c3.setChiefComplaint("Shortness of breath and wheezing for 2 days");
        c3.setSymptoms("Shortness of breath, dyspnea, dry cough, wheezing, chest tightness");
        c3.setAssociatedSymptoms("Nocturnal breathlessness, cough with scanty sputum");
        c3.setPastMedicalHistory("Bronchial Asthma since childhood, allergic rhinitis");
        c3.setCurrentMedication("Salbutamol MDI PRN (frequent usage lately)");
        c3.setDiagnosis("Moderate Acute Exacerbation of Bronchial Asthma");
        c3.setTreatment("Nebulized Salbutamol 2.5 mg + Ipratropium 0.5 mg STAT, Inhaler Budesonide-Formoterol 200/6 mcg 2 puffs BD, Oral Prednisolone 30 mg daily for 5 days");
        c3.setVitals("BP: 126/80 mmHg, HR: 104 bpm, Temp: 98.4°F, SpO2: 93%");
        c3.setOutcome("Bronchospasm relieved, SpO2 improved to 98% on room air, peak flow returned to 88% predicted");
        c3.setDoctor(doctor);
        c3.setVerifiedByDoctor("Dr. Ananya Roy, MD");
        c3.setVerifiedAt(LocalDateTime.now().minusDays(5));
        c3.setDoctorClinicalNotes("Good clinical response to bronchodilators and systemic steroids. Instructed on proper MDI technique with spacer and asthma action plan.");
        caseRepository.save(c3);

        // Benchmark Case 4: Acute Febrile Illness / Suspected Dengue
        User u4 = userRepository.findByEmail("anita.deshmukh@example.com").orElseGet(() -> {
            User u = new User("Anita Deshmukh", "anita.deshmukh@example.com", passwordEncoder.encode("patient123"), Role.PATIENT);
            return userRepository.save(u);
        });
        Patient p4 = patientRepository.findByUserId(u4.getId()).orElseGet(() -> {
            Patient p = new Patient(u4, "P-10084", 29, "Female", "9876540004", "General Medicine", "English");
            p.setBloodGroup("AB+");
            p.setConsentAccepted(true);
            return patientRepository.save(p);
        });

        MedicalCase c4 = new MedicalCase(p4);
        c4.setCaseNumber("MK-BENCH-04");
        c4.setTokenNumber(204);
        c4.setStatus(CaseStatus.VERIFIED);
        c4.setPriority(CasePriority.NORMAL);
        c4.setChiefComplaint("High fever with chills and severe headache for 4 days");
        c4.setSymptoms("High fever, pyrexia, severe headache, retro-orbital pain, fatigue, body ache, chills");
        c4.setAssociatedSymptoms("Loss of appetite, mild nausea, joint pain");
        c4.setPastMedicalHistory("None");
        c4.setCurrentMedication("Paracetamol SOS");
        c4.setDiagnosis("Acute Viral Febrile Illness (Suspected Dengue without warning signs)");
        c4.setTreatment("Tab. Paracetamol 650 mg QID PRN, Oral Rehydration Solutions 2.5 - 3 Litres/day, absolute rest, avoid NSAIDs, daily CBC monitoring for platelet count");
        c4.setVitals("BP: 110/72 mmHg, HR: 86 bpm, Temp: 101.8°F, SpO2: 98%");
        c4.setOutcome("Fever resolved on day 6, platelet count stable at 185,000/uL, complete functional recovery");
        c4.setDoctor(doctor);
        c4.setVerifiedByDoctor("Dr. Ananya Roy, MD");
        c4.setVerifiedAt(LocalDateTime.now().minusDays(3));
        c4.setDoctorClinicalNotes("NS1 antigen positive. Hemodynamically stable, hematocrit maintained. No bleeding manifestations. Home management with close OPD review.");
        caseRepository.save(c4);

        // Benchmark Case 5: Acute Gastroenteritis / Food Poisoning
        if (caseRepository.findByCaseNumber("MK-BENCH-05").isEmpty()) {
            User u5 = userRepository.findByEmail("deepak.verma@example.com").orElseGet(() -> {
                User u = new User("Deepak Verma", "deepak.verma@example.com", passwordEncoder.encode("patient123"), Role.PATIENT);
                return userRepository.save(u);
            });
            Patient p5 = patientRepository.findByUserId(u5.getId()).orElseGet(() -> {
                Patient p = new Patient(u5, "P-10085", 38, "Male", "9876540005", "Gastroenterology", "Hindi");
                p.setBloodGroup("O+");
                p.setConsentAccepted(true);
                return patientRepository.save(p);
            });

            MedicalCase c5 = new MedicalCase(p5);
            c5.setCaseNumber("MK-BENCH-05");
            c5.setTokenNumber(205);
            c5.setStatus(CaseStatus.VERIFIED);
            c5.setPriority(CasePriority.NORMAL);
            c5.setChiefComplaint("Watery diarrhea and abdominal cramps for 2 days");
            c5.setSymptoms("Diarrhea, loose stools, abdominal pain, stomach cramps, nausea, vomiting, mild fever, fatigue");
            c5.setAssociatedSymptoms("Frequent bowel motions after eating outside food, dehydration signs");
            c5.setPastMedicalHistory("None");
            c5.setCurrentMedication("ORS solution");
            c5.setDiagnosis("Acute Infectious Gastroenteritis with Mild Dehydration");
            c5.setTreatment("Oral Rehydration Salts (ORS) ad libitum, Tab. Rifaximin 400 mg TDS for 3 days, Probiotic capsule (Lactobacillus) BD, Tab. Ondansetron 4 mg SOS for nausea");
            c5.setVitals("BP: 114/74 mmHg, HR: 84 bpm, Temp: 99.4°F, SpO2: 99%");
            c5.setOutcome("Complete cessation of loose stools within 48 hours, fully rehydrated");
            c5.setDoctor(doctor);
            c5.setVerifiedByDoctor("Dr. Ananya Roy, MD");
            c5.setVerifiedAt(LocalDateTime.now().minusDays(2));
            c5.setDoctorClinicalNotes("Stool routine showed no RBCs or ova. Responded quickly to gut-targeted antimicrobial and rehydration therapy.");
            caseRepository.save(c5);
        }

        // Benchmark Case 6: Acute Upper Respiratory Tract Infection (URTI) / Viral Pharyngitis
        if (caseRepository.findByCaseNumber("MK-BENCH-06").isEmpty()) {
            User u6 = userRepository.findByEmail("pooja.sharma@example.com").orElseGet(() -> {
                User u = new User("Pooja Sharma", "pooja.sharma@example.com", passwordEncoder.encode("patient123"), Role.PATIENT);
                return userRepository.save(u);
            });
            Patient p6 = patientRepository.findByUserId(u6.getId()).orElseGet(() -> {
                Patient p = new Patient(u6, "P-10086", 26, "Female", "9876540006", "ENT / General Medicine", "English");
                p.setBloodGroup("B+");
                p.setConsentAccepted(true);
                return patientRepository.save(p);
            });

            MedicalCase c6 = new MedicalCase(p6);
            c6.setCaseNumber("MK-BENCH-06");
            c6.setTokenNumber(206);
            c6.setStatus(CaseStatus.VERIFIED);
            c6.setPriority(CasePriority.NORMAL);
            c6.setChiefComplaint("Sore throat and runny nose with low grade fever for 3 days");
            c6.setSymptoms("Sore throat, pharyngitis, runny nose, nasal congestion, sneezing, mild cough, low grade fever");
            c6.setAssociatedSymptoms("Difficulty swallowing, malaise");
            c6.setPastMedicalHistory("None");
            c6.setCurrentMedication("Paracetamol 500mg SOS");
            c6.setDiagnosis("Acute Viral Pharyngitis / Upper Respiratory Tract Infection (URTI)");
            c6.setTreatment("Warm saline gargles TDS, Tab. Levocetirizine 5 mg + Montelukast 10 mg at bedtime, Paracetamol 650 mg SOS, steam inhalation BD, hydration");
            c6.setVitals("BP: 116/78 mmHg, HR: 76 bpm, Temp: 99.8°F, SpO2: 99%");
            c6.setOutcome("Fully recovered in 4 days. COVID-19 rapid antigen test negative");
            c6.setDoctor(doctor);
            c6.setVerifiedByDoctor("Dr. Ananya Roy, MD");
            c6.setVerifiedAt(LocalDateTime.now().minusDays(1));
            c6.setDoctorClinicalNotes("Erythematous posterior pharynx without tonsillar exudates or cervical lymphadenopathy. Centor score 1. Managed symptomatically.");
            caseRepository.save(c6);
        }
    }

    private void updateExistingCasesIfEmpty() {
        caseRepository.findByCaseNumber("MK-2026-104").ifPresent(c -> {
            if (c.getSymptoms() == null) {
                c.setSymptoms("Chest pain, substernal tightness, left shoulder radiation, shortness of breath, perspiration");
                c.setVitals("BP: 146/92 mmHg, HR: 98 bpm, Temp: 98.4°F, SpO2: 94%");
                caseRepository.save(c);
            }
        });
        caseRepository.findByCaseNumber("MK-2026-105").ifPresent(c -> {
            if (c.getSymptoms() == null) {
                c.setSymptoms("Fever, dry cough, mild headache, nasal congestion, throat irritation");
                c.setVitals("BP: 118/78 mmHg, HR: 82 bpm, Temp: 100.2°F, SpO2: 98%");
                caseRepository.save(c);
            }
        });
        caseRepository.findByCaseNumber("MK-2026-106").ifPresent(c -> {
            if (c.getSymptoms() == null) {
                c.setSymptoms("Shortness of breath on exertion, breathlessness, pedal edema, orthopnea");
                c.setVitals("BP: 158/96 mmHg, HR: 92 bpm, Temp: 98.6°F, SpO2: 92%");
                caseRepository.save(c);
            }
        });
        caseRepository.findByCaseNumber("MK-2026-103").ifPresent(c -> {
            if (c.getDiagnosis() == null) {
                c.setSymptoms("Hypertension follow-up, occasional mild dizziness");
                c.setDiagnosis("Essential Primary Hypertension (Stage 1)");
                c.setTreatment("Tab. Amlodipine 5 mg OD, DASH diet, sodium restriction < 2g/day");
                c.setVitals("BP: 124/80 mmHg, HR: 74 bpm, Temp: 98.6°F, SpO2: 99%");
                c.setOutcome("Well controlled BP, asymptomatic, routine review in 3 months");
                caseRepository.save(c);
            }
        });
    }
}
