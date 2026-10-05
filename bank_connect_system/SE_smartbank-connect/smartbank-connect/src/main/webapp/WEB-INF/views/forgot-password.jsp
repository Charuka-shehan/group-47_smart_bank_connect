<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Forgot Password | SmartBank Connect</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="<c:url value='/css/style.css'/>" rel="stylesheet">
</head>
<body>
<div class="sb-auth-wrap">
    <div class="sb-auth-card">
        <h3>Reset password</h3>
        <p class="text-muted small">Enter your registered email. A reset link will be sent (sandbox: check the application console).</p>
        <c:if test="${not empty error}"><div class="alert alert-danger py-2">${error}</div></c:if>
        <c:if test="${not empty success}"><div class="alert alert-success py-2">${success}</div></c:if>
        <form method="post">
            <c:if test="${not empty _csrf}"><input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" /></c:if>
            <input type="email" name="email" class="form-control mb-3" required placeholder="you@example.com">
            <button class="btn btn-gold w-100">Send reset link</button>
        </form>
        <p class="text-center mt-3 mb-0"><a href="<c:url value='/login'/>">Back to login</a></p>
    </div>
</div>
</body>
</html>
