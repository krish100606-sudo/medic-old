<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Review Case Intake — MediKiosk</title>
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
        <div class="container d-flex justify-content-between align-items-center">
            <a class="mk-brand" href="/patient/dashboard">
                <i class="bi bi-hospital text-primary fs-4"></i>
                <span>MediKiosk</span>
                <span class="mk-brand-badge">Pre-Consultation Review</span>
            </a>
            <span class="text-muted small"><i class="bi bi-person-circle me-1"></i> ${patient.user.name}</span>
        </div>
    </header>

    <!-- Main Container -->
    <main class="container py-4">
        <div class="row justify-content-center">
            <div class="col-lg-9">

                <!-- Header Banner -->
                <div class="d-flex flex-wrap justify-content-between align-items-center mb-4">
                    <div>
                        <span class="fw-bold text-primary small text-uppercase">Final Review & Verification</span>
                        <h3 class="fw-bold text-dark mb-1">Review Your Clinical Case</h3>
                        <p class="text-muted small mb-0">Check the structured clinical information extracted before submitting it to the doctor's queue.</p>
                    </div>
                    <div>
                        <c:choose>
                            <c:when test="${medicalCase.priority == 'HIGH' || medicalCase.priority == 'CRITICAL'}">
                                <span class="mk-badge mk-badge-high fs-6 p-2 px-3">
                                    <i class="bi bi-exclamation-triangle-fill"></i> ${medicalCase.priority} Priority
                                </span>
                            </c:when>
                            <c:otherwise>
                                <span class="mk-badge mk-badge-normal fs-6 p-2 px-3">
                                    <i class="bi bi-check-circle"></i> Standard Priority
                                </span>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>

                <!-- Red-Flag Safety Alert Banner if Detected -->
                <c:if test="${medicalCase.redFlagsDetected}">
                    <div class="mk-red-flag-box">
                        <div class="mk-red-flag-title mb-1">
                            <i class="bi bi-exclamation-octagon-fill fs-5"></i> High Priority Triage Alert Detected
                        </div>
                        <div class="text-dark small">
                            <strong>Reason:</strong> ${medicalCase.priorityReason}
                        </div>
                        <div class="text-muted small mt-1">
                            <em>Note: This is a rule-based triage safety indicator to alert the triage nurse and doctor. It is not an autonomous medical diagnosis.</em>
                        </div>
                    </div>
                </c:if>

                <!-- Patient Demographics Summary -->
                <div class="mk-card mb-3">
                    <div class="row g-3">
                        <div class="col-sm-3">
                            <div class="small text-muted">Patient Name</div>
                            <div class="fw-bold text-dark">${patient.user.name}</div>
                        </div>
                        <div class="col-sm-3">
                            <div class="small text-muted">Patient ID / Age</div>
                            <div class="fw-semibold">${patient.patientId} (${patient.age} yrs, ${patient.gender})</div>
                        </div>
                        <div class="col-sm-3">
                            <div class="small text-muted">Department</div>
                            <div class="fw-semibold text-primary">${patient.department}</div>
                        </div>
                        <div class="col-sm-3">
                            <div class="small text-muted">Preferred Language</div>
                            <div class="fw-semibold">${patient.preferredLanguage}</div>
                        </div>
                    </div>
                </div>

                <!-- Clinical Intake Summary Breakdown -->
                <div class="mk-card mb-4">
                    <div class="mk-card-header">
                        <h6 class="fw-bold text-dark mb-0"><i class="bi bi-clipboard-pulse text-primary me-2"></i>Structured Clinical History</h6>
                        <a href="/patient/case-taking?step=1" class="btn btn-outline-primary btn-sm">
                            <i class="bi bi-pencil"></i> Edit Answers
                        </a>
                    </div>

                    <div class="row g-3">
                        <div class="col-md-6">
                            <div class="mk-summary-block h-100">
                                <div class="mk-summary-label"><i class="bi bi-chat-square-text"></i> Chief Complaint</div>
                                <div class="mk-summary-value text-primary fw-bold">${not empty medicalCase.chiefComplaint ? medicalCase.chiefComplaint : "Not specified"}</div>
                            </div>
                        </div>
                        <div class="col-md-6">
                            <div class="mk-summary-block h-100">
                                <div class="mk-summary-label"><i class="bi bi-mic"></i> Patient Statement (Verbatim)</div>
                                <div class="mk-summary-value fst-italic">"${not empty medicalCase.patientStatement ? medicalCase.patientStatement : 'No statement provided'}"</div>
                            </div>
                        </div>

                        <div class="col-md-4">
                            <div class="mk-summary-block h-100">
                                <div class="mk-summary-label"><i class="bi bi-clock"></i> Onset & Duration</div>
                                <div class="mk-summary-value">${not empty medicalCase.onset ? medicalCase.onset : "Not specified"}</div>
                            </div>
                        </div>
                        <div class="col-md-4">
                            <div class="mk-summary-block h-100">
                                <div class="mk-summary-label"><i class="bi bi-geo-alt"></i> Location</div>
                                <div class="mk-summary-value">${not empty medicalCase.location ? medicalCase.location : "Not specified"}</div>
                            </div>
                        </div>
                        <div class="col-md-4">
                            <div class="mk-summary-block h-100">
                                <div class="mk-summary-label"><i class="bi bi-speedometer2"></i> Severity</div>
                                <div class="mk-summary-value text-danger fw-bold">${not empty medicalCase.severity ? medicalCase.severity : "Not specified"}</div>
                            </div>
                        </div>

                        <div class="col-md-6">
                            <div class="mk-summary-block h-100">
                                <div class="mk-summary-label"><i class="bi bi-heart-pulse"></i> Associated Symptoms</div>
                                <div class="mk-summary-value">${not empty medicalCase.associatedSymptoms ? medicalCase.associatedSymptoms : "None reported"}</div>
                            </div>
                        </div>
                        <div class="col-md-6">
                            <div class="mk-summary-block h-100">
                                <div class="mk-summary-label"><i class="bi bi-file-medical"></i> Past Medical History</div>
                                <div class="mk-summary-value">${not empty medicalCase.pastMedicalHistory ? medicalCase.pastMedicalHistory : "None reported"}</div>
                            </div>
                        </div>

                        <div class="col-md-6">
                            <div class="mk-summary-block h-100">
                                <div class="mk-summary-label"><i class="bi bi-capsule"></i> Ongoing Medications</div>
                                <div class="mk-summary-value text-primary">${not empty medicalCase.currentMedication ? medicalCase.currentMedication : "None reported"}</div>
                            </div>
                        </div>
                        <div class="col-md-6">
                            <div class="mk-summary-block h-100">
                                <div class="mk-summary-label"><i class="bi bi-bandaid"></i> Surgical History & Allergies</div>
                                <div class="mk-summary-value">${not empty medicalCase.surgicalHistory ? medicalCase.surgicalHistory : "None reported"} | Allergies: ${not empty medicalCase.allergies ? medicalCase.allergies : "None reported"}</div>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- Medical Timeline Card -->
                <div class="mk-card mb-4">
                    <div class="mk-card-header">
                        <h6 class="fw-bold text-dark mb-0"><i class="bi bi-calendar3-range text-primary me-2"></i>Chronological Medical Timeline</h6>
                        <span class="badge bg-light text-muted border">Dynamic History</span>
                    </div>
                    <div class="mk-timeline">
                        <c:choose>
                            <c:when test="${not empty medicalCase.timelineList}">
                                <c:forEach var="item" items="${medicalCase.timelineList}">
                                    <div class="mk-timeline-item">
                                        <div class="mk-timeline-date"><c:out value="${item[0]}"/></div>
                                        <div class="mk-timeline-text ${item[0] == 'Today' ? 'fw-bold text-primary' : ''}">
                                            <c:out value="${item[1]}"/>
                                        </div>
                                    </div>
                                </c:forEach>
                            </c:when>
                            <c:otherwise>
                                <div class="mk-timeline-item">
                                    <div class="mk-timeline-date">Today</div>
                                    <div class="mk-timeline-text fw-bold text-primary">Pre-Consultation Intake: Presented with ${not empty medicalCase.chiefComplaint ? medicalCase.chiefComplaint : 'Symptoms'}</div>
                                </div>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>

                <!-- Submission Form -->
                <form action="/patient/submit-case" method="POST">
                    <div class="d-flex justify-content-between align-items-center pt-2">
                        <a href="/patient/document-upload" class="btn mk-btn mk-btn-secondary">
                            <i class="bi bi-arrow-left"></i> Back to Documents
                        </a>
                        <button type="submit" class="btn mk-btn mk-btn-primary mk-btn-lg">
                            <i class="bi bi-send-check"></i> Submit Case to OPD Queue
                        </button>
                    </div>
                </form>

            </div>
        </div>
    </main>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
