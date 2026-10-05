<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <title>Bank Officer Dashboard | SmartBank Connect</title>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css" rel="stylesheet">
    <link href="<c:url value='/css/bankdash.css'/>" rel="stylesheet">
</head>
<body>
<div class="bd-root">
    <aside class="bd-sidebar">
        <div class="bd-sidebar-logo">
            <i class="bi bi-bank2" style="font-size: 1.5rem; color: #1814F3;"></i>
            <span class="ms-2">SmartBank Connect</span>
        </div>
        <ul class="bd-nav">
            <li class="bd-nav-item active"><a href="<c:url value='/officer/dashboard'/>"><i class="bi bi-house-door-fill"></i> Dashboard</a></li>
            <li class="bd-nav-item"><a href="<c:url value='/accounts/open'/>"><i class="bi bi-plus-circle"></i> Open Account</a></li>
            <li class="bd-nav-item"><a href="<c:url value='/accounts/manage'/>"><i class="bi bi-wallet2"></i> Manage Accounts</a></li>
            <li class="bd-nav-item"><a href="<c:url value='/officer/customers'/>"><i class="bi bi-search"></i> Customers</a></li>
            <li class="bd-nav-item"><a href="<c:url value='/officer/transfers'/>"><i class="bi bi-arrow-left-right"></i> Transfers</a></li>
            <li class="bd-nav-item"><a href="<c:url value='/officer/reports'/>"><i class="bi bi-bar-chart"></i> Reports</a></li>
            <li class="bd-nav-item"><a href="<c:url value='/notifications'/>"><i class="bi bi-bell"></i> Notifications</a></li>
            <li class="bd-nav-item">
                <form action="<c:url value='/logout'/>" method="post" id="logoutForm" style="display:none;"><c:if test="${not empty _csrf}"><input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" /></c:if></form>
                <a href="javascript:document.getElementById('logoutForm').submit();"><i class="bi bi-box-arrow-right"></i> Log Out</a>
            </li>
        </ul>
    </aside>

    <main class="bd-main">
        <div class="bd-topbar">
            <h1>Officer Control Panel</h1>
            <div class="bd-welcome">Role: <strong>${dashboardRole}</strong> | User: <strong><c:out value="${userDto.fullName}"/></strong></div>
        </div>

        <div class="container-fluid mt-4">
            <div class="row g-4">
                <div class="col-md-8">
                    <div class="card p-4 mb-4 shadow-sm">
                        <div class="d-flex justify-content-between align-items-center mb-3">
                            <h4 class="mb-0">Pending Account Requests</h4>
                            <a href="<c:url value='/accounts/open'/>" class="btn btn-sm btn-primary"><i class="bi bi-plus-circle"></i> Open Account</a>
                        </div>
                        <div class="table-responsive">
                            <table class="table align-middle">
                                <thead>
                                    <tr><th>Account No.</th><th>Customer</th><th>Type</th><th>Initial Deposit</th><th>Action</th></tr>
                                </thead>
                                <tbody>
                                    <c:forEach var="a" items="${pendingAccounts}">
                                        <tr>
                                            <td class="fw-semibold"><c:out value="${a.accountNumber}"/></td>
                                            <td><c:out value="${a.customer.fullName}"/></td>
                                            <td><c:out value="${a.accountType}"/></td>
                                            <td>LKR <fmt:formatNumber value="${a.initialDeposit}" maxFractionDigits="2"/></td>
                                            <td>
                                                <a href="<c:url value='/accounts/manage'/>" class="btn btn-sm btn-gold">Review</a>
                                            </td>
                                        </tr>
                                    </c:forEach>
                                    <c:if test="${empty pendingAccounts}">
                                        <tr><td colspan="5" class="text-muted text-center py-4">No pending account requests.</td></tr>
                                    </c:if>
                                </tbody>
                            </table>
                        </div>
                    </div>

                    <div class="card p-4 shadow-sm">
                        <h4 class="mb-3">Recent Customer Transactions</h4>
                        <div class="table-responsive">
                            <table class="table align-middle">
                                <thead>
                                    <tr><th>Reference</th><th>Customer</th><th>Type</th><th>Amount</th><th>Status</th></tr>
                                </thead>
                                <tbody>
                                    <c:forEach var="txn" items="${recentTransactions}">
                                        <tr>
                                            <td><c:out value="${txn.referenceNumber}"/></td>
                                            <td><c:out value="${txn.sourceAccount.customer.fullName}"/></td>
                                            <td><c:out value="${txn.type}"/></td>
                                            <td>LKR <fmt:formatNumber value="${txn.amount}" maxFractionDigits="2"/></td>
                                            <td><span class="badge ${txn.status == 'COMPLETED' ? 'bg-success' : 'bg-warning text-dark'}"><c:out value="${txn.status}"/></span></td>
                                        </tr>
                                    </c:forEach>
                                    <c:if test="${empty recentTransactions}">
                                        <tr><td colspan="5" class="text-muted text-center py-4">No recent transactions.</td></tr>
                                    </c:if>
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>
                <div class="col-md-4">
                    <div class="card p-4 shadow-sm mb-4">
                        <h5>Operational Reports</h5>
                        <p class="text-muted small">Generate system wide records.</p>
                        <div class="d-grid gap-2">
                            <a href="<c:url value='/reports/transactions/daily'/>" target="_blank" class="btn btn-sm btn-outline-secondary"><i class="bi bi-file-pdf"></i> Daily Transactions Report</a>
                            <a href="<c:url value='/reports/transactions/weekly'/>" target="_blank" class="btn btn-sm btn-outline-secondary"><i class="bi bi-file-pdf"></i> Weekly Transactions Report</a>
                            <a href="<c:url value='/reports/accounts/active'/>" target="_blank" class="btn btn-sm btn-outline-secondary"><i class="bi bi-file-pdf"></i> Active Accounts Report</a>
                            <a href="<c:url value='/reports/loans/status'/>" target="_blank" class="btn btn-sm btn-outline-secondary"><i class="bi bi-file-pdf"></i> Loan Status Report</a>
                        </div>
                    </div>

                    <div class="card p-4 shadow-sm">
                        <h5>Quick Actions</h5>
                        <div class="d-grid gap-2 mt-3">
                            <a href="<c:url value='/accounts/open'/>" class="btn btn-primary"><i class="bi bi-plus-circle"></i> Open New Account</a>
                            <a href="<c:url value='/loans/review'/>" class="btn btn-gold"><i class="bi bi-file-earmark-check"></i> Review Loans</a>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </main>
</div>
</body>
</html>
