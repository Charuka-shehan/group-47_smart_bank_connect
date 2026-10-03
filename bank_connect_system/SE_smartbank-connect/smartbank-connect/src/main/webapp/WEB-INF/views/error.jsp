<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <title>Error | SmartBank Connect</title>
    <jsp:include page="/WEB-INF/views/common/app-head.jsp"/>
</head>
<body class="bg-light">
<div class="container py-5" style="max-width:640px;">
    <h2>Something went wrong</h2>
    <p class="text-muted"><c:out value="${message}"/></p>
    <a class="btn btn-primary" href="<c:url value='/'/>">Home</a>
</div>
</body>
</html>
