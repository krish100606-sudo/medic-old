<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Patient Dashboard — MediKiosk</title>
    <!-- Bootstrap 5 CSS -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <!-- Bootstrap Icons (No Emojis) -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css" rel="stylesheet">
    <!-- MediKiosk Design System -->
    <link href="/css/medikiosk.css" rel="stylesheet">
</head>
<body class="bg-light">

    <!-- Top Header -->
    <header class="mk-navbar sticky-top">
        <div class="container d-flex justify-content-between align-items-center">
            <a class="mk-brand" href="/patient/dashboard">
                <i class="bi bi-hospital text-primary fs-4"></i>
                <span>MediKiosk</span>
                <span class="mk-brand-badge">Patient Kiosk</span>
            </a>
            <div class="d-flex align-items-center gap-3">
                <div class="d-none d-md-block text-end">
                    <div class="fw-semibold small">${patient.user.name}</div>
                    <div class="text-muted small">${patient.patientId}</div>
                </div>
                <a href="/logout" class="btn mk-btn mk-btn-secondary btn-sm">
                    <i class="bi bi-box-arrow-right"></i> Logout
                </a>
            </div>
        </div>
    </header>

    <!-- Main Container -->
    <main class="container py-4">

        <!-- Welcome Banner -->
        <div class="mk-card mb-4 bg-white">
            <div class="row align-items-center g-3">
                <div class="col-md-8">
                    <div class="d-inline-flex align-items-center gap-2 px-2 py-1 mb-2 rounded bg-light text-primary small fw-semibold">
                        <i class="bi bi-check-circle"></i> Digital Patient Portal
                    </div>
                    <h2 class="fw-bold text-dark mb-1">Welcome, ${patient.user.name}</h2>
                    <p class="text-muted mb-0 small">Prepare your case intake summary before meeting your physician in the OPD.</p>
                </div>
                <div class="col-md-4 text-md-end">
                    <a href="/patient/consent" class="btn mk-btn mk-btn-primary mk-btn-lg">
                        <i class="bi bi-plus-circle"></i> Start New Case Intake
                    </a>
                </div>
            </div>
        </div>

        <div class="row g-4 mb-4">
            <!-- Patient Demographics Info Card -->
            <div class="col-lg-4">
                <div class="mk-card h-100">
                    <div class="mk-card-header">
                        <h6 class="fw-bold text-dark mb-0"><i class="bi bi-person-lines-fill text-primary me-2"></i>Patient Information</h6>
                    </div>
                    <div class="mb-3">
                        <div class="small text-muted">Patient ID</div>
                        <div class="fw-semibold">${patient.patientId}</div>
                    </div>
                    <div class="row g-2 mb-3">
                        <div class="col-6">
                            <div class="small text-muted">Age / Gender</div>
                            <div class="fw-semibold">${patient.age != null ? patient.age : "42"} yrs / ${patient.gender}</div>
                        </div>
                        <div class="col-6">
                            <div class="small text-muted">Blood Group</div>
                            <div class="fw-semibold">${patient.bloodGroup != null ? patient.bloodGroup : "B+"}</div>
                        </div>
                    </div>
                    <div class="mb-3">
                        <div class="small text-muted">Department</div>
                        <div class="fw-semibold text-primary">${patient.department}</div>
                    </div>
                    <div class="mb-3">
                        <div class="small text-muted">Preferred Language</div>
                        <div class="fw-semibold">${patient.preferredLanguage}</div>
                    </div>
                    <c:if test="${not empty patient.abhaId}">
                        <div class="p-2 bg-light rounded border">
                            <div class="small text-muted">ABHA ID</div>
                            <div class="fw-semibold text-dark small">${patient.abhaId}</div>
                        </div>
                    </c:if>
                </div>
            </div>

            <!-- Current Case / Active Intake Status Card -->
            <div class="col-lg-8">
                <div class="mk-card h-100">
                    <div class="mk-card-header">
                        <h6 class="fw-bold text-dark mb-0"><i class="bi bi-clipboard2-pulse text-primary me-2"></i>Current Active Case</h6>
                        <c:choose>
                            <c:when test="${currentCase.priority == 'HIGH' || currentCase.priority == 'CRITICAL'}">
                                <span class="mk-badge mk-badge-high">High Priority</span>
                            </c:when>
                            <c:otherwise>
                                <span class="mk-badge mk-badge-normal">Normal</span>
                            </c:otherwise>
                        </c:choose>
                    </div>

                    <c:choose>
                        <c:when test="${not empty currentCase}">
                            <div class="row g-3 mb-3">
                                <div class="col-sm-4">
                                    <div class="small text-muted">Case ID</div>
                                    <div class="fw-bold text-dark">${currentCase.caseNumber}</div>
                                </div>
                                <div class="col-sm-4">
                                    <div class="small text-muted">Token Number</div>
                                    <div class="fw-bold text-primary fs-5">Token #${currentCase.tokenNumber != null ? currentCase.tokenNumber : "Pending"}</div>
                                </div>
                                <div class="col-sm-4">
                                    <div class="small text-muted">Status</div>
                                    <div>
                                        <c:choose>
                                            <c:when test="${currentCase.status == 'SUBMITTED'}">
                                                <span class="mk-badge mk-badge-pending"><i class="bi bi-clock-history"></i> Submitted to OPD Queue</span>
                                            </c:when>
                                            <c:when test="${currentCase.status == 'UNDER_REVIEW'}">
                                                <span class="mk-badge mk-badge-pending"><i class="bi bi-eye"></i> Under Doctor Review</span>
                                            </c:when>
                                            <c:when test="${currentCase.status == 'VERIFIED'}">
                                                <span class="mk-badge mk-badge-verified"><i class="bi bi-shield-check"></i> Doctor Verified</span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="mk-badge mk-badge-normal">Draft In Progress</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </div>
                                </div>
                            </div>

                            <div class="p-3 bg-light rounded border mb-3">
                                <div class="small text-muted mb-1">Chief Complaint</div>
                                <div class="fw-semibold text-dark">${not empty currentCase.chiefComplaint ? currentCase.chiefComplaint : "Chest Pain"}</div>
                                <c:if test="${not empty currentCase.patientStatement}">
                                    <div class="small text-muted mt-1 fst-italic">"${currentCase.patientStatement}"</div>
                                </c:if>
                            </div>

                            <c:if test="${currentCase.redFlagsDetected}">
                                <div class="mk-red-flag-box py-2 px-3 mb-3">
                                    <div class="mk-red-flag-title small">
                                        <i class="bi bi-exclamation-triangle-fill"></i> Predefined Safety Alert: ${currentCase.priorityReason}
                                    </div>
                                </div>
                            </c:if>

                            <div class="d-flex flex-wrap gap-2">
                                <c:choose>
                                    <c:when test="${currentCase.status == 'DRAFT'}">
                                        <a href="/patient/case-taking?step=1" class="btn mk-btn mk-btn-primary">
                                            <i class="bi bi-play-circle"></i> Resume Guided Case Taking
                                        </a>
                                        <a href="/patient/document-upload" class="btn mk-btn mk-btn-secondary">
                                            <i class="bi bi-upload"></i> Upload Documents
                                        </a>
                                    </c:when>
                                    <c:otherwise>
                                        <a href="/patient/case-complete?id=${currentCase.id}" class="btn mk-btn mk-btn-primary">
                                            <i class="bi bi-ticket-detailed"></i> View Case Slip & Token
                                        </a>
                                    </c:otherwise>
                                </c:choose>
                            </div>
                        </c:when>
                        <c:otherwise>
                            <div class="text-center py-4 text-muted">
                                <i class="bi bi-folder2-open fs-1 text-secondary mb-2 d-block"></i>
                                <h6>No Active Intake Case</h6>
                                <p class="small mb-3">Click below to start your pre-consultation medical case history.</p>
                                <a href="/patient/consent" class="btn mk-btn mk-btn-primary">
                                    <i class="bi bi-plus-lg"></i> Begin Case Taking
                                </a>
                            </div>
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>
        </div>

        <!-- Previous Cases Table -->
        <div class="mk-card">
            <div class="mk-card-header">
                <h6 class="fw-bold text-dark mb-0"><i class="bi bi-clock-history text-primary me-2"></i>Previous Clinical Cases</h6>
            </div>
            <div class="table-responsive">
                <table class="table table-hover align-middle mb-0">
                    <thead class="table-light small text-muted text-uppercase">
                        <tr>
                            <th>Date / Time</th>
                            <th>Case ID</th>
                            <th>Chief Complaint</th>
                            <th>Priority</th>
                            <th>Status</th>
                            <th>Doctor Verification</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:choose>
                            <c:when test="${not empty cases}">
                                <c:forEach var="c" items="${cases}">
                                    <tr>
                                        <td class="small">${c.formattedCreatedAt}</td>
                                        <td class="fw-semibold text-dark">${c.caseNumber}</td>
                                        <td class="small">${not empty c.chiefComplaint ? c.chiefComplaint : "General Consultation"}</td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${c.priority == 'HIGH' || c.priority == 'CRITICAL'}">
                                                    <span class="mk-badge mk-badge-high">${c.priority}</span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="mk-badge mk-badge-normal">NORMAL</span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${c.status == 'VERIFIED'}">
                                                    <span class="mk-badge mk-badge-verified">VERIFIED</span>
                                                </c:when>
                                                <c:when test="${c.status == 'SUBMITTED'}">
                                                    <span class="mk-badge mk-badge-pending">SUBMITTED</span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="mk-badge mk-badge-normal">${c.status}</span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td class="small text-muted">
                                            <c:choose>
                                                <c:when test="${not empty c.verifiedByDoctor}">
                                                    <span class="text-primary fw-medium"><i class="bi bi-shield-check"></i> ${c.verifiedByDoctor}</span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span>Pending Review</span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </c:when>
                            <c:otherwise>
                                <tr>
                                    <td colspan="6" class="text-center text-muted py-3 small">No previous records found.</td>
                                </tr>
                            </c:otherwise>
                        </c:choose>
                    </tbody>
                </table>
            </div>
        </div>

    </main>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
