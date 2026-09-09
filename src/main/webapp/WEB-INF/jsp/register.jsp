<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Patient Registration — MediKiosk</title>
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
            <a class="mk-brand" href="/">
                <i class="bi bi-hospital text-primary fs-4"></i>
                <span>MediKiosk</span>
                <span class="mk-brand-badge">Patient Registration</span>
            </a>
            <a href="/login" class="btn mk-btn mk-btn-secondary btn-sm">
                <i class="bi bi-box-arrow-in-right"></i> Already Registered? Sign In
            </a>
        </div>
    </header>

    <!-- Main Container -->
    <main class="container py-5">
        <div class="row justify-content-center">
            <div class="col-lg-8">

                <!-- Registration Card -->
                <div class="mk-card p-4 p-md-5">
                    <div class="mb-4 pb-3 border-bottom">
                        <h3 class="fw-bold text-dark mb-1">New Patient Registration</h3>
                        <p class="text-muted small mb-0">Create your digital patient profile for instant hospital pre-consultation intake.</p>
                    </div>

                    <c:if test="${not empty errorMessage}">
                        <div class="alert alert-danger d-flex align-items-center gap-2 py-2 small mb-4" role="alert">
                            <i class="bi bi-exclamation-circle-fill"></i>
                            <div>${errorMessage}</div>
                        </div>
                    </c:if>

                    <form action="/register" method="POST">
                        <div class="row g-3">
                            <!-- Full Name -->
                            <div class="col-md-6">
                                <label for="name" class="form-label fw-semibold small text-muted">Full Name <span class="text-danger">*</span></label>
                                <input type="text" class="form-control" id="name" name="name" placeholder="e.g. Rahul Sharma" required>
                            </div>

                            <!-- Email -->
                            <div class="col-md-6">
                                <label for="email" class="form-label fw-semibold small text-muted">Email Address <span class="text-danger">*</span></label>
                                <input type="email" class="form-control" id="email" name="email" placeholder="e.g. rahul@gmail.com" required>
                            </div>

                            <!-- Password -->
                            <div class="col-md-6">
                                <label for="password" class="form-label fw-semibold small text-muted">Password <span class="text-danger">*</span></label>
                                <input type="password" class="form-control" id="password" name="password" placeholder="Create a secure password" required minlength="6">
                            </div>

                            <!-- Phone -->
                            <div class="col-md-6">
                                <label for="phone" class="form-label fw-semibold small text-muted">Contact Number <span class="text-danger">*</span></label>
                                <input type="tel" class="form-control" id="phone" name="phone" placeholder="e.g. 9876543210" required>
                            </div>

                            <!-- Age -->
                            <div class="col-md-4">
                                <label for="age" class="form-label fw-semibold small text-muted">Age <span class="text-danger">*</span></label>
                                <input type="number" class="form-control" id="age" name="age" min="1" max="120" value="42" required>
                            </div>

                            <!-- Gender -->
                            <div class="col-md-4">
                                <label for="gender" class="form-label fw-semibold small text-muted">Gender <span class="text-danger">*</span></label>
                                <select class="form-select" id="gender" name="gender" required>
                                    <option value="Male" selected>Male</option>
                                    <option value="Female">Female</option>
                                    <option value="Other">Other</option>
                                </select>
                            </div>

                            <!-- Preferred Language -->
                            <div class="col-md-4">
                                <label for="preferredLanguage" class="form-label fw-semibold small text-muted">Preferred Language</label>
                                <select class="form-select" id="preferredLanguage" name="preferredLanguage">
                                    <option value="English" selected>English</option>
                                    <option value="Hindi">Hindi (हिंदी)</option>
                                </select>
                            </div>

                            <!-- Hospital Department -->
                            <div class="col-md-6">
                                <label for="department" class="form-label fw-semibold small text-muted">Hospital Department <span class="text-danger">*</span></label>
                                <select class="form-select" id="department" name="department" required>
                                    <option value="General Medicine" selected>General Medicine</option>
                                    <option value="Cardiology">Cardiology</option>
                                    <option value="Pulmonology">Pulmonology</option>
                                    <option value="Orthopedics">Orthopedics</option>
                                    <option value="Pediatrics">Pediatrics</option>
                                    <option value="Neurology">Neurology</option>
                                </select>
                            </div>

                            <!-- ABHA ID (Optional) -->
                            <div class="col-md-6">
                                <label for="abhaId" class="form-label fw-semibold small text-muted">ABHA ID (Ayushman Bharat Health Account - Optional)</label>
                                <input type="text" class="form-control" id="abhaId" name="abhaId" placeholder="e.g. 14-1234-5678-9012">
                            </div>
                        </div>

                        <div class="mt-4 pt-3 border-top d-flex justify-content-between align-items-center">
                            <a href="/login" class="text-decoration-none text-muted small">
                                <i class="bi bi-arrow-left"></i> Cancel
                            </a>
                            <button type="submit" class="btn mk-btn mk-btn-primary mk-btn-lg">
                                <i class="bi bi-person-plus-fill"></i> Complete Registration
                            </button>
                        </div>
                    </form>
                </div>

            </div>
        </div>
    </main>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>