<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Clinician Workstation — Patient Queue — MediKiosk</title>
    <!-- Bootstrap 5 CSS -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <!-- Bootstrap Icons (No Emojis) -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css" rel="stylesheet">
    <!-- MediKiosk Design System -->
    <link href="/css/medikiosk.css" rel="stylesheet">
</head>
<body>

    <!-- Top Header -->
    <header class="mk-navbar sticky-top">
        <div class="container-fluid px-4 d-flex justify-content-between align-items-center">
            <a class="mk-brand" href="/doctor/dashboard">
                <i class="bi bi-hospital text-primary fs-4"></i>
                <span>MediKiosk</span>
                <span class="mk-brand-badge bg-secondary bg-opacity-10 text-secondary">Doctor Workstation</span>
            </a>
            <div class="d-flex align-items-center gap-3">
                <div class="text-end d-none d-sm-block">
                    <div class="fw-bold text-dark small">${doctor != null && doctor.user != null ? doctor.user.name : "Dr. Ananya Roy, MD"}</div>
                    <div class="text-muted small">${doctor != null ? doctor.department : "General Medicine"} | ${doctor != null ? doctor.doctorId : "DOC-101"}</div>
                </div>
                <a href="/logout" class="btn mk-btn mk-btn-secondary btn-sm">
                    <i class="bi bi-box-arrow-right"></i> Logout
                </a>
            </div>
        </div>
    </header>

    <!-- Main Doctor Layout (Sidebar + Content) -->
    <div class="mk-doctor-layout">
        <!-- Sidebar -->
        <aside class="mk-doctor-sidebar">
            <div class="mb-4 px-2">
                <div class="small fw-semibold text-muted text-uppercase mb-2">Navigation</div>
                <a href="/doctor/dashboard" class="mk-sidebar-link active">
                    <i class="bi bi-speedometer2"></i> Dashboard & Queue
                </a>
                <a href="/doctor/dashboard?status=SUBMITTED" class="mk-sidebar-link">
                    <i class="bi bi-clock-history"></i> Pending Intake (${statPendingReview})
                </a>
                <a href="/doctor/dashboard?priority=HIGH" class="mk-sidebar-link text-danger">
                    <i class="bi bi-exclamation-triangle"></i> High Priority (${statHighPriority})
                </a>
                <a href="/doctor/dashboard?status=VERIFIED" class="mk-sidebar-link">
                    <i class="bi bi-patch-check"></i> Verified Cases (${statVerified})
                </a>
            </div>

            <div class="p-3 bg-light rounded border small text-muted">
                <div class="fw-bold text-dark mb-1"><i class="bi bi-shield-check text-primary me-1"></i> Pre-Consultation AI</div>
                <div style="font-size: 0.78rem;">Cases are structured via kiosk intake and OCR. Final clinical decisions remain with the clinician.</div>
            </div>
        </aside>

        <!-- Main Content Area -->
        <main class="mk-doctor-content">
            <div class="d-flex flex-wrap justify-content-between align-items-center mb-4">
                <div>
                    <h3 class="fw-bold text-dark mb-1">OPD Patient Intake Queue</h3>
                    <p class="text-muted small mb-0">Live stream of patient case summaries submitted from MediKiosk terminals.</p>
                </div>
                <div>
                    <span class="badge bg-white border text-dark p-2 px-3 shadow-sm">
                        <i class="bi bi-calendar3 text-primary me-1"></i> Today: ${todayDate}
                    </span>
                </div>
            </div>

            <!-- Stats Metric Cards -->
            <div class="row g-3 mb-4">
                <div class="col-sm-6 col-xl-3">
                    <div class="mk-card p-3">
                        <div class="d-flex justify-content-between align-items-center">
                            <div>
                                <div class="text-muted small fw-semibold text-uppercase">Total in Queue</div>
                                <div class="fs-3 fw-bold text-dark">${statTotalQueue}</div>
                            </div>
                            <div class="p-3 bg-primary bg-opacity-10 text-primary rounded-circle">
                                <i class="bi bi-people fs-4"></i>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="col-sm-6 col-xl-3">
                    <div class="mk-card p-3 border-danger border-opacity-25">
                        <div class="d-flex justify-content-between align-items-center">
                            <div>
                                <div class="text-danger small fw-semibold text-uppercase">High Priority</div>
                                <div class="fs-3 fw-bold text-danger">${statHighPriority}</div>
                            </div>
                            <div class="p-3 bg-danger bg-opacity-10 text-danger rounded-circle">
                                <i class="bi bi-exclamation-octagon fs-4"></i>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="col-sm-6 col-xl-3">
                    <div class="mk-card p-3">
                        <div class="d-flex justify-content-between align-items-center">
                            <div>
                                <div class="text-warning small fw-semibold text-uppercase">Pending Review</div>
                                <div class="fs-3 fw-bold text-dark">${statPendingReview}</div>
                            </div>
                            <div class="p-3 bg-warning bg-opacity-10 text-warning rounded-circle">
                                <i class="bi bi-hourglass-split fs-4"></i>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="col-sm-6 col-xl-3">
                    <div class="mk-card p-3">
                        <div class="d-flex justify-content-between align-items-center">
                            <div>
                                <div class="text-success small fw-semibold text-uppercase">Verified Today</div>
                                <div class="fs-3 fw-bold text-success">${statVerified}</div>
                            </div>
                            <div class="p-3 bg-success bg-opacity-10 text-success rounded-circle">
                                <i class="bi bi-shield-check fs-4"></i>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <!-- Search and Filter Bar -->
            <div class="mk-card mb-4 p-3">
                <form action="/doctor/dashboard" method="GET" class="row g-2 align-items-center">
                    <div class="col-md-5">
                        <div class="input-group">
                            <span class="input-group-text bg-white border-end-0 text-muted"><i class="bi bi-search"></i></span>
                            <input type="text" class="form-control border-start-0" name="search" value="${search}" placeholder="Search patient name, token, case ID, symptom...">
                        </div>
                    </div>

                    <div class="col-md-3">
                        <select class="form-select" name="priority" onchange="this.form.submit()">
                            <option value="ALL" ${selectedPriority == 'ALL' ? 'selected' : ''}>All Priorities</option>
                            <option value="HIGH" ${selectedPriority == 'HIGH' ? 'selected' : ''}>High Priority Only</option>
                            <option value="NORMAL" ${selectedPriority == 'NORMAL' ? 'selected' : ''}>Normal Priority Only</option>
                        </select>
                    </div>

                    <div class="col-md-3">
                        <select class="form-select" name="status" onchange="this.form.submit()">
                            <option value="ALL" ${selectedStatus == 'ALL' ? 'selected' : ''}>All Statuses</option>
                            <option value="SUBMITTED" ${selectedStatus == 'SUBMITTED' ? 'selected' : ''}>Pending Review</option>
                            <option value="UNDER_REVIEW" ${selectedStatus == 'UNDER_REVIEW' ? 'selected' : ''}>Under Review</option>
                            <option value="VERIFIED" ${selectedStatus == 'VERIFIED' ? 'selected' : ''}>Verified Cases</option>
                        </select>
                    </div>

                    <div class="col-md-1 d-grid">
                        <button type="submit" class="btn mk-btn mk-btn-primary">Filter</button>
                    </div>
                </form>
            </div>

            <!-- Patient Queue Table -->
            <div class="mk-card">
                <div class="mk-card-header">
                    <h6 class="fw-bold text-dark mb-0"><i class="bi bi-list-check text-primary me-2"></i>Patient Queue (${cases != null ? cases.size() : 0} Patients)</h6>
                </div>

                <div class="table-responsive">
                    <table class="table table-hover align-middle mb-0">
                        <thead class="table-light small text-muted text-uppercase">
                            <tr>
                                <th>Token</th>
                                <th>Patient Name</th>
                                <th>Age / Gender</th>
                                <th>Department</th>
                                <th>Chief Complaint</th>
                                <th>Priority</th>
                                <th>Status</th>
                                <th class="text-end">Action</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:choose>
                                <c:when test="${not empty cases}">
                                    <c:forEach var="c" items="${cases}">
                                        <tr class="${c.priority == 'HIGH' || c.priority == 'CRITICAL' ? 'table-danger bg-opacity-25' : ''}">
                                            <td>
                                                <span class="fw-bold text-primary fs-6">#${c.tokenNumber != null ? c.tokenNumber : "N/A"}</span>
                                            </td>
                                            <td>
                                                <div class="fw-bold text-dark">${c.patient.user.name}</div>
                                                <div class="small text-muted">${c.patient.patientId}</div>
                                            </td>
                                            <td class="small">
                                                ${c.patient.age != null ? c.patient.age : "42"} yrs / ${c.patient.gender}
                                            </td>
                                            <td class="small fw-medium">${c.patient.department}</td>
                                            <td class="small">
                                                <div class="fw-semibold text-dark">${not empty c.chiefComplaint ? c.chiefComplaint : "General"}</div>
                                                <c:if test="${c.redFlagsDetected}">
                                                    <div class="small text-danger"><i class="bi bi-exclamation-triangle-fill"></i> ${c.priorityReason}</div>
                                                </c:if>
                                            </td>
                                            <td>
                                                <c:choose>
                                                    <c:when test="${c.priority == 'HIGH' || c.priority == 'CRITICAL'}">
                                                        <span class="mk-badge mk-badge-high">HIGH</span>
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
                                                    <c:when test="${c.status == 'UNDER_REVIEW'}">
                                                        <span class="mk-badge mk-badge-pending">UNDER REVIEW</span>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <span class="mk-badge mk-badge-pending">PENDING</span>
                                                    </c:otherwise>
                                                </c:choose>
                                            </td>
                                            <td class="text-end">
                                                <a href="/doctor/case/${c.id}" class="btn mk-btn ${c.status == 'VERIFIED' ? 'mk-btn-secondary' : 'mk-btn-primary'} btn-sm">
                                                    <i class="bi bi-folder2-open"></i> View Case
                                                </a>
                                            </td>
                                        </tr>
                                    </c:forEach>
                                </c:when>
                                <c:otherwise>
                                    <tr>
                                        <td colspan="8" class="text-center py-4 text-muted">No patients matching filter criteria.</td>
                                    </tr>
                                </c:otherwise>
                            </c:choose>
                        </tbody>
                    </table>
                </div>
            </div>

        </main>
    </div>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
