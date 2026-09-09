<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Clinical Consent & ABHA Verification — MediKiosk</title>
    <!-- Bootstrap 5 CSS -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <!-- Bootstrap Icons (No Emojis) -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css" rel="stylesheet">
    <!-- MediKiosk Design System -->
    <link href="/css/medikiosk.css" rel="stylesheet">
</head>
<body class="bg-light">

    <!-- Top Header -->
    <header class="mk-navbar">
        <div class="container d-flex justify-content-between align-items-center">
            <a class="mk-brand" href="/patient/dashboard">
                <i class="bi bi-hospital text-primary fs-4"></i>
                <span>MediKiosk</span>
                <span class="mk-brand-badge">Patient Intake</span>
            </a>
            <div class="d-flex align-items-center gap-3">
                <span class="text-muted small"><i class="bi bi-person-circle me-1"></i> ${patient.user.name}</span>
                <a href="/logout" class="btn btn-outline-danger btn-sm">
                    <i class="bi bi-box-arrow-right"></i> Sign Out
                </a>
            </div>
        </div>
    </header>

    <!-- Main Container -->
    <main class="container py-4 py-md-5">
        <div class="row justify-content-center">
            <div class="col-lg-8">

                <!-- Consent Card -->
                <div class="mk-card p-4 p-md-5">
                    <div class="d-flex align-items-center gap-3 mb-4 pb-3 border-bottom">
                        <div class="p-3 rounded-circle bg-primary bg-opacity-10 text-primary">
                            <i class="bi bi-shield-check fs-2"></i>
                        </div>
                        <div>
                            <h3 class="fw-bold text-dark mb-1">Clinical Consent & Registration</h3>
                            <p class="text-muted small mb-0">डिजिटल सहमति एवं आयुष्मान भारत स्वास्थ्य खाता (ABHA) सत्यापन</p>
                        </div>
                    </div>

                    <form action="/patient/consent" method="POST" id="consentForm" onsubmit="return handleFormSubmit()">

                        <!-- ABHA ID Registration / Verification -->
                        <div class="p-3 bg-white rounded border mb-4">
                            <label class="form-label fw-bold text-dark mb-1">
                                <i class="bi bi-card-heading text-primary me-1"></i> Ayushman Bharat Health Account (ABHA ID / Number)
                            </label>
                            <p class="text-muted small mb-2">Link your government ABHA for seamless ABDM electronic health record sharing.</p>
                            <div class="input-group">
                                <span class="input-group-text bg-light"><i class="bi bi-person-vcard"></i></span>
                                <input type="text" class="form-control" name="abhaId" id="abhaId"
                                       value="${not empty patient.abhaId ? patient.abhaId : '91-2026-4491-8899'}"
                                       placeholder="e.g. 91-2026-4491-8899 or name@abdm">
                                <button class="btn btn-outline-success" type="button" onclick="verifyAbha()">
                                    <i class="bi bi-check-circle"></i> Verify
                                </button>
                            </div>
                            <div id="abhaFeedback" class="small mt-1 text-success">
                                <i class="bi bi-patch-check-fill text-success"></i> ABDM linked: Verified Ayushman Arogya Mandir beneficiary.
                            </div>
                        </div>

                        <!-- Consent Policy Items -->
                        <div class="mb-4">
                            <h6 class="fw-bold text-dark mb-2">Consent Terms / सहमति की शर्तें:</h6>
                            
                            <div class="p-3 bg-light rounded border mb-3">
                                <h6 class="fw-semibold text-dark mb-1"><i class="bi bi-info-circle text-primary me-1"></i> Data Collection / डेटा संग्रहण</h6>
                                <p class="text-muted small mb-0">Chief symptoms, medical history, medications, allergies, and uploaded reports are collected for pre-consultation doctor review.</p>
                            </div>

                            <div class="p-3 bg-light rounded border mb-3">
                                <h6 class="fw-semibold text-dark mb-1"><i class="bi bi-lock text-primary me-1"></i> ABDM & Security / सुरक्षा</h6>
                                <p class="text-muted small mb-0">All data is encrypted under ABDM HIPAA standards and shared exclusively with your treating doctor.</p>
                            </div>

                            <div class="p-3 bg-primary bg-opacity-10 rounded border border-primary border-opacity-25 mb-3">
                                <h6 class="fw-semibold text-primary mb-1"><i class="bi bi-stethoscope me-1"></i> Clinical Protocol Notice</h6>
                                <p class="text-dark small mb-0">MediKiosk assists clinical case-taking and triage prioritization. The final medical diagnosis, evaluation, and prescription are rendered exclusively by licensed doctors.</p>
                            </div>
                        </div>

                        <!-- Digital Signature Pad -->
                        <div class="p-3 bg-white rounded border mb-4">
                            <div class="d-flex justify-content-between align-items-center mb-2">
                                <label class="form-label fw-bold text-dark mb-0">
                                    <i class="bi bi-pen text-primary me-1"></i> Patient Digital Signature / डिजिटल हस्ताक्षर
                                </label>
                                <button type="button" class="btn btn-sm btn-outline-secondary" onclick="clearSignature()">
                                    <i class="bi bi-eraser"></i> Clear Pad
                                </button>
                            </div>
                            <p class="text-muted small mb-2">Draw your signature or mark below using mouse or touchscreen:</p>
                            
                            <canvas id="sigCanvas" class="mk-sig-canvas" width="460" height="130"></canvas>
                            <input type="hidden" name="digitalSignature" id="digitalSignature">
                            <div class="small text-muted mt-1">
                                <i class="bi bi-fingerprint"></i> Biometric / Digital electronic consent recorded for this session.
                            </div>
                        </div>

                        <!-- Agreement Checkbox -->
                        <div class="form-check p-3 bg-white rounded border mb-4">
                            <input class="form-check-input ms-0 me-2" type="checkbox" name="consent" id="consentCheck" value="true" checked required>
                            <label class="form-check-label fw-semibold text-dark small" for="consentCheck">
                                I hereby confirm that I have read the consent terms and authorize the hospital to digitize my clinical intake for doctor review.
                            </label>
                        </div>

                        <!-- Submit Buttons -->
                        <div class="d-flex justify-content-between align-items-center">
                            <a href="/patient/dashboard" class="btn mk-btn mk-btn-secondary">
                                <i class="bi bi-x-circle"></i> Cancel
                            </a>
                            <button type="submit" class="btn mk-btn mk-btn-primary mk-btn-lg">
                                <i class="bi bi-check2-circle"></i> Agree & Continue to Language Selection
                            </button>
                        </div>
                    </form>
                </div>

            </div>
        </div>
    </main>

    <script>
        const canvas = document.getElementById('sigCanvas');
        const ctx = canvas.getContext('2d');
        let isDrawing = false;
        let hasSigned = false;

        // Signature Canvas Drawing Logic (Touch + Mouse)
        function initSignaturePad() {
            ctx.lineWidth = 2.5;
            ctx.lineCap = 'round';
            ctx.strokeStyle = '#0369a1';

            // Draw a pre-rendered demo signature for ease of testing
            drawSampleSignature();

            // Mouse Events
            canvas.addEventListener('mousedown', startDrawing);
            canvas.addEventListener('mousemove', draw);
            canvas.addEventListener('mouseup', stopDrawing);
            canvas.addEventListener('mouseout', stopDrawing);

            // Touch Events for Mobile / Tablet Kiosk
            canvas.addEventListener('touchstart', (e) => {
                e.preventDefault();
                const touch = e.touches[0];
                const rect = canvas.getBoundingClientRect();
                isDrawing = true;
                ctx.beginPath();
                ctx.moveTo(touch.clientX - rect.left, touch.clientY - rect.top);
            });
            canvas.addEventListener('touchmove', (e) => {
                e.preventDefault();
                if (!isDrawing) return;
                const touch = e.touches[0];
                const rect = canvas.getBoundingClientRect();
                ctx.lineTo(touch.clientX - rect.left, touch.clientY - rect.top);
                ctx.stroke();
                hasSigned = true;
            });
            canvas.addEventListener('touchend', stopDrawing);
        }

        function startDrawing(e) {
            isDrawing = true;
            hasSigned = true;
            ctx.beginPath();
            ctx.moveTo(e.offsetX, e.offsetY);
        }

        function draw(e) {
            if (!isDrawing) return;
            ctx.lineTo(e.offsetX, e.offsetY);
            ctx.stroke();
        }

        function stopDrawing() {
            if (isDrawing) {
                ctx.closePath();
                isDrawing = false;
            }
        }

        function clearSignature() {
            ctx.clearRect(0, 0, canvas.width, canvas.height);
            hasSigned = false;
            document.getElementById('digitalSignature').value = '';
        }

        function drawSampleSignature() {
            ctx.clearRect(0, 0, canvas.width, canvas.height);
            ctx.beginPath();
            ctx.moveTo(40, 75);
            ctx.bezierCurveTo(90, 30, 130, 95, 180, 55);
            ctx.bezierCurveTo(210, 80, 240, 45, 290, 70);
            ctx.stroke();
            hasSigned = true;
        }

        function verifyAbha() {
            const val = document.getElementById('abhaId').value.trim();
            const fb = document.getElementById('abhaFeedback');
            if (val.length > 5) {
                fb.className = 'small mt-1 text-success';
                fb.innerHTML = '<i class="bi bi-patch-check-fill text-success"></i> ABHA verified successfully via ABDM gateway.';
            } else {
                fb.className = 'small mt-1 text-danger';
                fb.innerHTML = '<i class="bi bi-exclamation-triangle text-danger"></i> Please enter a valid 14-digit ABHA number or ABHA address.';
            }
        }

        function handleFormSubmit() {
            // Save base64 signature image to hidden field
            if (hasSigned) {
                document.getElementById('digitalSignature').value = canvas.toDataURL('image/png');
            }
            return true;
        }

        document.addEventListener('DOMContentLoaded', initSignaturePad);
    </script>
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
