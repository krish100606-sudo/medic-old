<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Upload Medical Records & OCR — MediKiosk</title>
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
                <span class="mk-brand-badge">Document Digitization</span>
            </a>
            <span class="text-muted small"><i class="bi bi-person-circle me-1"></i> ${patient.user.name}</span>
        </div>
    </header>

    <!-- Main Container -->
    <main class="container py-4">
        <div class="row justify-content-center">
            <div class="col-lg-9">

                <!-- Header Banner -->
                <div class="mb-4">
                    <div class="d-flex justify-content-between align-items-center mb-2">
                        <span class="fw-bold text-primary small text-uppercase">Document Processing & Digitization</span>
                        <span class="text-muted small">Step 2 of 3</span>
                    </div>
                    <h3 class="fw-bold text-dark mb-1">Upload Previous Medical Records</h3>
                    <p class="text-muted small">Upload your previous prescriptions, lab investigations, or discharge summaries for automatic OCR text and medical entity extraction.</p>
                </div>

                <!-- Upload Card & Fast Sample Attachments -->
                <div class="row g-4 mb-4">
                    <div class="col-md-7">
                        <div class="mk-card h-100 p-4">
                            <h6 class="fw-bold text-dark mb-3"><i class="bi bi-cloud-arrow-up text-primary me-2"></i>Upload File</h6>

                            <form action="/patient/document-upload" method="POST" enctype="multipart/form-data" id="uploadForm">
                                <div class="mb-3">
                                    <label class="form-label fw-semibold small text-muted">Document Type</label>
                                    <select class="form-select" name="documentType" id="docType">
                                        <option value="PRESCRIPTION" selected>Prescription (डॉक्टर का पर्चा)</option>
                                        <option value="BLOOD_REPORT">Blood / Pathology Report (खून की जांच रिपोर्ट)</option>
                                        <option value="INVESTIGATION">Investigation Report (X-Ray / ECG / Scan)</option>
                                        <option value="DISCHARGE_SUMMARY">Discharge Summary (अस्पताल छुट्टी सारांश)</option>
                                        <option value="OTHER">Other Health Record</option>
                                    </select>
                                </div>

                                <div class="p-4 border border-2 border-dashed rounded text-center bg-light mb-3" id="dropZone">
                                    <i class="bi bi-file-earmark-medical fs-1 text-primary mb-2 d-block"></i>
                                    <div class="fw-semibold text-dark mb-1">Drag and drop file here or click to browse</div>
                                    <div class="small text-muted mb-3">Supports JPG, PNG, PDF (Up to 10MB)</div>
                                    <input type="file" name="file" id="fileInput" class="d-none" onchange="document.getElementById('uploadForm').submit()">
                                    <button type="button" class="btn mk-btn mk-btn-primary btn-sm" onclick="document.getElementById('fileInput').click()">
                                        <i class="bi bi-folder2-open"></i> Browse Files
                                    </button>
                                </div>
                            </form>
                        </div>
                    </div>

                    <!-- Fast Demonstration Document Trigger -->
                    <div class="col-md-5">
                        <div class="mk-card h-100 p-4 bg-white">
                            <h6 class="fw-bold text-dark mb-2"><i class="bi bi-lightning-charge text-primary me-2"></i>SIH Test Documents</h6>
                            <p class="text-muted small mb-3">Click below to instantly attach and test the prototype OCR engine with sample records:</p>

                            <div class="d-grid gap-2">
                                <form action="/patient/document-upload/sample" method="POST">
                                    <input type="hidden" name="sampleType" value="prescription">
                                    <button type="submit" class="btn btn-outline-primary w-100 text-start p-2 d-flex justify-content-between align-items-center">
                                        <div>
                                            <div class="fw-semibold small"><i class="bi bi-file-earmark-text me-1"></i> previous-prescription.jpg</div>
                                            <div class="small text-muted">Prescription: Metformin 500mg, Diabetes</div>
                                        </div>
                                        <span class="badge bg-primary">Attach</span>
                                    </button>
                                </form>

                                <form action="/patient/document-upload/sample" method="POST">
                                    <input type="hidden" name="sampleType" value="blood">
                                    <button type="submit" class="btn btn-outline-secondary w-100 text-start p-2 d-flex justify-content-between align-items-center">
                                        <div>
                                            <div class="fw-semibold small text-dark"><i class="bi bi-droplet me-1"></i> blood-report.jpg</div>
                                            <div class="small text-muted">Pathology: HbA1c 7.8%, Glucose 154</div>
                                        </div>
                                        <span class="badge bg-secondary">Attach</span>
                                    </button>
                                </form>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- Processed Documents & Extracted Medical Data Table -->
                <div class="mk-card mb-4">
                    <div class="mk-card-header">
                        <h6 class="fw-bold text-dark mb-0"><i class="bi bi-check2-all text-success me-2"></i>Digitized Medical Documents (${documents != null ? documents.size() : 0})</h6>
                    </div>

                    <c:choose>
                        <c:when test="${not empty documents}">
                            <div class="table-responsive">
                                <table class="table table-hover align-middle mb-0">
                                    <thead class="table-light small text-muted text-uppercase">
                                        <tr>
                                            <th>File Name</th>
                                            <th>Document Type</th>
                                            <th>Extracted Diagnosis</th>
                                            <th>Extracted Medications</th>
                                            <th>Lab Investigations</th>
                                            <th>OCR Status</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <c:forEach var="doc" items="${documents}">
                                            <tr>
                                                <td class="fw-semibold text-dark">
                                                    <i class="bi bi-file-earmark-medical text-primary me-1"></i>
                                                    ${doc.originalFileName}
                                                </td>
                                                <td class="small"><span class="badge bg-light text-dark border">${doc.documentType}</span></td>
                                                <td class="small fw-medium text-dark">${doc.extractedDiagnosis != null ? doc.extractedDiagnosis : "N/A"}</td>
                                                <td class="small text-primary">${doc.extractedMedications != null ? doc.extractedMedications : "N/A"}</td>
                                                <td class="small text-secondary">${doc.extractedInvestigations != null ? doc.extractedInvestigations : "N/A"}</td>
                                                <td>
                                                    <span class="mk-badge mk-badge-verified">
                                                        <i class="bi bi-check-circle-fill"></i> ${doc.ocrStatus}
                                                    </span>
                                                </td>
                                            </tr>
                                        </c:forEach>
                                    </tbody>
                                </table>
                            </div>
                        </c:when>
                        <c:otherwise>
                            <div class="text-center py-4 text-muted">
                                <i class="bi bi-files fs-2 text-secondary mb-2 d-block"></i>
                                <h6>No documents uploaded yet</h6>
                                <p class="small mb-0">Upload a prescription or click one of the SIH sample buttons above.</p>
                            </div>
                        </c:otherwise>
                    </c:choose>
                </div>

                <!-- Navigation Buttons -->
                <div class="d-flex justify-content-between align-items-center">
                    <a href="/patient/case-taking?step=${not empty lastStep ? lastStep : 10}" class="btn mk-btn mk-btn-secondary">
                        <i class="bi bi-arrow-left"></i> Back to Questions
                    </a>
                    <a href="/patient/review" class="btn mk-btn mk-btn-primary mk-btn-lg">
                        <span>Review Structured Case Summary</span>
                        <i class="bi bi-arrow-right"></i>
                    </a>
                </div>

            </div>
        </div>
    </main>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
