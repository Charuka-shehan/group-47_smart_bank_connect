<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <title>Access Denied | SmartBank Connect</title>
    <jsp:include page="/WEB-INF/views/common/app-head.jsp"/>
</head>
<body class="bg-light">
<div class="container py-5 text-center">
    <i class="bi bi-shield-x text-danger" style="font-size:3rem;"></i>
    <h2 class="mt-3">HTTP 403 — Access Denied</h2>
    <p class="text-muted">You are not authorised to open this page for your role.</p>
    <a class="btn btn-primary" href="<c:url value='/bankdash/overview'/>">Back to dashboard</a>
</div>
</body>
</html>
