<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Log In | SmartBank Connect</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css" rel="stylesheet">
    <link href="<c:url value='/css/style.css'/>" rel="stylesheet">
</head>
<body>
<div class="sb-auth-wrap">
    <div class="sb-auth-card">
        <div class="text-center mb-4">
            <i class="bi bi-bank2" style="font-size:2.2rem; color: var(--sb-navy);"></i>
            <h3 class="mt-2">Welcome back</h3>
            <p class="text-muted small">Log in to SmartBank Connect</p>
        </div>

        <c:if test="${param.error == 'oauth'}">
            <div class="alert alert-danger py-2">No bank record exists for that Google account. Complete officer-assisted account opening first.</div>
        </c:if>
        <c:if test="${param.logout != null}">
            <div class="alert alert-success py-2">You have been logged out successfully.</div>
        </c:if>
        <c:if test="${param.expired != null}">
            <div class="alert alert-warning py-2">Your session expired. Please log in again.</div>
        </c:if>
        <c:if test="${not empty success}">
            <div class="alert alert-success py-2"><i class="bi bi-check-circle me-2"></i>${success}</div>
        </c:if>
        <c:if test="${param.error == 'true'}">
            <div class="alert alert-danger py-2"><i class="bi bi-exclamation-circle me-2"></i><strong>Login failed:</strong> Invalid username/email or password. Please try again.</div>
        </c:if>
        <c:if test="${param.locked == 'true'}">
            <div class="alert alert-danger py-2"><i class="bi bi-lock me-2"></i><strong>Account locked:</strong> Your account has been locked due to too many failed login attempts. Please try again after 30 minutes or <a href="<c:url value='/forgot-password'/>" class="alert-link">reset your password</a>.</div>
        </c:if>
        <c:if test="${param.disabled == 'true'}">
            <div class="alert alert-danger py-2"><i class="bi bi-exclamation-circle me-2"></i><strong>Account disabled:</strong> Your account has been disabled. Please contact support.</div>
        </c:if>

        <form method="post" action="<c:url value='/login'/>">
            <!-- Include CSRF token so Spring Security accepts the POST request -->
            <c:if test="${not empty _csrf}">
                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />
            </c:if>
            <div class="mb-3">
                <label class="form-label">Username or Email</label>
                <input type="text" class="form-control" name="username" placeholder="manager or you@example.com" required>
            </div>
            <div class="mb-3">
                <label class="form-label">Password</label>
                <input type="password" class="form-control" name="password" placeholder="••••••••" required>
            </div>
            <button type="submit" class="btn btn-gold w-100 py-2">Log In</button>
        </form>

        <div class="text-center my-3 text-muted small">or</div>

        <a href="<c:url value='/oauth2/authorization/google'/>" class="btn btn-outline-secondary w-100 py-2 mb-3">
            <i class="bi bi-google me-1"></i> Continue with Google
        </a>

        <p class="text-center small text-muted mb-1">
            Demo accounts:
        </p>
        <p class="text-center small text-muted">
            manager@lankatrust.lk / Manager@123<br>
            officer@lankatrust.lk / Officer@123 &middot; customer@lankatrust.lk / Customer@123<br>
            admin@lankatrust.lk / Admin@123 &middot; compliance@lankatrust.lk / Compliance@123<br>
            cre@lankatrust.lk / Cre@123
        </p>

        <p class="text-center mt-3 mb-0">
            <a href="<c:url value='/'/>" class="text-decoration-none small">&larr; Back to Home</a> &nbsp;|&nbsp;
            <a href="<c:url value='/forgot-password'/>" class="text-decoration-none small">Forgot password</a> &nbsp;|&nbsp;
            <a href="<c:url value='/register'/>" class="text-decoration-none small">Create an account</a>
        </p>
    </div>
</div>
</body>
</html>
