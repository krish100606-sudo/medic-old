<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Case Submitted — Token #${medicalCase.tokenNumber} — MediKiosk</title>
    <!-- Bootstrap 5 CSS -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <!-- Bootstrap Icons (No Emojis) -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css" rel="stylesheet">
    <!-- MediKiosk Design System -->
    <link href="/css/medikiosk.css" rel="stylesheet">
</head>
<body class="bg-light">

    <!-- Top Navigation -->
    <header class="mk-navbar">
        <div class="container d-flex justify-content-between align-items-center">
            <a class="mk-brand" href="/patient/dashboard">
                <i class="bi bi-hospital text-primary fs-4"></i>
                <span>MediKiosk</span>
                <span class="mk-brand-badge">Intake Complete</span>
            </a>
            <span class="text-muted small"><i class="bi bi-person-circle me-1"></i> ${patient.user.name}</span>
        </div>
    </header>

    <!-- Main Container -->
    <main class="container py-5">
        <div class="row justify-content-center">
            <div class="col-md-8 col-lg-6">

                <!-- Success Confirmation Card -->
                <div class="mk-card p-4 p-md-5 text-center">
                    <div class="mb-4">
                        <div class="d-inline-flex p-3 rounded-circle bg-success bg-opacity-10 text-success mb-3">
                            <i class="bi bi-check2-circle fs-1"></i>
                        </div>
                        <h2 class="fw-bold text-dark mb-1">Case Submitted Successfully</h2>
                        <p class="text-muted small">Your medical history has been structured and transmitted to the OPD Clinician Workstation.</p>
                    </div>

                    <!-- Token Display Box -->
                    <div class="p-4 bg-light rounded border mb-4">
                        <div class="small text-muted text-uppercase fw-semibold mb-1">Your Consultation Token</div>
                        <div class="display-3 fw-bold text-primary mb-2">#${medicalCase.tokenNumber}</div>
                        <div class="d-flex justify-content-center gap-2 mb-2">
                            <span class="badge bg-secondary">Case ID: ${medicalCase.caseNumber}</span>
                            <c:choose>
                                <c:when test="${medicalCase.priority == 'HIGH' || medicalCase.priority == 'CRITICAL'}">
                                    <span class="mk-badge mk-badge-high"><i class="bi bi-exclamation-triangle-fill"></i> ${medicalCase.priority} Priority</span>
                                </c:when>
                                <c:otherwise>
                                    <span class="mk-badge mk-badge-normal">Standard Priority</span>
                                </c:otherwise>
                            </c:choose>
                        </div>
                        <div class="small text-muted">Department: <strong class="text-dark">${patient.department}</strong></div>
                    </div>

                    <!-- Guidance Steps -->
                    <div class="text-start bg-white p-3 rounded border mb-4 small">
                        <div class="fw-bold text-dark mb-2"><i class="bi bi-geo-alt-fill text-primary me-1"></i> Next Steps for Consultation:</div>
                        <ol class="mb-0 ps-3 text-muted">
                            <li class="mb-1">Please proceed to <strong>OPD Waiting Area 2 (Room 108)</strong>.</li>
                            <li class="mb-1">Your token number <strong>#${medicalCase.tokenNumber}</strong> will be announced on the queue display board.</li>
                            <li>The doctor already has your digitized case summary ready on their screen.</li>
                        </ol>
                    </div>

                    <!-- Actions -->
                    <div class="d-flex flex-wrap gap-2 justify-content-center">
                        <button type="button" class="btn mk-btn mk-btn-secondary" onclick="window.print()">
                            <i class="bi bi-printer"></i> Print Token Slip
                        </button>
                        <a href="/patient/dashboard" class="btn mk-btn mk-btn-primary">
                            <i class="bi bi-house"></i> Return to Dashboard
                        </a>
                    </div>
                </div>

            </div>
        </div>
    </main>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
