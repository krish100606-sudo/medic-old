#!/usr/bin/env python3
"""
MediKiosk Local ML Disease Inference Script
Integrates mainmodel Random Forest classifier for disease risk classification.
"""

import sys
import os
import json
import warnings
import argparse

warnings.filterwarnings("ignore")

DIR = os.path.dirname(os.path.abspath(__file__))

def get_model_assets():
    import joblib
    rf_path = os.path.join(DIR, "case_recognition_random_forest.pkl")
    ge_path = os.path.join(DIR, "gender_encoder.pkl")
    se_path = os.path.join(DIR, "symptom_encoder.pkl")
    fn_path = os.path.join(DIR, "feature_names.pkl")

    rf = joblib.load(rf_path)
    ge = joblib.load(ge_path)
    se = joblib.load(se_path)
    fn = joblib.load(fn_path)
    return rf, ge, se, fn

def predict(age, gender, symptoms):
    import numpy as np
    import pandas as pd

    rf, ge, se, fn = get_model_assets()

    # Standardize gender
    gender = gender.capitalize() if gender else "Other"
    if gender not in ["Male", "Female"]:
        gender = "Other"

    # Filter recognized symptoms
    clean_symptoms = []
    known_symptoms = set(se.classes_)
    for s in symptoms:
        s_clean = s.strip().lower()
        if s_clean in known_symptoms:
            clean_symptoms.append(s_clean)
        else:
            # Partial match check
            for ks in known_symptoms:
                if s_clean in ks or ks in s_clean:
                    clean_symptoms.append(ks)
                    break

    clean_symptoms = list(set(clean_symptoms))

    symptom_bin = se.transform([clean_symptoms])
    symptom_df = pd.DataFrame(symptom_bin, columns=se.classes_)

    gender_bin = ge.transform([[gender]])
    gender_df = pd.DataFrame(gender_bin, columns=[f"Gender_{g}" for g in ge.categories_[0]])

    df = pd.DataFrame([[int(age), len(clean_symptoms)]], columns=["Age", "Symptom_Count"])
    full_df = pd.concat([df, gender_df, symptom_df], axis=1)[fn]

    pred = rf.predict(full_df)
    proba = rf.predict_proba(full_df)[0]
    top_indices = np.argsort(proba)[::-1][:5]

    results = []
    for idx in top_indices:
        prob = float(proba[idx])
        if prob > 0.001:
            results.append({
                "disease": str(rf.classes_[idx]),
                "probability": round(prob, 4)
            })

    top_disease = str(pred[0])
    top_prob = float(proba[top_indices[0]]) if len(top_indices) > 0 else 0.0

    return {
        "top_disease": top_disease,
        "top_probability": round(top_prob, 4),
        "recognized_symptoms": clean_symptoms,
        "predictions": results
    }

def main():
    parser = argparse.ArgumentParser(description="MediKiosk Local ML Disease Inference")
    parser.add_argument("--age", type=int, default=35)
    parser.add_argument("--gender", type=str, default="Male")
    parser.add_argument("--symptoms", type=str, default="")
    parser.add_argument("--json", type=str, default="")

    args = parser.parse_args()

    if args.json:
        try:
            data = json.loads(args.json)
            age = data.get("age", 35)
            gender = data.get("gender", "Male")
            symptoms = data.get("symptoms", [])
            if isinstance(symptoms, str):
                symptoms = [s.strip() for s in symptoms.split(",")]
        except Exception as e:
            print(json.dumps({"error": str(e)}))
            sys.exit(1)
    else:
        age = args.age
        gender = args.gender
        symptoms = [s.strip() for s in args.symptoms.split(",") if s.strip()]

    try:
        res = predict(age, gender, symptoms)
        print(json.dumps(res))
    except Exception as e:
        print(json.dumps({"error": str(e)}))
        sys.exit(1)

if __name__ == "__main__":
    main()
