<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <title>Transfer Successful | SmartBank Connect</title>
    <jsp:include page="../common/app-head.jsp"/>
</head>
<body>
<div class="sb-shell">
    <jsp:include page="../common/sidebar.jsp"/>
    <div class="sb-main">
        <div class="sb-topbar"><h5 class="mb-0">Transfer Confirmation</h5></div>
        <div class="sb-content">
            <div class="sb-card text-center" style="max-width: 460px;">
                <i class="bi bi-check-circle-fill" style="font-size: 3rem; color: var(--sb-success);"></i>
                <h5 class="mt-3">Transfer Successful</h5>
                <p class="text-muted">Rs. <fmt:formatNumber value="${transaction.amount}" maxFractionDigits="2"/>
                    sent to ${transaction.beneficiaryName}.</p>
                <table class="table table-sm text-start mt-3">
                    <tr><th>Reference No.</th><td>${transaction.referenceNumber}</td></tr>
                    <tr><th>Status</th><td><span class="sb-badge sb-badge-completed">${transaction.status}</span></td></tr>
                    <tr><th>Date</th><td>${transaction.processedAt}</td></tr>
                </table>
                <a href="<c:url value='/bankdash/overview'/>" class="btn btn-gold w-100 py-2 mt-2">Back to Dashboard</a>
            </div>
        </div>
    </div>
</div>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
