<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Doctor Workstation Login — MediKiosk</title>
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
                <span class="mk-brand-badge bg-secondary bg-opacity-10 text-secondary">Doctor Workstation</span>
            </a>
            <a href="/login" class="btn mk-btn mk-btn-secondary btn-sm">
                <i class="bi bi-person-fill"></i> Patient Kiosk
            </a>
        </div>
    </header>

    <!-- Main Container -->
    <main class="container py-5">
        <div class="row justify-content-center">
            <div class="col-md-7 col-lg-5">

                <!-- Doctor Login Card -->
                <div class="mk-card p-4 p-md-5">
                    <div class="text-center mb-4">
                        <div class="d-inline-flex p-3 rounded-circle bg-secondary bg-opacity-10 text-secondary mb-3">
                            <i class="bi bi-stethoscope fs-2"></i>
                        </div>
                        <h3 class="fw-bold text-dark mb-1">Clinician Portal</h3>
                        <p class="text-muted small">Authorized Medical Practitioner & OPD Queue Sign In</p>
                    </div>

                    <!-- Alerts -->
                    <c:if test="${not empty errorMessage}">
                        <div class="alert alert-danger d-flex align-items-center gap-2 py-2 small" role="alert">
                            <i class="bi bi-exclamation-circle-fill"></i>
                            <div>${errorMessage}</div>
                        </div>
                    </c:if>
                    <c:if test="${not empty logoutMessage}">
                        <div class="alert alert-info d-flex align-items-center gap-2 py-2 small" role="alert">
                            <i class="bi bi-info-circle-fill"></i>
                            <div>${logoutMessage}</div>
                        </div>
                    </c:if>

                    <form action="/login-process" method="POST">
                        <div class="mb-3">
                            <label for="username" class="form-label fw-semibold small text-muted">Clinician ID / Email</label>
                            <div class="input-group">
                                <span class="input-group-text bg-white border-end-0 text-muted"><i class="bi bi-person-badge"></i></span>
                                <input type="text" class="form-control border-start-0" id="username" name="username" placeholder="doctor@medikiosk.com" required autofocus>
                            </div>
                        </div>

                        <div class="mb-4">
                            <label for="password" class="form-label fw-semibold small text-muted">Password</label>
                            <div class="input-group">
                                <span class="input-group-text bg-white border-end-0 text-muted"><i class="bi bi-shield-lock"></i></span>
                                <input type="password" class="form-control border-start-0" id="password" name="password" placeholder="Enter clinician password" required>
                            </div>
                        </div>

                        <button type="submit" class="btn mk-btn mk-btn-primary w-100 mk-btn-lg mb-3">
                            <i class="bi bi-box-arrow-in-right"></i> Sign In to Workstation
                        </button>
                    </form>

                    <!-- Demo Fast-Fill Helper Box -->
                    <div class="mt-3 p-3 bg-light rounded border">
                        <div class="small fw-semibold text-muted mb-2 d-flex align-items-center justify-content-between">
                            <span><i class="bi bi-lightning-charge text-primary"></i> SIH Demo Clinician</span>
                            <span class="badge bg-secondary">Quick Fill</span>
                        </div>
                        <button type="button" class="btn btn-outline-secondary btn-sm w-100 text-start d-flex justify-content-between align-items-center" onclick="fillDoctorDemo()">
                            <div>
                                <div class="fw-semibold text-dark">Dr. Ananya Roy, MD</div>
                                <div class="small text-muted">doctor@medikiosk.com / doctor123</div>
                            </div>
                            <i class="bi bi-arrow-right-short fs-5"></i>
                        </button>
                    </div>

                </div>

            </div>
        </div>
    </main>

    <script>
        function fillDoctorDemo() {
            document.getElementById('username').value = 'doctor@medikiosk.com';
            document.getElementById('password').value = 'doctor123';
        }
    </script>
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
