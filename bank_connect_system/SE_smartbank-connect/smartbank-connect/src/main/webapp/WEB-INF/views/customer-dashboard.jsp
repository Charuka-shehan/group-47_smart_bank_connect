<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <title>Customer Dashboard | SmartBank Connect</title>
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
            <li class="bd-nav-item active"><a href="<c:url value='/customer/dashboard'/>"><i class="bi bi-house-door-fill"></i> Dashboard</a></li>
            <li class="bd-nav-item"><a href="<c:url value='/transfer'/>"><i class="bi bi-arrow-left-right"></i> Fund Transfer</a></li>
            <li class="bd-nav-item"><a href="<c:url value='/history'/>"><i class="bi bi-clock-history"></i> Transactions</a></li>
            <li class="bd-nav-item"><a href="<c:url value='/loans'/>"><i class="bi bi-cash-coin"></i> My Loans</a></li>
            <li class="bd-nav-item"><a href="<c:url value='/customer/beneficiaries'/>"><i class="bi bi-people"></i> Beneficiaries</a></li>
            <li class="bd-nav-item"><a href="<c:url value='/customer/profile'/>"><i class="bi bi-person"></i> Profile</a></li>
            <li class="bd-nav-item"><a href="<c:url value='/notifications'/>"><i class="bi bi-bell"></i> Notifications</a></li>
            <li class="bd-nav-item">
                <form action="<c:url value='/logout'/>" method="post" id="logoutForm" style="display:none;"><c:if test="${not empty _csrf}"><input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" /></c:if></form>
                <a href="javascript:document.getElementById('logoutForm').submit();"><i class="bi bi-box-arrow-right"></i> Log Out</a>
            </li>
        </ul>
    </aside>

    <main class="bd-main">
        <div class="bd-topbar">
            <h1>Customer Overview</h1>
            <div class="bd-welcome">Role: <strong>${dashboardRole}</strong> | User: <strong><c:out value="${userDto.fullName}"/></strong></div>
        </div>

        <div class="container-fluid mt-4">
            <div class="row g-4">
                <div class="col-lg-8">
                    <div class="card p-4 mb-4 shadow-sm">
                        <div class="d-flex justify-content-between align-items-center mb-3">
                            <h4 class="mb-0">My Accounts</h4>
                            <a href="<c:url value='/reports/statement/download'/>" target="_blank" class="btn btn-sm btn-outline-primary"><i class="bi bi-file-earmark-pdf"></i> Download Statement</a>
                        </div>
                        <c:forEach var="acc" items="${accounts}">
                            <div class="d-flex align-items-center justify-content-between py-3 border-bottom">
                                <div>
                                    <h6 class="mb-0"><c:out value="${acc.accountType}"/></h6>
                                    <small class="text-muted">Acc No: <c:out value="${acc.accountNumber}"/></small>
                                </div>
                                <div class="text-end">
                                    <span class="badge bg-success">LKR <fmt:formatNumber value="${acc.balance}" maxFractionDigits="2"/></span>
                                    <div class="small text-muted mt-1"><c:out value="${acc.status}"/></div>
                                </div>
                            </div>
                        </c:forEach>
                        <c:if test="${empty accounts}">
                            <div class="text-muted text-center py-4">No accounts found.</div>
                        </c:if>
                    </div>

                    <div class="card p-4 mb-4 shadow-sm">
                        <h4 class="mb-3">Recent Transactions</h4>
                        <div class="table-responsive">
                            <table class="table align-middle">
                                <thead>
                                    <tr><th>Reference</th><th>Type</th><th>Amount</th><th>Status</th></tr>
                                </thead>
                                <tbody>
                                    <c:forEach var="txn" items="${recentTransactions}">
                                        <tr>
                                            <td><c:out value="${txn.referenceNumber}"/></td>
                                            <td><c:out value="${txn.type}"/></td>
                                            <td>LKR <fmt:formatNumber value="${txn.amount}" maxFractionDigits="2"/></td>
                                            <td><span class="badge ${txn.status == 'COMPLETED' ? 'bg-success' : 'bg-warning text-dark'}"><c:out value="${txn.status}"/></span></td>
                                        </tr>
                                    </c:forEach>
                                    <c:if test="${empty recentTransactions}">
                                        <tr><td colspan="4" class="text-muted text-center py-3">No recent transactions.</td></tr>
                                    </c:if>
                                </tbody>
                            </table>
                        </div>
                    </div>

                    <div class="card p-4 shadow-sm">
                        <h4 class="mb-3">Loan Status</h4>
                        <div class="table-responsive">
                            <table class="table align-middle">
                                <thead>
                                    <tr><th>Application</th><th>Type</th><th>Amount</th><th>Status</th></tr>
                                </thead>
                                <tbody>
                                    <c:forEach var="loan" items="${loans}">
                                        <tr>
                                            <td><c:out value="${loan.applicationNumber}"/></td>
                                            <td><c:out value="${loan.loanType}"/></td>
                                            <td>LKR <fmt:formatNumber value="${loan.requestedAmount}" maxFractionDigits="2"/></td>
                                            <td>
                                                <span class="badge ${loan.status == 'APPROVED' ? 'bg-success' : (loan.status == 'REJECTED' ? 'bg-danger' : 'bg-warning text-dark')}">
                                                    <c:out value="${loan.status}"/>
                                                </span>
                                            </td>
                                        </tr>
                                    </c:forEach>
                                    <c:if test="${empty loans}">
                                        <tr><td colspan="4" class="text-muted text-center py-3">No loan applications.</td></tr>
                                    </c:if>
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>

                <div class="col-lg-4">
                    <c:if test="${not empty atmCard}">
                        <div class="card p-4 shadow-sm mb-4">
                            <h5 class="mb-3">My ATM Card</h5>
                            <div class="p-3 rounded" style="background: linear-gradient(135deg, #1814F3, #4C49ED); color: white;">
                                <div class="d-flex justify-content-between align-items-center mb-3">
                                    <span class="small opacity-75">LankaTrust Bank</span>
                                    <i class="bi bi-wifi" style="transform: rotate(90deg);"></i>
                                </div>
                                <div class="fs-5 mb-3" style="letter-spacing: 2px;">
                                    <c:out value="${atmCard.maskedCardNumber}"/>
                                </div>
                                <div class="d-flex justify-content-between small opacity-75">
                                    <span>Expiry: <c:out value="${atmCard.expiryDate}"/></span>
                                    <span>CVV: ***</span>
                                </div>
                            </div>
                            <div class="mt-3 small text-muted">Status: <c:out value="${atmCard.status}"/></div>
                        </div>
                    </c:if>

                    <div class="card p-4 shadow-sm mb-4">
                        <h5>Quick Actions</h5>
                        <div class="d-grid gap-2 mt-3">
                            <a href="<c:url value='/transfer'/>" class="btn btn-primary"><i class="bi bi-arrow-left-right"></i> Transfer Funds</a>
                            <a href="<c:url value='/loans/apply'/>" class="btn btn-gold"><i class="bi bi-cash-coin"></i> Apply for Loan</a>
                            <a href="<c:url value='/demo/payment'/>" class="btn btn-outline-primary"><i class="bi bi-credit-card"></i> Demo Card Payment</a>
                        </div>
                    </div>

                    <div class="card p-4 shadow-sm">
                        <h5>Notifications</h5>
                        <c:forEach var="n" items="${notifications}" varStatus="loop">
                            <c:if test="${loop.index < 5}">
                                <div class="d-flex align-items-start py-2 ${not n.read ? 'fw-bold' : 'text-muted'} border-bottom">
                                    <i class="bi bi-bell-fill me-2 mt-1 text-primary"></i>
                                    <div>
                                        <div class="small"><c:out value="${n.title}"/></div>
                                        <div class="small text-truncate" style="max-width: 250px;"><c:out value="${n.message}"/></div>
                                    </div>
                                </div>
                            </c:if>
                        </c:forEach>
                        <c:if test="${empty notifications}">
                            <div class="text-muted text-center py-3 small">No notifications.</div>
                        </c:if>
                        <a href="<c:url value='/notifications'/>" class="btn btn-sm btn-outline-secondary mt-3">View All</a>
                    </div>
                </div>
            </div>
        </div>
    </main>
</div>
</body>
</html>
