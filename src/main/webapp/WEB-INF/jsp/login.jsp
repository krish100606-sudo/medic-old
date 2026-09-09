<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Patient Intake Login — MediKiosk</title>
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
                <span class="mk-brand-badge">Patient Kiosk</span>
            </a>
            <div class="d-flex align-items-center gap-2">
                <a href="/doctor/login" class="btn mk-btn mk-btn-secondary btn-sm">
                    <i class="bi bi-person-badge"></i> Doctor Login
                </a>
            </div>
        </div>
    </header>

    <!-- Main Container -->
    <main class="container py-5">
        <div class="row justify-content-center">
            <div class="col-md-7 col-lg-5">

                <!-- Login Card -->
                <div class="mk-card p-4 p-md-5">
                    <div class="text-center mb-4">
                        <div class="d-inline-flex p-3 rounded-circle bg-primary bg-opacity-10 text-primary mb-3">
                            <i class="bi bi-person-vcard fs-2"></i>
                        </div>
                        <h3 class="fw-bold text-dark mb-1">Patient Sign In</h3>
                        <p class="text-muted small">Enter your Patient ID, Registered Phone, or Email to start intake.</p>
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
                    <c:if test="${not empty successMessage}">
                        <div class="alert alert-success d-flex align-items-center gap-2 py-2 small" role="alert">
                            <i class="bi bi-check-circle-fill"></i>
                            <div>${successMessage}</div>
                        </div>
                    </c:if>

                    <form action="/login-process" method="POST">
                        <div class="mb-3">
                            <label for="username" class="form-label fw-semibold small text-muted">Patient ID / Phone / Email</label>
                            <div class="input-group">
                                <span class="input-group-text bg-white border-end-0 text-muted"><i class="bi bi-person"></i></span>
                                <input type="text" class="form-control border-start-0" id="username" name="username" placeholder="e.g. rahul@gmail.com or 9876543210" required autofocus>
                            </div>
                        </div>

                        <div class="mb-4">
                            <div class="d-flex justify-content-between">
                                <label for="password" class="form-label fw-semibold small text-muted">Password</label>
                            </div>
                            <div class="input-group">
                                <span class="input-group-text bg-white border-end-0 text-muted"><i class="bi bi-lock"></i></span>
                                <input type="password" class="form-control border-start-0" id="password" name="password" placeholder="Enter your password" required>
                            </div>
                        </div>

                        <button type="submit" class="btn mk-btn mk-btn-primary w-100 mk-btn-lg mb-3">
                            <i class="bi bi-box-arrow-in-right"></i> Sign In to Kiosk
                        </button>
                    </form>

                    <div class="text-center pt-3 border-top">
                        <span class="text-muted small">New to the hospital?</span>
                        <a href="/register" class="fw-semibold text-primary ms-1 small text-decoration-none">Create Account</a>
                    </div>

                    <!-- Demo Fast-Fill Helper Box -->
                    <div class="mt-4 p-3 bg-light rounded border">
                        <div class="small fw-semibold text-muted mb-2 d-flex align-items-center justify-content-between">
                            <span><i class="bi bi-lightning-charge text-primary"></i> SIH Demo Account</span>
                            <span class="badge bg-secondary">Quick Fill</span>
                        </div>
                        <button type="button" class="btn btn-outline-secondary btn-sm w-100 text-start d-flex justify-content-between align-items-center" onclick="fillPatientDemo()">
                            <div>
                                <div class="fw-semibold text-dark">Rahul Sharma (Age 42)</div>
                                <div class="small text-muted">rahul@gmail.com / patient123</div>
                            </div>
                            <i class="bi bi-arrow-right-short fs-5"></i>
                        </button>
                    </div>

                </div>

            </div>
        </div>
    </main>

    <script>
        function fillPatientDemo() {
            document.getElementById('username').value = 'rahul@gmail.com';
            document.getElementById('password').value = 'patient123';
        }
    </script>
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
