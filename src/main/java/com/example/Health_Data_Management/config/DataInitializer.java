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
        // Upgrade columns to TEXT in database if necessary
        try (java.sql.Connection conn = dataSource.getConnection();
             java.sql.Statement stmt = conn.createStatement()) {
            String[] cols = {
                "digital_signature", "drug_interactions_json", "dashavidha_assessment",
                "doctor_clinical_notes", "structured_summary", "medical_timeline",
                "priority_reason", "red_flags_details", "patient_statement",
                "associated_symptoms", "past_medical_history", "current_medication", "investigations"
            };
            for (String col : cols) {
                try {
                    stmt.execute("ALTER TABLE medical_cases ALTER COLUMN " + col + " TYPE TEXT");
                } catch (SQLException ignored) {}
            }
        } catch (SQLException ignored) {}

        // Check if demo doctor already exists
        if (userRepository.findByEmail("doctor@medikiosk.com").isPresent()) {
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
        case103.setMedicalTimeline("2022 | Hypertension diagnosed\n2026 (Today) | Follow-up: Verified and advised maintenance");
        caseRepository.save(case103);
    }
}
