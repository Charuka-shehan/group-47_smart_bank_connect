<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <title>Dashboard | SmartBank Connect</title>
    <jsp:include page="common/app-head.jsp"/>
</head>
<body>
<div class="sb-shell">
    <jsp:include page="common/sidebar.jsp"/>
    <div class="sb-main">
        <div class="sb-topbar">
            <h5 class="mb-0">Welcome, ${user.fullName}</h5>
            <span class="sb-badge sb-badge-active text-uppercase">${user.role}</span>
        </div>
        <div class="sb-content">

            <c:if test="${user.role == 'CUSTOMER'}">
                <div class="row g-3 mb-4">
                    <div class="col-md-4">
                        <div class="sb-card sb-stat-card">
                            <div class="label">My Accounts</div>
                            <div class="value">${accounts.size()}</div>
                        </div>
                    </div>
                    <div class="col-md-4">
                        <div class="sb-card sb-stat-card">
                            <div class="label">Total Balance (Rs.)</div>
                            <div class="value">
                                <fmt:formatNumber value="${0}" var="total" type="number"/>
                                <c:set var="totalBal" value="0"/>
                                <c:forEach var="a" items="${accounts}"><c:set var="totalBal" value="${totalBal + a.balance}"/></c:forEach>
                                <fmt:formatNumber value="${totalBal}" type="number" maxFractionDigits="2"/>
                            </div>
                        </div>
                    </div>
                    <div class="col-md-4">
                        <div class="sb-card sb-stat-card">
                            <div class="label">Loan Applications</div>
                            <div class="value">${loans.size()}</div>
                        </div>
                    </div>
                </div>

                <div class="sb-card mb-4">
                    <div class="d-flex justify-content-between align-items-center mb-3">
                        <h6 class="mb-0">My Accounts</h6>
                        <form action="<c:url value='/accounts/request'/>" method="post" class="d-flex gap-2">
<c:if test="${not empty _csrf}"><input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" /></c:if>
                            <select name="accountType" class="form-select form-select-sm" required>
                                <option value="">New account type...</option>
                                <option value="SAVINGS">Savings</option>
                                <option value="CURRENT">Current</option>
                                <option value="FIXED_DEPOSIT">Fixed Deposit</option>
                            </select>
                            <button class="btn btn-sm btn-gold" type="submit">Request</button>
                        </form>
                    </div>
                    <div class="table-responsive">
                        <table class="table align-middle">
                            <thead><tr><th>Account No.</th><th>Type</th><th>Balance (Rs.)</th><th>Status</th></tr></thead>
                            <tbody>
                            <c:forEach var="a" items="${accounts}">
                                <tr>
                                    <td>${a.accountNumber}</td>
                                    <td>${a.accountType}</td>
                                    <td><fmt:formatNumber value="${a.balance}" maxFractionDigits="2"/></td>
                                    <td><span class="sb-badge sb-badge-${a.status.toString().toLowerCase()}">${a.status}</span></td>
                                </tr>
                            </c:forEach>
                            <c:if test="${empty accounts}"><tr><td colspan="4" class="text-muted text-center py-3">No accounts yet. Request one above.</td></tr></c:if>
                            </tbody>
                        </table>
                    </div>
                </div>

                <div class="sb-card">
                    <h6 class="mb-3">My Loan Applications</h6>
                    <div class="table-responsive">
                        <table class="table align-middle">
                            <thead><tr><th>Application No.</th><th>Type</th><th>Amount (Rs.)</th><th>Status</th></tr></thead>
                            <tbody>
                            <c:forEach var="l" items="${loans}">
                                <tr>
                                    <td>${l.applicationNumber}</td>
                                    <td>${l.loanType}</td>
                                    <td><fmt:formatNumber value="${l.requestedAmount}" maxFractionDigits="2"/></td>
                                    <td><span class="sb-badge sb-badge-${l.status.toString().toLowerCase()}">${l.status}</span></td>
                                </tr>
                            </c:forEach>
                            <c:if test="${empty loans}"><tr><td colspan="4" class="text-muted text-center py-3">No loan applications yet.</td></tr></c:if>
                            </tbody>
                        </table>
                    </div>
                </div>
            </c:if>

            <c:if test="${user.role != 'CUSTOMER'}">
                <div class="row g-3 mb-4">
                    <div class="col-md-4">
                        <div class="sb-card sb-stat-card">
                            <div class="label">Total Accounts</div>
                            <div class="value">${allAccounts.size()}</div>
                        </div>
                    </div>
                    <div class="col-md-4">
                        <div class="sb-card sb-stat-card">
                            <div class="label">Pending Approvals</div>
                            <c:set var="pendingCount" value="0"/>
                            <c:forEach var="a" items="${allAccounts}"><c:if test="${a.status == 'PENDING_APPROVAL'}"><c:set var="pendingCount" value="${pendingCount + 1}"/></c:if></c:forEach>
                            <div class="value">${pendingCount}</div>
                        </div>
                    </div>
                    <div class="col-md-4">
                        <div class="sb-card sb-stat-card">
                            <div class="label">Loan Applications</div>
                            <div class="value">${allLoans.size()}</div>
                        </div>
                    </div>
                </div>
                <div class="sb-card">
                    <h6 class="mb-3">Quick Links</h6>
                    <div class="d-flex gap-2 flex-wrap">
                        <a href="<c:url value='/accounts/manage'/>" class="btn btn-outline-primary btn-sm">Manage Accounts</a>
                        <a href="<c:url value='/loans/review'/>" class="btn btn-outline-primary btn-sm">Loan Review Queue</a>
                        <c:if test="${user.role == 'SYSTEM_ADMINISTRATOR'}">
                            <a href="<c:url value='/admin/audit-logs'/>" class="btn btn-outline-primary btn-sm">Audit Logs</a>
                            <a href="<c:url value='/admin/staff'/>" class="btn btn-outline-primary btn-sm">Staff Management</a>
                        </c:if>
                    </div>
                </div>
            </c:if>

        </div>
    </div>
</div>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
