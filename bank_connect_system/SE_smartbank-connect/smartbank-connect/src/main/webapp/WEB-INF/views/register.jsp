<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Register | SmartBank Connect</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css" rel="stylesheet">
    <link href="<c:url value='/css/style.css'/>" rel="stylesheet">
</head>
<body>
<div class="sb-auth-wrap">
    <div class="sb-auth-card" style="max-width: 500px;">
        <div class="text-center mb-4">
            <i class="bi bi-person-plus" style="font-size:2.2rem; color: var(--sb-navy);"></i>
            <h3 class="mt-2">Activate Online Banking</h3>
            <p class="text-muted small">Register only after a bank officer has opened and a manager has approved your account.</p>
        </div>

        <c:if test="${not empty error}">
            <div class="alert alert-danger py-2 text-danger"><i class="bi bi-exclamation-triangle-fill me-2"></i><c:out value="${error}"/></div>
        </c:if>
        <c:if test="${not empty success}">
            <div class="alert alert-success py-2 text-success"><i class="bi bi-check-circle-fill me-2"></i><c:out value="${success}"/></div>
        </c:if>

        <form id="regForm" method="post" action="<c:url value='/register'/>" novalidate>
            <c:if test="${not empty _csrf}"><input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" /></c:if>
            <div class="mb-3">
                <label class="form-label" for="customerId">Customer ID <span class="text-danger">*</span></label>
                <input type="text" class="form-control" name="customerId" id="customerId" placeholder="e.g. CUST100001" required>
                <div class="invalid-feedback text-danger" id="custIdError">Customer ID is required.</div>
            </div>
            <div class="mb-3">
                <label class="form-label" for="accountNumber">Account Number <span class="text-danger">*</span></label>
                <input type="text" class="form-control" name="accountNumber" id="accountNumber" placeholder="e.g. LTB123456789" required>
                <div class="invalid-feedback text-danger" id="accNumError">Account number is required.</div>
            </div>
            <div class="row">
                <div class="col-6 mb-3">
                    <label class="form-label" for="email">Email Address <span class="text-danger">*</span></label>
                    <input type="email" class="form-control" name="email" id="email" placeholder="name@example.com" required>
                    <div class="invalid-feedback text-danger" id="emailError">Valid email required.</div>
                </div>
                <div class="col-6 mb-3">
                    <label class="form-label" for="phone">Mobile Number <span class="text-danger">*</span></label>
                    <input type="text" class="form-control" name="phone" id="phone" placeholder="077 123 4567" required>
                    <div class="invalid-feedback text-danger" id="phoneError">Valid phone number required.</div>
                </div>
            </div>
            <div class="mb-3">
                <label class="form-label" for="username">Username <span class="text-danger">*</span></label>
                <input type="text" class="form-control" name="username" id="username" placeholder="Choose a username" minlength="4" required>
                <div class="invalid-feedback text-danger" id="userError">Username must be at least 4 characters.</div>
            </div>
            <div class="mb-3">
                <label class="form-label" for="password">Password <span class="text-danger">*</span></label>
                <input type="password" class="form-control" name="password" id="password" minlength="8" required>
                <div class="invalid-feedback text-danger" id="pwError">Must meet all password rules below.</div>
                <div class="mt-2 small" id="pwRules">
                    <div id="ruleLength" class="text-muted"><i class="bi bi-circle me-1"></i> At least 8 characters</div>
                    <div id="ruleUpper" class="text-muted"><i class="bi bi-circle me-1"></i> At least one uppercase letter (A-Z)</div>
                    <div id="ruleLower" class="text-muted"><i class="bi bi-circle me-1"></i> At least one lowercase letter (a-z)</div>
                    <div id="ruleDigit" class="text-muted"><i class="bi bi-circle me-1"></i> At least one digit (0-9)</div>
                    <div id="ruleSpecial" class="text-muted"><i class="bi bi-circle me-1"></i> At least one special character (@$!%*?&#)</div>
                </div>
            </div>
            <div class="mb-3">
                <label class="form-label" for="confirmPassword">Confirm Password <span class="text-danger">*</span></label>
                <input type="password" class="form-control" name="confirmPassword" id="confirmPassword" minlength="8" required>
                <div class="invalid-feedback text-danger" id="matchError">Passwords must match.</div>
                <div id="matchSuccess" class="text-success small mt-1" style="display:none;"><i class="bi bi-check-circle me-1"></i> Passwords match!</div>
            </div>
            <button type="submit" class="btn btn-gold w-100 py-2">Activate Online Banking</button>
        </form>

        <p class="text-center mt-3 mb-0">
            <a href="<c:url value='/'/>" class="text-decoration-none small">&larr; Back to Home</a> &nbsp;|&nbsp;
            <a href="<c:url value='/login'/>" class="text-decoration-none small">Already have an account?</a>
        </p>
    </div>
</div>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
<script>
const pw = document.getElementById('password');
const cpw = document.getElementById('confirmPassword');
const ruleLength = document.getElementById('ruleLength');
const ruleUpper = document.getElementById('ruleUpper');
const ruleLower = document.getElementById('ruleLower');
const ruleDigit = document.getElementById('ruleDigit');
const ruleSpecial = document.getElementById('ruleSpecial');
const matchError = document.getElementById('matchError');
const matchSuccess = document.getElementById('matchSuccess');

function updateRule(el, valid) {
    if (valid) {
        el.className = 'text-success';
        el.innerHTML = '<i class="bi bi-check-circle-fill me-1"></i>' + el.textContent.trim();
    } else {
        el.className = 'text-danger';
        el.innerHTML = '<i class="bi bi-x-circle-fill me-1"></i>' + el.textContent.trim();
    }
}

pw.addEventListener('input', function() {
    const val = pw.value;
    updateRule(ruleLength, val.length >= 8);
    updateRule(ruleUpper, /[A-Z]/.test(val));
    updateRule(ruleLower, /[a-z]/.test(val));
    updateRule(ruleDigit, /[0-9]/.test(val));
    updateRule(ruleSpecial, /[@$!%*?&#]/.test(val));
    checkMatch();
});

function checkMatch() {
    if (!cpw.value) {
        matchSuccess.style.display = 'none';
        cpw.classList.remove('is-invalid');
        return;
    }
    if (pw.value === cpw.value) {
        matchSuccess.style.display = 'block';
        cpw.classList.remove('is-invalid');
    } else {
        matchSuccess.style.display = 'none';
        cpw.classList.add('is-invalid');
    }
}

cpw.addEventListener('input', checkMatch);

document.getElementById('regForm').addEventListener('submit', function(e) {
    let valid = true;
    const val = pw.value;
    const isPwValid = val.length >= 8 && /[A-Z]/.test(val) && /[a-z]/.test(val) && /[0-9]/.test(val) && /[@$!%*?&#]/.test(val);

    if (!isPwValid) {
        pw.classList.add('is-invalid');
        valid = false;
    } else {
        pw.classList.remove('is-invalid');
    }

    if (val !== cpw.value || !cpw.value) {
        cpw.classList.add('is-invalid');
        valid = false;
    } else {
        cpw.classList.remove('is-invalid');
    }

    if (!valid) {
        e.preventDefault();
        return false;
    }
});
</script>
</body>
</html>
