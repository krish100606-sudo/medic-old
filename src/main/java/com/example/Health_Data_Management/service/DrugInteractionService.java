package com.example.Health_Data_Management.service;

import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class DrugInteractionService {

    private static class Rule {
        String drugA;
        String drugB;
        String severity;
        String effect;
        String recommendation;

        Rule(String a, String b, String sev, String eff, String rec) {
            this.drugA = a.toLowerCase();
            this.drugB = b.toLowerCase();
            this.severity = sev;
            this.effect = eff;
            this.recommendation = rec;
        }

        boolean matches(String text) {
            String lower = text.toLowerCase();
            return lower.contains(drugA) && lower.contains(drugB);
        }
    }

    private final List<Rule> interactionRules = new ArrayList<>();

    public DrugInteractionService() {
        initRules();
    }

    private void initRules() {
        // Cardiovascular & Anticoagulant
        addRule("Warfarin", "Aspirin", "CRITICAL", "Severe risk of major bleeding and hemorrhage", "Avoid concurrent use unless indicated by cardiologist with close INR monitoring.");
        addRule("Clopidogrel", "Omeprazole", "HIGH", "Reduced antiplatelet efficacy of clopidogrel via CYP2C19 inhibition", "Consider Pantoprazole instead of Omeprazole.");
        addRule("Amiodarone", "Digoxin", "CRITICAL", "Doubles serum digoxin concentration leading to fatal digitalis toxicity", "Reduce digoxin dosage by 50% and monitor ECG and potassium.");
        addRule("Amlodipine", "Simvastatin", "HIGH", "Increased risk of myopathy and rhabdomyolysis", "Limit Simvastatin dose to 20 mg daily or switch to Atorvastatin.");
        addRule("ACE Inhibitor", "Spironolactone", "HIGH", "Severe hyperkalemia causing fatal cardiac arrhythmias", "Monitor serum potassium and renal function closely.");
        addRule("Ramipril", "Potassium", "HIGH", "Potassium retention leading to hyperkalemia", "Avoid potassium supplements unless strictly hypokalemic.");
        addRule("Enalapril", "Spironolactone", "HIGH", "Hyperkalemia and renal impairment", "Regularly assess serum electrolytes.");
        addRule("Metoprolol", "Verapamil", "CRITICAL", "Severe bradycardia, complete heart block, and cardiac failure", "Avoid concurrent IV or high-dose combination; monitor heart rate.");

        // Metabolic & Diabetes
        addRule("Metformin", "Contrast", "CRITICAL", "High risk of contrast-induced nephropathy and fatal lactic acidosis", "Withhold Metformin 48 hours before and after radiocontrast administration.");
        addRule("Metformin", "Alcohol", "HIGH", "Potentiation of metformin effect on lactate metabolism causing lactic acidosis", "Advise strict avoidance of heavy alcohol intake.");
        addRule("Glimepiride", "Fluconazole", "HIGH", "Profound and prolonged hypoglycemia due to CYP2C9 inhibition", "Monitor blood glucose frequently; adjust sulfonylurea dose.");
        addRule("Glibenclamide", "Ciprofloxacin", "MODERATE", "Increased hypoglycemia risk due to CYP2C9 interaction", "Advise patient regarding hypoglycemic symptoms and glucose checks.");

        // Antimicrobials & CNS
        addRule("Ciprofloxacin", "Antacid", "MODERATE", "Chelation reduces ciprofloxacin bioavailability by up to 90%", "Separate administration by at least 2 hours before or 6 hours after antacids.");
        addRule("Azithromycin", "Amiodarone", "CRITICAL", "Prolongation of QT interval and fatal Torsades de Pointes", "Avoid combination or perform serial ECG monitoring.");
        addRule("Levofloxacin", "Prednisolone", "HIGH", "Significantly increased risk of severe tendinitis and tendon rupture", "Avoid combination, especially in elderly and athletic patients.");
        addRule("Linezolid", "Fluoxetine", "CRITICAL", "Risk of life-threatening Serotonin Syndrome due to MAO inhibition", "Do not administer concurrently; allow 2-week washout period.");
        addRule("Metronidazole", "Alcohol", "HIGH", "Severe disulfiram-like reaction (flushing, nausea, vomiting, tachycardia)", "Strictly avoid alcohol during therapy and for 48 hours post-completion.");
        addRule("Doxycycline", "Iron", "MODERATE", "Iron salts significantly decrease tetracycline absorption", "Separate intake by at least 2-3 hours.");

        // Analgesics & NSAIDs
        addRule("Ibuprofen", "Aspirin", "HIGH", "Ibuprofen antagonizes irreversible platelet inhibition of low-dose aspirin", "Take aspirin at least 30 minutes before or 8 hours after ibuprofen.");
        addRule("Diclofenac", "Prednisolone", "HIGH", "Markedly increased risk of gastrointestinal ulceration and bleeding", "Co-prescribe PPI (e.g., Pantoprazole) and monitor for melena.");
        addRule("Tramadol", "Sertraline", "CRITICAL", "Risk of Serotonin Syndrome and lowered seizure threshold", "Monitor for agitation, clonus, hyperthermia; adjust doses.");
        addRule("Paracetamol", "Alcohol", "HIGH", "Increased hepatotoxicity risk through CYP2E1 induction producing NAPQI", "Limit paracetamol dose to < 2g/day in chronic alcohol users.");

        // Psych & Neuro
        addRule("Lithium", "Ibuprofen", "HIGH", "NSAIDs reduce renal clearance of lithium, inducing toxic serum levels", "Monitor lithium levels closely or use alternative analgesics like Paracetamol.");
        addRule("Carbamazepine", "Erythromycin", "CRITICAL", "Erythromycin inhibits carbamazepine metabolism causing acute toxicity", "Use azithromycin instead, which does not inhibit CYP3A4.");
        addRule("Phenytoin", "Fluconazole", "HIGH", "Fluconazole elevates phenytoin levels leading to cerebellar ataxia", "Check phenytoin serum levels and adjust maintenance dose.");

        // Respiratory & Miscellaneous
        addRule("Theophylline", "Ciprofloxacin", "HIGH", "Ciprofloxacin inhibits theophylline clearance causing seizures and tachycardia", "Reduce theophylline dose by 50% and track blood levels.");
        addRule("Methotrexate", "Diclofenac", "CRITICAL", "Severe bone marrow suppression and renal failure due to reduced clearance", "Avoid NSAIDs during high-dose methotrexate therapy.");
        addRule("Thyroxine", "Calcium", "MODERATE", "Calcium carbonate binds levothyroxine in gut, decreasing absorption", "Separate doses by at least 4 hours.");
        addRule("Atorvastatin", "Clarithromycin", "HIGH", "Strong CYP3A4 inhibition elevates statin levels risking rhabdomyolysis", "Temporarily suspend statin while taking clarithromycin.");
        addRule("Sildenafil", "Nitroglycerin", "CRITICAL", "Profound, life-threatening systemic hypotension and cardiovascular collapse", "Absolute contraindication: do not administer within 24 hours of each other.");
    }

    private void addRule(String a, String b, String sev, String eff, String rec) {
        interactionRules.add(new Rule(a, b, sev, eff, rec));
    }

    public List<DrugInteraction> checkInteractions(String medicationText) {
        List<DrugInteraction> detected = new ArrayList<>();
        if (medicationText == null || medicationText.trim().isEmpty()) {
            return detected;
        }

        for (Rule r : interactionRules) {
            if (r.matches(medicationText)) {
                detected.add(new DrugInteraction(r.drugA, r.drugB, r.severity, r.effect, r.recommendation));
            }
        }

        return detected;
    }

    public List<DrugInteraction> checkPairwise(List<String> medications) {
        List<DrugInteraction> detected = new ArrayList<>();
        if (medications == null || medications.size() < 2) return detected;

        String combined = String.join(" ", medications);
        return checkInteractions(combined);
    }
}
