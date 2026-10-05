<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Set New Password | SmartBank Connect</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="<c:url value='/css/style.css'/>" rel="stylesheet">
</head>
<body>
<div class="sb-auth-wrap">
    <div class="sb-auth-card">
        <h3>Choose a new password</h3>
        <c:if test="${not empty error}"><div class="alert alert-danger py-2">${error}</div></c:if>
        <form method="post">
            <c:if test="${not empty _csrf}"><input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" /></c:if>
            <input type="hidden" name="token" value="${token}">
            <input type="password" name="newPassword" class="form-control mb-2" placeholder="New password" required>
            <input type="password" name="confirmPassword" class="form-control mb-3" placeholder="Confirm password" required>
            <button class="btn btn-gold w-100">Update password</button>
        </form>
    </div>
</div>
</body>
</html>
