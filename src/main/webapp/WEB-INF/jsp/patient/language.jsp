<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Select Language — MediKiosk</title>
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
                <span class="mk-brand-badge">Language Selection</span>
            </a>
            <span class="text-muted small"><i class="bi bi-person-circle me-1"></i> ${patient.user.name}</span>
        </div>
    </header>

    <!-- Main Container -->
    <main class="container py-5">
        <div class="row justify-content-center">
            <div class="col-md-8 col-lg-6">

                <div class="mk-card p-4 p-md-5 text-center">
                    <div class="mb-4">
                        <div class="d-inline-flex p-3 rounded-circle bg-primary bg-opacity-10 text-primary mb-3">
                            <i class="bi bi-translate fs-2"></i>
                        </div>
                        <h3 class="fw-bold text-dark mb-1">Select Preferred Language</h3>
                        <h5 class="text-muted fw-normal">अपनी पसंदीदा भाषा चुनें</h5>
                        <p class="text-muted small mt-2">Questions and voice input will be adapted to your chosen language.</p>
                    </div>

                    <form action="/patient/language" method="POST" id="langForm">
                        <input type="hidden" name="language" id="selectedLanguage" value="English">

                        <div class="row g-3 mb-4">
                            <!-- English Card -->
                            <div class="col-6">
                                <div class="mk-touch-option selected p-4 text-center d-flex flex-column align-items-center justify-content-center h-100" id="cardEnglish" onclick="selectLang('English')">
                                    <i class="bi bi-globe2 fs-1 text-primary mb-2"></i>
                                    <div class="fs-4 fw-bold">English</div>
                                    <div class="small text-muted">Standard English Intake</div>
                                </div>
                            </div>

                            <!-- Hindi Card -->
                            <div class="col-6">
                                <div class="mk-touch-option p-4 text-center d-flex flex-column align-items-center justify-content-center h-100" id="cardHindi" onclick="selectLang('Hindi')">
                                    <i class="bi bi-chat-heart fs-1 text-primary mb-2"></i>
                                    <div class="fs-4 fw-bold">हिंदी</div>
                                    <div class="small text-muted">Hindi Voice & Intake</div>
                                </div>
                            </div>
                        </div>

                        <button type="submit" class="btn mk-btn mk-btn-primary w-100 mk-btn-lg">
                            <span>Continue to Intake</span> <i class="bi bi-arrow-right"></i>
                        </button>
                    </form>
                </div>

            </div>
        </div>
    </main>

    <script>
        function selectLang(lang) {
            document.getElementById('selectedLanguage').value = lang;
            document.getElementById('cardEnglish').classList.toggle('selected', lang === 'English');
            document.getElementById('cardHindi').classList.toggle('selected', lang === 'Hindi');
        }
    </script>
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
