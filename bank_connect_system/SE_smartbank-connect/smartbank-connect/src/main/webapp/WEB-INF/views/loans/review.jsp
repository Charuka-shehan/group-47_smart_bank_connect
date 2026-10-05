<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <title>Loan Review Queue | SmartBank Connect</title>
    <jsp:include page="../common/app-head.jsp"/>
</head>
<body>
<div class="sb-shell">
    <jsp:include page="../common/sidebar.jsp"/>
    <div class="sb-main">
        <div class="sb-topbar"><h5 class="mb-0">Loan Review Queue</h5></div>
        <div class="sb-content">
            <c:if test="${not empty success}">
                <div class="alert alert-success alert-dismissible fade show" role="alert">
                    <i class="bi bi-check-circle me-2"></i><c:out value="${success}"/>
                    <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
                </div>
            </c:if>
            <c:if test="${not empty error}">
                <div class="alert alert-danger alert-dismissible fade show text-danger" role="alert">
                    <i class="bi bi-exclamation-triangle me-2"></i><c:out value="${error}"/>
                    <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
                </div>
            </c:if>

            <div class="sb-card">
                <div class="table-responsive">
                    <table class="table align-middle">
                        <thead><tr><th>App No.</th><th>Customer</th><th>Type</th><th>Amount (Rs.)</th><th>Status</th><th>Actions</th></tr></thead>
                        <tbody>
                        <c:forEach var="l" items="${loans}">
                            <tr>
                                <td>${l.applicationNumber}</td>
                                <td>${l.customer.fullName}</td>
                                <td>${l.loanType}</td>
                                <td><fmt:formatNumber value="${l.requestedAmount}" maxFractionDigits="2"/></td>
                                <td><span class="sb-badge sb-badge-${l.status.toString().toLowerCase()}">${l.status}</span></td>
                                <td class="d-flex gap-1 flex-wrap">
                                    <sec:authorize access="hasAnyRole('BANK_OFFICER','SYSTEM_ADMINISTRATOR')">
                                        <c:if test="${l.status == 'SUBMITTED'}">
                                            <form action="<c:url value='/loans/${l.id}/verify'/>" method="post">
                                                <c:if test="${not empty _csrf}"><input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" /></c:if>
                                                <input type="hidden" name="remarks" value="Documents verified by officer">
                                                <button class="btn btn-sm btn-outline-primary">Verify</button>
                                            </form>
                                        </c:if>
                                    </sec:authorize>
                                    <sec:authorize access="hasAnyRole('COMPLIANCE_OFFICER','BANK_MANAGER')">
                                        <c:if test="${l.status == 'UNDER_VERIFICATION'}">
                                            <form action="<c:url value='/loans/${l.id}/compliance'/>" method="post">
                                                <c:if test="${not empty _csrf}"><input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" /></c:if>
                                                <input type="hidden" name="remarks" value="Compliance and risk checks passed">
                                                <button class="btn btn-sm btn-outline-info">Compliance Check</button>
                                            </form>
                                        </c:if>
                                    </sec:authorize>
                                    <sec:authorize access="hasAnyRole('BANK_MANAGER','SYSTEM_ADMINISTRATOR')">
                                        <c:if test="${l.status == 'COMPLIANCE_CHECK'}">
                                            <form action="<c:url value='/loans/${l.id}/approve'/>" method="post">
                                                <c:if test="${not empty _csrf}"><input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" /></c:if>
                                                <button class="btn btn-sm btn-success">Approve</button>
                                            </form>
                                        </c:if>
                                    </sec:authorize>
                                    <c:if test="${l.status != 'APPROVED' && l.status != 'REJECTED'}">
                                        <form action="<c:url value='/loans/${l.id}/reject'/>" method="post"
                                              onsubmit="var r = prompt('Enter rejection reason:'); if (!r) return false; this.querySelector('input[name=reason]').value = r;">
                                                <c:if test="${not empty _csrf}"><input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" /></c:if>
                                            <input type="hidden" name="reason" value="">
                                            <button class="btn btn-sm btn-outline-danger">Reject</button>
                                        </form>
                                    </c:if>
                                </td>
                            </tr>
                        </c:forEach>
                        <c:if test="${empty loans}"><tr><td colspan="6" class="text-muted text-center py-3">No loan applications in the queue.</td></tr></c:if>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    </div>
</div>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
