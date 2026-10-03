<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>OTP Verification | SmartBank Connect</title>
    <jsp:include page="../common/app-head.jsp"/>
</head>
<body>
<div class="sb-shell">
    <jsp:include page="../common/sidebar.jsp"/>
    <div class="sb-main">
        <div class="sb-topbar"><h5 class="mb-0">Security Verification</h5></div>
        <div class="sb-content">
            <div class="sb-card text-center" style="max-width: 480px;">
                <i class="bi bi-shield-lock" style="font-size: 3rem; color: var(--sb-gold);"></i>
                <h5 class="mt-3 mb-2">Enter Verification Code</h5>
                <p class="text-muted mb-3">
                    <strong style="color:#1814F3;">Fund Transfer</strong><br>
                    A 6-digit verification code has been sent to your registered email address.
                </p>

                <c:if test="${not empty error}">
                    <div class="alert alert-danger py-2 text-start mb-3">
                        <i class="bi bi-exclamation-circle me-2"></i>${error}
                    </div>
                </c:if>

                <c:if test="${not empty success}">
                    <div class="alert alert-success py-2 text-start mb-3">
                        <i class="bi bi-check-circle me-2"></i>${success}
                    </div>
                </c:if>

                <form id="otpForm" method="post" action="<c:url value='/transfer/otp'/>">
                    <c:if test="${not empty _csrf}">
                        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />
                    </c:if>
                    <input type="hidden" name="transactionId" value="${transactionId}">

                    <div class="mb-4">
                        <input type="text" name="otp" id="otpInput" class="form-control text-center" 
                               maxlength="6" placeholder="000000" 
                               style="letter-spacing: 0.5em; font-size: 1.5rem; font-weight: 600; font-family: 'Courier New', monospace;"
                               inputmode="numeric" required autocomplete="off">
                        <small class="form-text text-muted mt-2">Enter only digits (0-9)</small>
                    </div>

                    <button type="submit" class="btn btn-gold w-100 py-2 mb-2">Verify &amp; Complete Transfer</button>
                </form>

                <form method="post" action="<c:url value='/transfer/otp/resend'/>">
                    <c:if test="${not empty _csrf}">
                        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />
                    </c:if>
                    <input type="hidden" name="transactionId" value="${transactionId}">
                    <button type="submit" class="btn btn-outline-secondary w-100 py-2">Resend Code</button>
                </form>

                <div class="mt-4 pt-3 border-top">
                    <div class="alert alert-info py-2 text-start small mb-2">
                        <strong><i class="bi bi-info-circle me-2"></i>OTP Information:</strong>
                        <ul class="mb-0 ps-4 mt-2">
                            <li>Code expires in <strong id="timer">${otpExpiryMinutes}</strong> minutes</li>
                            <li>You have up to 5 attempts to enter the code</li>
                            <li>Do not share the code with anyone</li>
                            <li>Never enter OTP on unsecured networks</li>
                        </ul>
                    </div>
                </div>

                <p class="text-center mt-3 small">
                    <a href="<c:url value='/transfer'/>" class="text-decoration-none">← Back to Transfer Form</a>
                </p>
            </div>
        </div>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
<script>
    // Only allow numeric input
    document.getElementById('otpInput').addEventListener('keypress', function(e) {
        if (!/[0-9]/.test(e.key)) {
            e.preventDefault();
        }
    });

    // Auto-submit when 6 digits are entered
    document.getElementById('otpInput').addEventListener('input', function() {
        if (this.value.length === 6 && /^[0-9]{6}$/.test(this.value)) {
            // Optionally auto-submit: document.getElementById('otpForm').submit();
        }
    });

    // Countdown timer
    let timeRemaining = ${otpExpiryMinutes} * 60; // Convert to seconds
    const timerElement = document.getElementById('timer');
    
    setInterval(function() {
        timeRemaining--;
        let minutes = Math.floor(timeRemaining / 60);
        let seconds = timeRemaining % 60;
        
        if (timeRemaining <= 0) {
            timerElement.textContent = "Expired";
            document.getElementById('otpInput').disabled = true;
            document.getElementById('otpForm').style.opacity = "0.5";
            document.getElementById('otpForm').style.pointerEvents = "none";
        } else if (timeRemaining <= 60) {
            timerElement.innerHTML = seconds + 's <span style="color:red;"> (expiring soon)</span>';
        } else {
            timerElement.textContent = minutes + ':' + (seconds < 10 ? '0' : '') + seconds;
        }
    }, 1000);
</script>
</body>
</html>
