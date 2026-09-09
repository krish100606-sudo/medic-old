<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>MediKiosk — AI-Assisted Patient Intake & Clinical Summary Platform</title>
    <!-- Bootstrap 5 CSS -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <!-- Bootstrap Icons (No Emojis Policy) -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css" rel="stylesheet">
    <!-- Custom MediKiosk Design System -->
    <link href="/css/medikiosk.css" rel="stylesheet">
</head>
<body>

    <!-- Top Navigation -->
    <nav class="navbar navbar-expand-lg mk-navbar sticky-top">
        <div class="container">
            <a class="mk-brand" href="/">
                <i class="bi bi-hospital text-primary fs-4"></i>
                <span>MediKiosk</span>
                <span class="mk-brand-badge">Clinical Intake</span>
            </a>
            <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navContent">
                <span class="navbar-toggler-icon"></span>
            </button>
            <div class="collapse navbar-collapse justify-content-end" id="navContent">
                <ul class="navbar-nav align-items-center gap-3">
                    <li class="nav-item">
                        <a class="nav-link text-dark fw-medium" href="#how-it-works">How It Works</a>
                    </li>
                    <li class="nav-item">
                        <a class="nav-link text-dark fw-medium" href="#about">About MediKiosk</a>
                    </li>
                    <li class="nav-item">
                        <a class="btn mk-btn mk-btn-secondary" href="/doctor/login">
                            <i class="bi bi-person-badge"></i> Doctor Login
                        </a>
                    </li>
                    <li class="nav-item">
                        <a class="btn mk-btn mk-btn-primary" href="/login">
                            <i class="bi bi-person-fill"></i> Patient Portal / Kiosk
                        </a>
                    </li>
                </ul>
            </div>
        </div>
    </nav>

    <!-- Hero Section -->
    <section class="py-5 bg-white border-bottom">
        <div class="container py-4">
            <div class="row align-items-center g-5">
                <div class="col-lg-7">
                    <div class="d-inline-flex align-items-center gap-2 px-3 py-1 mb-3 rounded-pill bg-light border text-primary small fw-semibold">
                        <i class="bi bi-shield-check"></i>
                        <span>Smart India Hackathon 2026 Prototype — Problem Statement 26047</span>
                    </div>
                    <h1 class="display-5 fw-bold text-dark mb-3 lh-sm">
                        Prepare Your Medical History Before Your Consultation
                    </h1>
                    <p class="lead text-muted mb-4 fs-5">
                        Answer guided clinical questions in English or Hindi, digitize previous prescriptions and lab reports with OCR, and provide your doctor with a structured, verifiable case summary before you enter the OPD.
                    </p>

                    <div class="d-flex flex-wrap gap-3 mb-4">
                        <a href="/login" class="btn mk-btn mk-btn-primary mk-btn-lg">
                            <i class="bi bi-arrow-right-circle"></i> Start Patient Intake
                        </a>
                        <a href="/doctor/login" class="btn mk-btn mk-btn-secondary mk-btn-lg">
                            <i class="bi bi-stethoscope"></i> Doctor Workstation
                        </a>
                    </div>

                    <div class="d-flex flex-wrap gap-4 pt-3 border-top text-muted small">
                        <div class="d-flex align-items-center gap-2">
                            <i class="bi bi-mic text-primary"></i> Voice & Multilingual Input
                        </div>
                        <div class="d-flex align-items-center gap-2">
                            <i class="bi bi-file-earmark-medical text-primary"></i> Prescription OCR Digitization
                        </div>
                        <div class="d-flex align-items-center gap-2">
                            <i class="bi bi-exclamation-triangle text-danger"></i> Deterministic Red-Flag Triage
                        </div>
                    </div>
                </div>

                <div class="col-lg-5">
                    <div class="mk-card border p-4 shadow-sm bg-light">
                        <div class="d-flex align-items-center justify-content-between mb-3 pb-2 border-bottom">
                            <div class="d-flex align-items-center gap-2">
                                <i class="bi bi-clipboard2-pulse-fill text-primary fs-5"></i>
                                <span class="fw-bold">Clinical Case Summary Preview</span>
                            </div>
                            <span class="mk-badge mk-badge-high">High Priority</span>
                        </div>
                        
                        <div class="bg-white p-3 rounded border mb-3">
                            <div class="small text-muted mb-1">Chief Complaint</div>
                            <div class="fw-semibold text-dark">Chest pain radiating to left arm (Duration: 2 hours)</div>
                        </div>

                        <div class="bg-white p-3 rounded border mb-3">
                            <div class="small text-muted mb-1">OCR Digitized Medical Record</div>
                            <div class="d-flex justify-content-between small">
                                <span>Type 2 Diabetes (HbA1c: 7.8%)</span>
                                <span class="text-success fw-medium"><i class="bi bi-check-circle"></i> Extracted</span>
                            </div>
                        </div>

                        <div class="p-2 bg-light rounded text-center small text-muted">
                            <i class="bi bi-info-circle me-1"></i> Physician-verified clinical decision support tool.
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </section>

    <!-- How MediKiosk Works Section -->
    <section id="how-it-works" class="py-5 bg-light">
        <div class="container py-3">
            <div class="text-center max-w-700 mx-auto mb-5">
                <h2 class="fw-bold text-dark mb-2">How MediKiosk Works</h2>
                <p class="text-muted">A step-by-step pre-consultation workflow designed for hospital kiosks and mobile intake.</p>
            </div>

            <div class="row g-4">
                <div class="col-md-4 col-lg">
                    <div class="mk-card h-100 p-4 text-center">
                        <div class="mb-3 d-inline-flex p-3 rounded-circle bg-primary bg-opacity-10 text-primary">
                            <i class="bi bi-person-check fs-4"></i>
                        </div>
                        <h5 class="fw-bold mb-2">1. Identify</h5>
                        <p class="text-muted small mb-0">Sign in with Patient ID or Phone number, confirm consent, and select your preferred language.</p>
                    </div>
                </div>

                <div class="col-md-4 col-lg">
                    <div class="mk-card h-100 p-4 text-center">
                        <div class="mb-3 d-inline-flex p-3 rounded-circle bg-primary bg-opacity-10 text-primary">
                            <i class="bi bi-chat-left-dots fs-4"></i>
                        </div>
                        <h5 class="fw-bold mb-2">2. Answer</h5>
                        <p class="text-muted small mb-0">Answer structured clinical questions via voice, touch, or text input with ease.</p>
                    </div>
                </div>

                <div class="col-md-4 col-lg">
                    <div class="mk-card h-100 p-4 text-center">
                        <div class="mb-3 d-inline-flex p-3 rounded-circle bg-primary bg-opacity-10 text-primary">
                            <i class="bi bi-cloud-arrow-up fs-4"></i>
                        </div>
                        <h5 class="fw-bold mb-2">3. Upload</h5>
                        <p class="text-muted small mb-0">Upload previous prescriptions and lab reports for automatic OCR medical entity extraction.</p>
                    </div>
                </div>

                <div class="col-md-4 col-lg">
                    <div class="mk-card h-100 p-4 text-center">
                        <div class="mb-3 d-inline-flex p-3 rounded-circle bg-primary bg-opacity-10 text-primary">
                            <i class="bi bi-diagram-3 fs-4"></i>
                        </div>
                        <h5 class="fw-bold mb-2">4. Structure</h5>
                        <p class="text-muted small mb-0">The system creates a clinical timeline and detects predefined safety red flags.</p>
                    </div>
                </div>

                <div class="col-md-4 col-lg">
                    <div class="mk-card h-100 p-4 text-center">
                        <div class="mb-3 d-inline-flex p-3 rounded-circle bg-primary bg-opacity-10 text-primary">
                            <i class="bi bi-clipboard2-check fs-4"></i>
                        </div>
                        <h5 class="fw-bold mb-2">5. Review</h5>
                        <p class="text-muted small mb-0">Patient reviews and submits for instant doctor verification in the OPD queue.</p>
                    </div>
                </div>
            </div>
        </div>
    </section>

    <!-- Clinical Value Proposition Banner -->
    <section id="about" class="py-5 bg-white border-top border-bottom">
        <div class="container text-center py-3">
            <h4 class="fw-bold text-dark mb-3">
                "Collect the patient's story before the consultation, structure the information automatically, and let the doctor focus on the clinical decision."
            </h4>
            <p class="text-muted mb-4 max-w-700 mx-auto small">
                MediKiosk strictly functions as a pre-consultation intake and document digitizing platform. The consulting physician retains 100% responsibility for clinical diagnosis and final treatment decisions.
            </p>
            <div class="d-flex justify-content-center gap-3">
                <a href="/login" class="btn mk-btn mk-btn-primary">
                    <i class="bi bi-box-arrow-in-right"></i> Open Patient Kiosk
                </a>
                <a href="/doctor/login" class="btn mk-btn mk-btn-secondary">
                    <i class="bi bi-shield-lock"></i> Doctor Portal
                </a>
            </div>
        </div>
    </section>

    <!-- Footer -->
    <footer class="py-4 bg-light border-top text-center text-muted small">
        <div class="container">
            <p class="mb-1 fw-medium">MediKiosk — Pre-Consultation Clinical Intake & Document Digitization Platform</p>
            <p class="mb-0 text-secondary">Smart India Hackathon Problem Statement 26047 Prototype | Built with Spring Boot, Hibernate & PostgreSQL</p>
        </div>
    </footer>

    <!-- Bootstrap 5 JS -->
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
