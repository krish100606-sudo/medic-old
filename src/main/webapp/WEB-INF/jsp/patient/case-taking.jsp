<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Clinical Case Intake — Question ${currentStep} of ${totalSteps} — MediKiosk</title>
    <!-- Bootstrap 5 CSS -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <!-- Bootstrap Icons (No Emojis) -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css" rel="stylesheet">
    <!-- MediKiosk Design System -->
    <link href="/css/medikiosk.css" rel="stylesheet">
</head>
<body class="bg-light">

    <!-- Top Navigation -->
    <header class="mk-navbar sticky-top">
        <div class="container-fluid px-4 d-flex justify-content-between align-items-center">
            <a class="mk-brand" href="/patient/dashboard">
                <i class="bi bi-hospital text-primary fs-4"></i>
                <span>MediKiosk</span>
                <span class="mk-brand-badge">Clinical Intake</span>
            </a>
            <div class="d-flex align-items-center gap-3">
                <span class="badge bg-light text-dark border"><i class="bi bi-translate me-1"></i> ${lang}</span>
                <button type="button" class="btn btn-sm btn-outline-primary d-lg-none" data-bs-toggle="collapse" data-bs-target="#conversationSidebarMobile">
                    <i class="bi bi-chat-left-dots"></i> Transcript (${not empty conversationMessages ? conversationMessages.size() : 0})
                </button>
                <a href="/patient/dashboard" class="btn mk-btn mk-btn-secondary btn-sm">
                    <i class="bi bi-door-closed"></i> Save & Exit
                </a>
            </div>
        </div>
    </header>

    <!-- Intake Container with 2-Column Responsive Layout -->
    <main class="container-fluid px-4 py-4">
        <div class="row g-4 justify-content-center">

            <!-- Primary Guided Intake Column -->
            <div class="col-lg-8 col-xl-7">

                <!-- Progress Header -->
                <div class="mb-4">
                    <div class="d-flex justify-content-between align-items-center mb-2">
                        <span class="fw-bold text-primary small text-uppercase">
                            Question ${currentStep} of ${totalSteps}
                            <c:if test="${not empty currentQuestion and fn:startsWith(currentQuestion.questionCode, 'Q_ADAPTIVE_')}">
                                <span class="badge bg-primary-subtle text-primary border border-primary-subtle ms-2">
                                    <i class="bi bi-robot"></i> Adaptive
                                </span>
                            </c:if>
                        </span>
                        <div class="d-flex align-items-center gap-2">
                            <button type="button" class="btn btn-sm btn-light border py-0 px-2 small text-primary" onclick="readCurrentQuestion()" title="Listen to question">
                                <i class="bi bi-volume-up-fill"></i> Read Aloud
                            </button>
                            <span class="text-muted small">${not empty progressPercent ? progressPercent : 10}% Completed</span>
                        </div>
                    </div>
                    <div class="progress" style="height: 8px;">
                        <div class="progress-bar bg-primary progress-bar-striped progress-bar-animated" role="progressbar" style="width: ${not empty progressPercent ? progressPercent : 10}%;"></div>
                    </div>
                </div>

                <!-- Guided Question Card -->
                <div class="mk-card p-4 p-md-5">

                    <form action="/patient/case-taking/save-step" method="POST" id="intakeForm">
                        <input type="hidden" name="step" value="${currentStep}">
                        <input type="hidden" name="inputType" id="inputType" value="TEXT">

                        <!-- ---------------- STEP 1: CHIEF COMPLAINT ---------------- -->
                        <c:if test="${currentQuestion.questionCode == 'Q_CHIEF_COMPLAINT' || (empty currentQuestion and currentStep == 1)}">
                            <input type="hidden" name="questionCode" value="Q_CHIEF_COMPLAINT">
                            <input type="hidden" name="questionText" value="What is your main health problem?">

                            <div class="text-center mb-4">
                                <h3 class="fw-bold text-dark mb-1" id="questionTitleEn">What is your main health problem?</h3>
                                <h5 class="text-muted fw-normal" id="questionTitleHi">आपकी मुख्य स्वास्थ्य समस्या क्या है?</h5>
                                <p class="text-muted small">Select from common issues or speak / type your symptom below.</p>
                            </div>

                            <!-- Fast Touch Options -->
                            <div class="row g-2 mb-4">
                                <div class="col-6 col-md-4">
                                    <button type="button" class="mk-touch-option w-100 ${medicalCase.chiefComplaint == 'Chest Pain' ? 'selected' : ''}" onclick="selectAnswer('Chest Pain', 'TOUCH', event)">
                                        <span>Chest Pain / छाती में दर्द</span>
                                        <i class="bi bi-heart-pulse text-danger"></i>
                                    </button>
                                </div>
                                <div class="col-6 col-md-4">
                                    <button type="button" class="mk-touch-option w-100 ${medicalCase.chiefComplaint == 'Fever and Cough' ? 'selected' : ''}" onclick="selectAnswer('Fever and Cough', 'TOUCH', event)">
                                        <span>Fever & Cough / बुखार-खांसी</span>
                                        <i class="bi bi-thermometer-high text-warning"></i>
                                    </button>
                                </div>
                                <div class="col-6 col-md-4">
                                    <button type="button" class="mk-touch-option w-100 ${medicalCase.chiefComplaint == 'Shortness of Breath' ? 'selected' : ''}" onclick="selectAnswer('Shortness of Breath', 'TOUCH', event)">
                                        <span>Breathlessness / सांस फूलना</span>
                                        <i class="bi bi-lungs text-primary"></i>
                                    </button>
                                </div>
                                <div class="col-6 col-md-4">
                                    <button type="button" class="mk-touch-option w-100 ${medicalCase.chiefComplaint == 'Severe Headache' ? 'selected' : ''}" onclick="selectAnswer('Severe Headache', 'TOUCH', event)">
                                        <span>Severe Headache / तेज सिरदर्द</span>
                                        <i class="bi bi-activity text-secondary"></i>
                                    </button>
                                </div>
                                <div class="col-6 col-md-4">
                                    <button type="button" class="mk-touch-option w-100 ${medicalCase.chiefComplaint == 'Abdominal Pain' ? 'selected' : ''}" onclick="selectAnswer('Abdominal Pain', 'TOUCH', event)">
                                        <span>Abdominal Pain / पेट दर्द</span>
                                        <i class="bi bi-slash-circle text-info"></i>
                                    </button>
                                </div>
                                <div class="col-6 col-md-4">
                                    <button type="button" class="mk-touch-option w-100 ${medicalCase.chiefComplaint == 'Joint Pain / Injury' ? 'selected' : ''}" onclick="selectAnswer('Joint Pain / Injury', 'TOUCH', event)">
                                        <span>Joint Pain / जोड़ों का दर्द</span>
                                        <i class="bi bi-bandaid text-success"></i>
                                    </button>
                                </div>
                            </div>

                            <div class="mb-4">
                                <label class="form-label fw-semibold small text-muted">Or type / speak your complaint:</label>
                                <div class="input-group">
                                    <input type="text" class="form-control form-control-lg" id="answerInput" name="answerText" value="${medicalCase.chiefComplaint}" placeholder="e.g. Chest Pain or Fever" required>
                                    <button type="button" class="mk-mic-btn ms-2" id="micBtn" title="Speak in English or Hindi">
                                        <i class="bi bi-mic-fill"></i>
                                    </button>
                                </div>
                                <div class="d-flex align-items-center gap-2 mt-2">
                                    <div class="mk-waveform" id="voiceWaveform">
                                        <span class="mk-waveform-bar"></span>
                                        <span class="mk-waveform-bar"></span>
                                        <span class="mk-waveform-bar"></span>
                                        <span class="mk-waveform-bar"></span>
                                        <span class="mk-waveform-bar"></span>
                                    </div>
                                    <div id="voiceStatus" class="small text-muted">Click microphone to speak (English or Hindi).</div>
                                </div>
                            </div>
                        </c:if>

                        <!-- ---------------- STEP 2: PATIENT VERBATIM STATEMENT ---------------- -->
                        <c:if test="${currentQuestion.questionCode == 'Q_STATEMENT' || (empty currentQuestion and currentStep == 2)}">
                            <input type="hidden" name="questionCode" value="Q_STATEMENT">
                            <input type="hidden" name="questionText" value="Please describe how you feel in your own words">

                            <div class="text-center mb-4">
                                <h3 class="fw-bold text-dark mb-1" id="questionTitleEn">Describe your symptoms in your own words</h3>
                                <h5 class="text-muted fw-normal" id="questionTitleHi">कृपया अपनी भाषा में अपनी समस्या बताएं</h5>
                                <p class="text-muted small">You can speak in Hindi or English (e.g. "Mujhe do ghante se chest mein pain ho raha hai").</p>
                            </div>

                            <div class="mb-4">
                                <div class="d-flex justify-content-between align-items-center mb-2">
                                    <label class="form-label fw-semibold small text-muted mb-0">Your Spoken / Written Statement:</label>
                                    <button type="button" class="btn btn-sm btn-outline-primary" onclick="setDemoVoiceSample()">
                                        <i class="bi bi-magic"></i> Auto-fill Sample Voice
                                    </button>
                                </div>
                                <div class="position-relative">
                                    <textarea class="form-control" id="answerInput" name="answerText" rows="4" placeholder="Speak or type your symptoms freely..." required>${medicalCase.patientStatement}</textarea>
                                </div>
                                <div class="d-flex align-items-center gap-3 mt-3">
                                    <button type="button" class="mk-mic-btn" id="micBtn">
                                        <i class="bi bi-mic-fill"></i>
                                    </button>
                                    <div>
                                        <div class="fw-semibold small">Microphone Voice Input</div>
                                        <div class="d-flex align-items-center gap-2">
                                            <div class="mk-waveform" id="voiceWaveform">
                                                <span class="mk-waveform-bar"></span>
                                                <span class="mk-waveform-bar"></span>
                                                <span class="mk-waveform-bar"></span>
                                                <span class="mk-waveform-bar"></span>
                                                <span class="mk-waveform-bar"></span>
                                            </div>
                                            <div id="voiceStatus" class="small text-muted">Tap mic and speak in English or Hindi.</div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </c:if>

                        <!-- ---------------- STEP 3: ONSET / DURATION ---------------- -->
                        <c:if test="${currentQuestion.questionCode == 'Q_ONSET' || (empty currentQuestion and currentStep == 3)}">
                            <input type="hidden" name="questionCode" value="Q_ONSET">
                            <input type="hidden" name="questionText" value="When did the problem start?">

                            <div class="text-center mb-4">
                                <h3 class="fw-bold text-dark mb-1" id="questionTitleEn">When did the problem start?</h3>
                                <h5 class="text-muted fw-normal" id="questionTitleHi">यह समस्या कब शुरू हुई?</h5>
                                <p class="text-muted small">Select the duration of your symptoms.</p>
                            </div>

                            <input type="hidden" id="answerInput" name="answerText" value="${medicalCase.onset}">

                            <div class="d-grid gap-3 mb-4">
                                <button type="button" class="mk-touch-option p-3 ${medicalCase.onset == 'Today' || empty medicalCase.onset ? 'selected' : ''}" onclick="selectAnswer('Today', 'TOUCH', event)">
                                    <div>
                                        <div class="fw-bold">Today / आज (Few hours ago)</div>
                                        <div class="small text-muted">Acute onset started within the last 24 hours</div>
                                    </div>
                                    <i class="bi bi-check-lg fs-4 text-primary"></i>
                                </button>
                                <button type="button" class="mk-touch-option p-3 ${medicalCase.onset == '1–3 days' ? 'selected' : ''}" onclick="selectAnswer('1–3 days', 'TOUCH', event)">
                                    <div>
                                        <div class="fw-bold">1–3 days / 1-3 दिन</div>
                                        <div class="small text-muted">Started over the past couple of days</div>
                                    </div>
                                    <i class="bi bi-check-lg fs-4 text-primary"></i>
                                </button>
                                <button type="button" class="mk-touch-option p-3 ${medicalCase.onset == '4–7 days' ? 'selected' : ''}" onclick="selectAnswer('4–7 days', 'TOUCH', event)">
                                    <div>
                                        <div class="fw-bold">4–7 days / 4-7 दिन</div>
                                        <div class="small text-muted">Persistent over roughly one week</div>
                                    </div>
                                    <i class="bi bi-check-lg fs-4 text-primary"></i>
                                </button>
                                <button type="button" class="mk-touch-option p-3 ${medicalCase.onset == 'More than 1 week' ? 'selected' : ''}" onclick="selectAnswer('More than 1 week', 'TOUCH', event)">
                                    <div>
                                        <div class="fw-bold">More than 1 week / 1 सप्ताह से अधिक</div>
                                        <div class="small text-muted">Chronic or long-standing discomfort</div>
                                    </div>
                                    <i class="bi bi-check-lg fs-4 text-primary"></i>
                                </button>
                            </div>
                        </c:if>

                        <!-- ---------------- STEP 4: LOCATION ---------------- -->
                        <c:if test="${currentQuestion.questionCode == 'Q_LOCATION' || (empty currentQuestion and currentStep == 4)}">
                            <input type="hidden" name="questionCode" value="Q_LOCATION">
                            <input type="hidden" name="questionText" value="Where is the problem located?">

                            <div class="text-center mb-4">
                                <h3 class="fw-bold text-dark mb-1" id="questionTitleEn">Where is the problem located?</h3>
                                <h5 class="text-muted fw-normal" id="questionTitleHi">समस्या शरीर के किस भाग में है?</h5>
                                <p class="text-muted small">Select the anatomical area or specify details.</p>
                            </div>

                            <div class="row g-2 mb-3">
                                <div class="col-6">
                                    <button type="button" class="mk-touch-option w-100 ${medicalCase.location == 'Substernal chest region radiating to left shoulder' ? 'selected' : ''}" onclick="selectAnswer('Substernal chest region radiating to left shoulder', 'TOUCH', event)">
                                        <span>Chest / Heart (छाती)</span>
                                        <i class="bi bi-heart"></i>
                                    </button>
                                </div>
                                <div class="col-6">
                                    <button type="button" class="mk-touch-option w-100 ${medicalCase.location == 'Throat and Upper Respiratory' ? 'selected' : ''}" onclick="selectAnswer('Throat and Upper Respiratory', 'TOUCH', event)">
                                        <span>Throat / Neck (गला)</span>
                                        <i class="bi bi-activity"></i>
                                    </button>
                                </div>
                                <div class="col-6">
                                    <button type="button" class="mk-touch-option w-100 ${medicalCase.location == 'Upper Abdomen / Stomach' ? 'selected' : ''}" onclick="selectAnswer('Upper Abdomen / Stomach', 'TOUCH', event)">
                                        <span>Abdomen / Stomach (पेट)</span>
                                        <i class="bi bi-slash-circle"></i>
                                    </button>
                                </div>
                                <div class="col-6">
                                    <button type="button" class="mk-touch-option w-100 ${medicalCase.location == 'Head / Cranial' ? 'selected' : ''}" onclick="selectAnswer('Head / Cranial', 'TOUCH', event)">
                                        <span>Head / Eyes (सिर/आंख)</span>
                                        <i class="bi bi-person"></i>
                                    </button>
                                </div>
                            </div>

                            <div class="mb-4">
                                <label class="form-label fw-semibold small text-muted">Specific location description:</label>
                                <input type="text" class="form-control" id="answerInput" name="answerText" value="${medicalCase.location}" required>
                            </div>
                        </c:if>

                        <!-- ---------------- STEP 5: SEVERITY ---------------- -->
                        <c:if test="${currentQuestion.questionCode == 'Q_SEVERITY' || (empty currentQuestion and currentStep == 5)}">
                            <input type="hidden" name="questionCode" value="Q_SEVERITY">
                            <input type="hidden" name="questionText" value="How severe is the problem?">

                            <div class="text-center mb-4">
                                <h3 class="fw-bold text-dark mb-1" id="questionTitleEn">How severe is the problem?</h3>
                                <h5 class="text-muted fw-normal" id="questionTitleHi">तकलीफ कितनी गंभीर है?</h5>
                                <p class="text-muted small">Choose the intensity level that best describes your discomfort.</p>
                            </div>

                            <input type="hidden" id="answerInput" name="answerText" value="${medicalCase.severity}">

                            <div class="d-grid gap-3 mb-4">
                                <button type="button" class="mk-touch-option p-3 ${medicalCase.severity == 'Mild (1-3/10)' ? 'selected' : ''}" onclick="selectAnswer('Mild (1-3/10)', 'TOUCH', event)">
                                    <div>
                                        <div class="fw-bold text-success">Mild (1–3 / 10) / हल्का</div>
                                        <div class="small text-muted">Noticeable but does not disrupt routine activities</div>
                                    </div>
                                    <span class="badge bg-success bg-opacity-10 text-success">Mild</span>
                                </button>
                                <button type="button" class="mk-touch-option p-3 ${medicalCase.severity == 'Moderate (4-6/10)' ? 'selected' : ''}" onclick="selectAnswer('Moderate (4-6/10)', 'TOUCH', event)">
                                    <div>
                                        <div class="fw-bold text-warning">Moderate (4–6 / 10) / मध्यम</div>
                                        <div class="small text-muted">Uncomfortable and interferes with daily tasks</div>
                                    </div>
                                    <span class="badge bg-warning bg-opacity-10 text-warning">Moderate</span>
                                </button>
                                <button type="button" class="mk-touch-option p-3 ${medicalCase.severity == 'Severe (8/10)' || empty medicalCase.severity ? 'selected' : ''}" onclick="selectAnswer('Severe (8/10)', 'TOUCH', event)">
                                    <div>
                                        <div class="fw-bold text-danger">Severe (7–8 / 10) / तीव्र</div>
                                        <div class="small text-muted">Severe distress, requires prompt medical evaluation</div>
                                    </div>
                                    <span class="badge bg-danger bg-opacity-10 text-danger">Severe</span>
                                </button>
                                <button type="button" class="mk-touch-option p-3 ${medicalCase.severity == 'Critical (9-10/10)' ? 'selected' : ''}" onclick="selectAnswer('Critical (9-10/10)', 'TOUCH', event)">
                                    <div>
                                        <div class="fw-bold text-danger">Critical (9–10 / 10) / अति गंभीर</div>
                                        <div class="small text-muted">Extremely acute unbearable pain</div>
                                    </div>
                                    <span class="badge bg-danger text-white">Critical</span>
                                </button>
                            </div>
                        </c:if>

                        <!-- ---------------- STEP 6: ASSOCIATED SYMPTOMS ---------------- -->
                        <c:if test="${currentQuestion.questionCode == 'Q_ASSOCIATED_SYMPTOMS' || (empty currentQuestion and currentStep == 6)}">
                            <input type="hidden" name="questionCode" value="Q_ASSOCIATED_SYMPTOMS">
                            <input type="hidden" name="questionText" value="Do you have any associated symptoms?">

                            <div class="text-center mb-4">
                                <h3 class="fw-bold text-dark mb-1" id="questionTitleEn">Do you have any associated symptoms?</h3>
                                <h5 class="text-muted fw-normal" id="questionTitleHi">क्या आपको कोई अन्य लक्षण हैं?</h5>
                                <p class="text-muted small">Select all symptoms you are currently experiencing alongside the main complaint.</p>
                            </div>

                            <c:if test="${not empty currentQuestion.touchOptions}">
                                <div class="row g-2 mb-3">
                                    <c:forEach var="opt" items="${currentQuestion.touchOptions}">
                                        <div class="col-12 col-md-6">
                                            <button type="button" class="mk-touch-option w-100" onclick="toggleSymptomOption('${opt}', this)">
                                                <span>${opt}</span>
                                                <i class="bi bi-plus-circle text-primary"></i>
                                            </button>
                                        </div>
                                    </c:forEach>
                                </div>
                            </c:if>

                            <div class="mb-4">
                                <label class="form-label fw-semibold small text-muted">Summary of symptoms (editable):</label>
                                <input type="text" class="form-control form-control-lg" id="answerInput" name="answerText" value="${medicalCase.associatedSymptoms}" required>
                            </div>
                        </c:if>

                        <!-- ---------------- ADAPTIVE CLINICAL BRANCHING (Steps 7+) ---------------- -->
                        <c:if test="${not empty currentQuestion and fn:startsWith(currentQuestion.questionCode, 'Q_ADAPTIVE_')}">
                            <input type="hidden" name="questionCode" value="${currentQuestion.questionCode}">
                            <input type="hidden" name="questionText" value="${currentQuestion.questionTextEn}">

                            <div class="text-center mb-4">
                                <span class="badge bg-primary-subtle text-primary border border-primary-subtle px-3 py-1 mb-2">
                                    <i class="bi bi-robot me-1"></i> AI Adaptive Follow-Up Question
                                </span>
                                <h3 class="fw-bold text-dark mb-1" id="questionTitleEn">${currentQuestion.questionTextEn}</h3>
                                <h5 class="text-muted fw-normal" id="questionTitleHi">${currentQuestion.questionTextHi}</h5>
                                <p class="text-muted small">Customized follow-up derived from your complaint (${medicalCase.chiefComplaint}).</p>
                            </div>

                            <c:if test="${not empty currentQuestion.touchOptions}">
                                <div class="row g-2 mb-4">
                                    <c:forEach var="opt" items="${currentQuestion.touchOptions}">
                                        <div class="col-12 col-md-6">
                                            <button type="button" class="mk-touch-option w-100" onclick="selectAnswer('${opt}', 'TOUCH', event)">
                                                <span>${opt}</span>
                                                <i class="bi bi-chevron-right text-primary"></i>
                                            </button>
                                        </div>
                                    </c:forEach>
                                </div>
                            </c:if>

                            <div class="mb-4">
                                <label class="form-label fw-semibold small text-muted">Your Response / आपकी प्रतिक्रिया:</label>
                                <div class="input-group">
                                    <input type="text" class="form-control form-control-lg" id="answerInput" name="answerText" placeholder="Select above or type / speak..." required>
                                    <c:if test="${currentQuestion.allowVoice}">
                                        <button type="button" class="mk-mic-btn ms-2" id="micBtn" title="Speak in English or Hindi">
                                            <i class="bi bi-mic-fill"></i>
                                        </button>
                                    </c:if>
                                </div>
                                <div class="d-flex align-items-center gap-2 mt-2">
                                    <div class="mk-waveform" id="voiceWaveform">
                                        <span class="mk-waveform-bar"></span>
                                        <span class="mk-waveform-bar"></span>
                                        <span class="mk-waveform-bar"></span>
                                        <span class="mk-waveform-bar"></span>
                                        <span class="mk-waveform-bar"></span>
                                    </div>
                                    <div id="voiceStatus" class="small text-muted">Click microphone to speak in English or Hindi.</div>
                                </div>
                            </div>
                        </c:if>

                        <!-- ---------------- PAST MEDICAL HISTORY ---------------- -->
                        <c:if test="${currentQuestion.questionCode == 'Q_PAST_DISEASES' || (empty currentQuestion and currentStep == 7)}">
                            <input type="hidden" name="questionCode" value="Q_PAST_DISEASES">
                            <input type="hidden" name="questionText" value="Previous medical conditions">

                            <div class="text-center mb-4">
                                <h3 class="fw-bold text-dark mb-1" id="questionTitleEn">Previous Medical Conditions</h3>
                                <h5 class="text-muted fw-normal" id="questionTitleHi">पिछली या पुरानी बीमारियाँ</h5>
                                <p class="text-muted small">Do you have any ongoing or diagnosed health conditions?</p>
                            </div>

                            <div class="row g-2 mb-3">
                                <div class="col-6 col-md-4">
                                    <button type="button" class="mk-touch-option w-100 ${medicalCase.pastMedicalHistory == 'Type 2 Diabetes Mellitus' ? 'selected' : ''}" onclick="selectAnswer('Type 2 Diabetes Mellitus (since 2024)', 'TOUCH', event)">
                                        <span>Diabetes / मधुमेह</span>
                                        <i class="bi bi-droplet"></i>
                                    </button>
                                </div>
                                <div class="col-6 col-md-4">
                                    <button type="button" class="mk-touch-option w-100 ${medicalCase.pastMedicalHistory == 'Hypertension (High BP)' ? 'selected' : ''}" onclick="selectAnswer('Hypertension (High BP)', 'TOUCH', event)">
                                        <span>High BP / रक्तचाप</span>
                                        <i class="bi bi-speedometer2"></i>
                                    </button>
                                </div>
                                <div class="col-6 col-md-4">
                                    <button type="button" class="mk-touch-option w-100 ${medicalCase.pastMedicalHistory == 'Asthma / Respiratory' ? 'selected' : ''}" onclick="selectAnswer('Asthma / Respiratory', 'TOUCH', event)">
                                        <span>Asthma / दमा</span>
                                        <i class="bi bi-lungs"></i>
                                    </button>
                                </div>
                                <div class="col-6 col-md-4">
                                    <button type="button" class="mk-touch-option w-100 ${medicalCase.pastMedicalHistory == 'Thyroid Disorder' ? 'selected' : ''}" onclick="selectAnswer('Thyroid Disorder', 'TOUCH', event)">
                                        <span>Thyroid / थायराइड</span>
                                        <i class="bi bi-circle"></i>
                                    </button>
                                </div>
                                <div class="col-6 col-md-4">
                                    <button type="button" class="mk-touch-option w-100 ${medicalCase.pastMedicalHistory == 'None' ? 'selected' : ''}" onclick="selectAnswer('None', 'TOUCH', event)">
                                        <span>None / कोई नहीं</span>
                                        <i class="bi bi-check-circle"></i>
                                    </button>
                                </div>
                            </div>

                            <div class="mb-4">
                                <label class="form-label fw-semibold small text-muted">Details or year diagnosed:</label>
                                <input type="text" class="form-control" id="answerInput" name="answerText" value="${medicalCase.pastMedicalHistory}" required>
                            </div>
                        </c:if>

                        <!-- ---------------- SURGICAL HISTORY ---------------- -->
                        <c:if test="${currentQuestion.questionCode == 'Q_SURGERIES' || (empty currentQuestion and currentStep == 8)}">
                            <input type="hidden" name="questionCode" value="Q_SURGERIES">
                            <input type="hidden" name="questionText" value="Previous surgeries or hospitalizations">

                            <div class="text-center mb-4">
                                <h3 class="fw-bold text-dark mb-1" id="questionTitleEn">Previous Surgeries</h3>
                                <h5 class="text-muted fw-normal" id="questionTitleHi">पिछली सर्जरी या अस्पताल में भर्ती</h5>
                                <p class="text-muted small">Have you undergone any major surgery in the past?</p>
                            </div>

                            <div class="row g-2 mb-3">
                                <div class="col-6">
                                    <button type="button" class="mk-touch-option w-100" onclick="selectAnswer('Laparoscopic Appendectomy (2023)', 'TOUCH', event)">
                                        <span>Appendectomy (2023)</span>
                                    </button>
                                </div>
                                <div class="col-6">
                                    <button type="button" class="mk-touch-option w-100" onclick="selectAnswer('Gallbladder Surgery (Cholecystectomy)', 'TOUCH', event)">
                                        <span>Gallbladder Surgery</span>
                                    </button>
                                </div>
                                <div class="col-6">
                                    <button type="button" class="mk-touch-option w-100" onclick="selectAnswer('Cardiac / Stent Procedure', 'TOUCH', event)">
                                        <span>Cardiac Stent</span>
                                    </button>
                                </div>
                                <div class="col-6">
                                    <button type="button" class="mk-touch-option w-100" onclick="selectAnswer('No previous surgeries', 'TOUCH', event)">
                                        <span>No Surgeries / कोई नहीं</span>
                                    </button>
                                </div>
                            </div>

                            <div class="mb-4">
                                <label class="form-label fw-semibold small text-muted">Surgery description & year:</label>
                                <input type="text" class="form-control" id="answerInput" name="answerText" value="${medicalCase.surgicalHistory}" required>
                            </div>
                        </c:if>

                        <!-- ---------------- CURRENT MEDICATIONS ---------------- -->
                        <c:if test="${currentQuestion.questionCode == 'Q_MEDICATIONS' || (empty currentQuestion and currentStep == 9)}">
                            <input type="hidden" name="questionCode" value="Q_MEDICATIONS">
                            <input type="hidden" name="questionText" value="Current medications">

                            <div class="text-center mb-4">
                                <h3 class="fw-bold text-dark mb-1" id="questionTitleEn">Current Medications</h3>
                                <h5 class="text-muted fw-normal" id="questionTitleHi">वर्तमान दवाइयाँ</h5>
                                <p class="text-muted small">List any tablets, insulin, or syrups you take regularly.</p>
                            </div>

                            <div class="mb-4">
                                <div class="d-flex justify-content-between align-items-center mb-2">
                                    <label class="form-label fw-semibold small text-muted mb-0">Medications & Dosages:</label>
                                    <button type="button" class="btn btn-sm btn-outline-primary" onclick="selectAnswer('Tab. Metformin 500 mg BD (after meals)', 'TOUCH', event)">
                                        <i class="bi bi-capsule"></i> Metformin 500mg (Auto-fill)
                                    </button>
                                </div>
                                <div class="input-group">
                                    <input type="text" class="form-control form-control-lg" id="answerInput" name="answerText" value="${medicalCase.currentMedication}" placeholder="e.g. Metformin 500 mg BD" required>
                                    <button type="button" class="mk-mic-btn ms-2" id="micBtn">
                                        <i class="bi bi-mic-fill"></i>
                                    </button>
                                </div>
                                <div class="d-flex align-items-center gap-2 mt-2">
                                    <div class="mk-waveform" id="voiceWaveform">
                                        <span class="mk-waveform-bar"></span>
                                        <span class="mk-waveform-bar"></span>
                                        <span class="mk-waveform-bar"></span>
                                        <span class="mk-waveform-bar"></span>
                                        <span class="mk-waveform-bar"></span>
                                    </div>
                                    <div id="voiceStatus" class="small text-muted">Speak medicine names or upload prescriptions in the next step.</div>
                                </div>
                            </div>
                        </c:if>

                        <!-- ---------------- ALLERGIES & FAMILY HISTORY ---------------- -->
                        <c:if test="${currentQuestion.questionCode == 'Q_ALLERGIES' || (empty currentQuestion and currentStep == 10)}">
                            <input type="hidden" name="questionCode" value="Q_ALLERGIES">
                            <input type="hidden" name="questionText" value="Allergies and Family History">

                            <div class="text-center mb-4">
                                <h3 class="fw-bold text-dark mb-1" id="questionTitleEn">Allergies & Family History</h3>
                                <h5 class="text-muted fw-normal" id="questionTitleHi">एलर्जी और पारिवारिक इतिहास</h5>
                                <p class="text-muted small">Any known drug allergies or family history of cardiac / diabetes disease?</p>
                            </div>

                            <div class="mb-3">
                                <label class="form-label fw-semibold small text-muted">Allergies (Medicines, Food, Contrast):</label>
                                <input type="text" class="form-control" id="answerInput" name="answerText" value="${medicalCase.allergies}" required>
                            </div>

                            <div class="p-3 bg-light rounded border mb-4">
                                <div class="small fw-semibold text-dark mb-1"><i class="bi bi-check2-circle text-primary me-1"></i> Ready for Document Digitization</div>
                                <div class="small text-muted">Next step: Upload previous prescriptions or blood reports to auto-extract clinical values.</div>
                            </div>
                        </c:if>

                        <!-- Navigation Footer Buttons -->
                        <div class="d-flex justify-content-between align-items-center pt-3 border-top">
                            <c:choose>
                                <c:when test="${currentStep > 1}">
                                    <button type="submit" name="action" value="back" class="btn mk-btn mk-btn-secondary">
                                        <i class="bi bi-arrow-left"></i> Back
                                    </button>
                                </c:when>
                                <c:otherwise>
                                    <a href="/patient/dashboard" class="btn mk-btn mk-btn-secondary">
                                        <i class="bi bi-x-circle"></i> Cancel
                                    </a>
                                </c:otherwise>
                            </c:choose>

                            <button type="submit" name="action" value="next" class="btn mk-btn mk-btn-primary mk-btn-lg">
                                <span>Continue</span>
                                <i class="bi bi-arrow-right"></i>
                            </button>
                        </div>

                    </form>

                </div>

            </div>

            <!-- Conversation History Sidebar (Desktop & Tablet) -->
            <div class="col-lg-4 col-xl-4 d-none d-lg-block">
                <div class="mk-card h-100 d-flex flex-column" style="max-height: 720px;">
                    <div class="mk-card-header pb-2 mb-2">
                        <div>
                            <span class="fw-bold text-dark"><i class="bi bi-chat-left-text text-primary me-1"></i> Conversation Transcript</span>
                            <div class="small text-muted">AI-Recorded Patient Intake Log</div>
                        </div>
                        <span class="badge bg-primary rounded-pill">${not empty conversationMessages ? conversationMessages.size() : 0} msgs</span>
                    </div>

                    <div class="mk-chat-container flex-grow-1" id="chatContainer">
                        <c:choose>
                            <c:when test="${not empty conversationMessages}">
                                <c:forEach var="msg" items="${conversationMessages}">
                                    <c:choose>
                                        <c:when test="${msg.sender == 'PATIENT'}">
                                            <div class="mk-chat-bubble mk-chat-bubble-patient">
                                                <div>${msg.messageText}</div>
                                                <div class="mk-chat-bubble-meta">
                                                    <span><i class="bi ${msg.inputMethod == 'VOICE' ? 'bi-mic' : (msg.inputMethod == 'TOUCH' ? 'bi-hand-index-thumb' : 'bi-keyboard')}"></i> ${msg.inputMethod}</span>
                                                    <span>•</span>
                                                    <span>${msg.formattedTime}</span>
                                                </div>
                                            </div>
                                        </c:when>
                                        <c:otherwise>
                                            <div class="mk-chat-bubble mk-chat-bubble-system">
                                                <div class="small fw-semibold text-primary mb-1">MediKiosk AI</div>
                                                <div>${msg.messageText}</div>
                                                <div class="mk-chat-bubble-meta text-muted">
                                                    <span>${msg.questionCode}</span>
                                                </div>
                                            </div>
                                        </c:otherwise>
                                    </c:choose>
                                </c:forEach>
                            </c:when>
                            <c:otherwise>
                                <div class="text-center py-5 text-muted">
                                    <i class="bi bi-chat-square-dots fs-1 text-secondary opacity-50 mb-2 d-block"></i>
                                    <p class="small mb-0">Your responses and voice statements are recorded here in real-time for doctor reference.</p>
                                </div>
                            </c:otherwise>
                        </c:choose>
                    </div>

                    <div class="p-2 border-top bg-light rounded-bottom text-muted small d-flex justify-content-between align-items-center">
                        <span><i class="bi bi-shield-check text-success"></i> ABDM HIPAA Encrypted</span>
                        <span>ABHA Ready</span>
                    </div>
                </div>
            </div>

        </div>
    </main>

    <!-- Mobile Conversation Drawer Collapse -->
    <div class="collapse d-lg-none px-4 mb-4" id="conversationSidebarMobile">
        <div class="mk-card">
            <h6 class="fw-bold mb-3"><i class="bi bi-chat-left-text text-primary me-1"></i> Intake Transcript</h6>
            <div class="mk-chat-container" id="chatContainerMobile" style="max-height: 300px;">
                <c:forEach var="msg" items="${conversationMessages}">
                    <div class="p-2 rounded mb-2 ${msg.sender == 'PATIENT' ? 'bg-primary text-white text-end' : 'bg-light'}">
                        <div class="small fw-bold">${msg.sender} (${msg.inputMethod})</div>
                        <div>${msg.messageText}</div>
                    </div>
                </c:forEach>
            </div>
        </div>
    </div>

    <!-- Voice Recognition & TTS Module -->
    <script src="/js/medikiosk-voice.js"></script>
    <script>
        let voiceModule = null;

        function escapeHtml(str) {
            if (!str) return '';
            return str.replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;").replace(/"/g, "&quot;").replace(/'/g, "&#039;");
        }

        function appendOptimisticChat(text, method) {
            const containers = [document.getElementById('chatContainer'), document.getElementById('chatContainerMobile')];
            const timeStr = new Date().toLocaleTimeString([], {hour: '2-digit', minute:'2-digit'});
            const icon = method === 'VOICE' ? 'bi-mic' : (method === 'TOUCH' ? 'bi-hand-index-thumb' : 'bi-keyboard');

            containers.forEach(container => {
                if (!container) return;
                const emptyPlaceholder = container.querySelector('.text-center.py-5');
                if (emptyPlaceholder) emptyPlaceholder.remove();

                const patientBubble = document.createElement('div');
                patientBubble.className = 'mk-chat-bubble mk-chat-bubble-patient mk-chat-bubble-animate shadow-sm';
                patientBubble.innerHTML = '<div>' + escapeHtml(text) + '</div>' +
                    '<div class="mk-chat-bubble-meta"><span><i class="bi ' + icon + '"></i> ' + method + '</span><span>•</span><span>' + timeStr + '</span></div>';
                container.appendChild(patientBubble);

                const aiTyping = document.createElement('div');
                aiTyping.id = 'optimisticAiTyping';
                aiTyping.className = 'mk-chat-bubble mk-chat-bubble-system mk-chat-bubble-animate opacity-75 mt-2';
                aiTyping.innerHTML = '<div class="small fw-semibold text-primary mb-1">MediKiosk AI</div>' +
                    '<div class="d-flex align-items-center gap-2"><span class="spinner-grow spinner-grow-sm text-primary"></span><span>Recording clinical intake...</span></div>';
                container.appendChild(aiTyping);
                container.scrollTop = container.scrollHeight;
            });
        }

        document.addEventListener('DOMContentLoaded', () => {
            const langCode = '${lang}' === 'Hindi' ? 'hi-IN' : 'en-IN';
            voiceModule = new MediKioskVoice({
                targetInputId: 'answerInput',
                micButtonId: 'micBtn',
                statusElementId: 'voiceStatus',
                waveformId: 'voiceWaveform',
                lang: langCode
            });

            // Scroll chat transcript to bottom
            const chatEl = document.getElementById('chatContainer');
            if (chatEl) {
                chatEl.scrollTop = chatEl.scrollHeight;
            }

            // Zero-latency optimistic UI update on form submit
            const form = document.getElementById('intakeForm');
            if (form) {
                form.addEventListener('submit', () => {
                    const input = document.getElementById('answerInput');
                    const val = input ? input.value : '';
                    const method = document.getElementById('inputType')?.value || 'TEXT';
                    if (val && val.trim()) {
                        appendOptimisticChat(val.trim(), method);
                    }
                });
            }
        });

        function readCurrentQuestion() {
            const isHindi = '${lang}' === 'Hindi';
            const enTitle = document.getElementById('questionTitleEn')?.innerText;
            const hiTitle = document.getElementById('questionTitleHi')?.innerText;

            if (isHindi && hiTitle) {
                if (voiceModule) voiceModule.speak(hiTitle, 'hi-IN');
            } else if (enTitle) {
                if (voiceModule) voiceModule.speak(enTitle, 'en-IN');
            }
        }

        function selectAnswer(text, type, evt) {
            const input = document.getElementById('answerInput');
            if (input) input.value = text;
            const inputTypeEl = document.getElementById('inputType');
            if (inputTypeEl) inputTypeEl.value = type;
            
            document.querySelectorAll('.mk-touch-option').forEach(el => el.classList.remove('selected'));
            const target = (evt && evt.currentTarget) ? evt.currentTarget : (window.event ? window.event.currentTarget : null);
            if (target && target.classList) {
                target.classList.add('selected');
            }
        }

        function toggleSymptomOption(optText, btnEl) {
            const input = document.getElementById('answerInput');
            if (!input) return;
            let current = input.value ? input.value.split(',').map(s => s.trim()).filter(s => s.length > 0) : [];
            const idx = current.indexOf(optText);
            if (idx >= 0) {
                current.splice(idx, 1);
                btnEl.classList.remove('selected');
            } else {
                current.push(optText);
                btnEl.classList.add('selected');
            }
            input.value = current.join(', ');
            const inputTypeEl = document.getElementById('inputType');
            if (inputTypeEl) inputTypeEl.value = 'TOUCH';
        }

        function setDemoVoiceSample() {
            const input = document.getElementById('answerInput');
            if (input) {
                input.value = "Mujhe do ghante se chest mein pain ho raha hai aur saans lene mein takleef hai.";
            }
            const inputTypeEl = document.getElementById('inputType');
            if (inputTypeEl) inputTypeEl.value = 'VOICE';
        }
    </script>
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
