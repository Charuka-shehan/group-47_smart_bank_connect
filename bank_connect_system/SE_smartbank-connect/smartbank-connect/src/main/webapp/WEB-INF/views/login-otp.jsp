<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Two-Factor Authentication | SmartBank Connect</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css" rel="stylesheet">
    <link href="<c:url value='/css/style.css'/>" rel="stylesheet">
    <style>
        .otp-input-field {
            letter-spacing: 0.5rem;
            font-size: 1.8rem;
            text-align: center;
            font-weight: 700;
        }
    </style>
</head>
<body>
<div class="sb-auth-wrap">
    <div class="sb-auth-card">
        <div class="text-center mb-4">
            <i class="bi bi-shield-lock-fill" style="font-size:3rem; color: var(--sb-navy);"></i>
            <h3 class="mt-2">Security Verification</h3>
            <p class="text-muted small">Enter the 6-digit OTP code sent to your registered Gmail address.</p>
        </div>

        <c:if test="${not empty error}">
            <div class="alert alert-danger py-2 text-center small">${error}</div>
        </c:if>
        <c:if test="${not empty success}">
            <div class="alert alert-success py-2 text-center small">${success}</div>
        </c:if>

        <form method="post" action="<c:url value='/login/otp/verify'/>">
            <c:if test="${not empty _csrf}">
                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />
            </c:if>
            <div class="mb-3">
                <label class="form-label text-center d-block fw-semibold text-muted">6-Digit Code</label>
                <input type="text" id="otp-input" class="form-control otp-input-field" name="otp" maxlength="6" pattern="\d{6}" placeholder="000000" required autofocus autocomplete="off">
            </div>

            <div class="text-center mb-4 small" id="timer-text">
                OTP expires in <span id="otp-timer" class="fw-bold text-primary">05:00</span>
            </div>

            <button type="submit" id="verify-btn" class="btn btn-gold w-100 py-2">Verify & Proceed</button>
        </form>

        <div class="text-center mt-4">
            <p class="small text-muted mb-2">Didn't receive the code?</p>
            <a href="<c:url value='/login/otp/resend'/>" class="btn btn-sm btn-outline-secondary px-3">
                <i class="bi bi-arrow-clockwise me-1"></i> Resend OTP
            </a>
        </div>

        <p class="text-center mt-4 mb-0">
            <a href="<c:url value='/logout'/>" class="text-decoration-none small text-danger"><i class="bi bi-box-arrow-left"></i> Cancel and Log Out</a>
        </p>
    </div>
</div>

<script>
    document.addEventListener("DOMContentLoaded", function() {
        let timeLeft = 300; // 5 minutes in seconds
        const timerElement = document.getElementById("otp-timer");
        const verifyBtn = document.getElementById("verify-btn");
        const otpInput = document.getElementById("otp-input");
        const timerText = document.getElementById("timer-text");

        const countdown = setInterval(function() {
            if (timeLeft <= 0) {
                clearInterval(countdown);
                timerText.innerHTML = "<span class='text-danger fw-bold'>OTP expired</span>";
                verifyBtn.disabled = true;
                otpInput.disabled = true;
            } else {
                let minutes = Math.floor(timeLeft / 60);
                let seconds = timeLeft % 60;
                if (seconds < 10) seconds = "0" + seconds;
                if (minutes < 10) minutes = "0" + minutes;
                timerElement.textContent = minutes + ":" + seconds;
                timeLeft--;
            }
        }, 1000);
    });
</script>
</body>
</html>
