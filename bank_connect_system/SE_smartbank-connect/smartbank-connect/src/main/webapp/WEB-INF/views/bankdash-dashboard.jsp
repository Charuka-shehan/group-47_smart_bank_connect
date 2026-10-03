<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <title>Overview Dashboard | SmartBank Connect</title>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css" rel="stylesheet">
    <link href="<c:url value='/css/bankdash.css'/>" rel="stylesheet">
    <style>
        .quick-action-card {
            background: #ffffff;
            border-radius: 16px;
            padding: 20px;
            text-align: center;
            border: 1px solid #E6EFF5;
            transition: all 0.2s ease;
            text-decoration: none;
            display: block;
        }
        .quick-action-card:hover {
            transform: translateY(-2px);
            box-shadow: 0 8px 24px rgba(24, 20, 243, 0.08);
            border-color: #1814F3;
        }
        .quick-action-card i {
            font-size: 2rem;
            color: #1814F3;
            margin-bottom: 10px;
            display: block;
        }
        .quick-action-card span {
            font-weight: 600;
            color: #343C6A;
            font-size: 0.9rem;
        }
        .stat-card-custom {
            background: #ffffff;
            border-radius: 16px;
            padding: 20px;
            border: 1px solid #E6EFF5;
            display: flex;
            align-items: center;
            gap: 15px;
        }
        .stat-card-custom .icon-wrap {
            width: 50px;
            height: 50px;
            border-radius: 50%;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 1.5rem;
        }
        .stat-card-custom .val {
            font-size: 1.6rem;
            font-weight: 800;
            color: #343C6A;
            line-height: 1.2;
        }
        .stat-card-custom .lbl {
            font-size: 0.8rem;
            color: #718EBF;
            font-weight: 500;
            text-transform: uppercase;
            letter-spacing: 0.02em;
        }
    </style>
</head>
<body>
<div class="bd-root">

    <!-- ============================= SIDEBAR ============================= -->
    <aside class="bd-sidebar">
        <div class="bd-sidebar-logo">
            <i class="bi bi-bank2" style="font-size: 1.6rem; color: #1814F3;"></i>
            <span>SmartBank Connect.</span>
        </div>
        <ul class="bd-nav">
            <li class="bd-nav-item active"><a href="<c:url value='/bankdash/overview'/>"><i class="bi bi-house-door-fill"></i> Dashboard</a></li>
            <c:if test="${not empty userDto}">
                <c:if test="${userDto.role == 'CUSTOMER' || userDto.role == 'BANK_MANAGER'}">
                    <li class="bd-nav-item"><a href="<c:url value='/transfer'/>"><i class="bi bi-arrow-left-right"></i> Fund Transfer</a></li>
                    <li class="bd-nav-item"><a href="<c:url value='/history'/>"><i class="bi bi-clock-history"></i> Transactions</a></li>
                    <li class="bd-nav-item"><a href="<c:url value='/loans'/>"><i class="bi bi-cash-coin"></i> My Loans</a></li>
                </c:if>
                <c:if test="${userDto.role == 'BANK_OFFICER' || userDto.role == 'BANK_MANAGER'}">
                    <li class="bd-nav-item"><a href="<c:url value='/accounts/manage'/>"><i class="bi bi-wallet2"></i> Manage Accounts</a></li>
                </c:if>
                <c:if test="${userDto.role == 'BANK_OFFICER' || userDto.role == 'COMPLIANCE_OFFICER' || userDto.role == 'BANK_MANAGER'}">
                    <li class="bd-nav-item"><a href="<c:url value='/loans/review'/>"><i class="bi bi-file-earmark-check"></i> Loan Review</a></li>
                </c:if>
                <c:if test="${userDto.role == 'SYSTEM_ADMINISTRATOR' || userDto.role == 'BANK_MANAGER'}">
                    <li class="bd-nav-item"><a href="<c:url value='/admin/audit-logs'/>"><i class="bi bi-shield-check"></i> Audit Logs</a></li>
                    <li class="bd-nav-item"><a href="<c:url value='/admin/staff'/>"><i class="bi bi-people"></i> Staff Management</a></li>
                </c:if>
                <li class="bd-nav-item"><a href="<c:url value='/notifications'/>"><i class="bi bi-bell"></i> Notifications</a></li>
                <li class="bd-nav-item">
                    <form action="<c:url value='/logout'/>" method="post" id="logoutForm" style="display:none;"><c:if test="${not empty _csrf}"><input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" /></c:if></form>
                    <a href="javascript:document.getElementById('logoutForm').submit();"><i class="bi bi-box-arrow-right"></i> Log Out</a>
                </li>
            </c:if>
        </ul>
    </aside>

    <!-- =============================== MAIN =============================== -->
    <main class="bd-main">

        <!-- Topbar -->
        <div class="bd-topbar">
            <h1>Dashboard Overview</h1>
            <div class="bd-topbar-right">
                <c:if test="${not empty userDto}">
                    <div class="bd-welcome me-3">Welcome, <strong><c:out value="${userDto.fullName}"/></strong> <span class="badge bg-secondary ms-1 text-uppercase">${userDto.role}</span></div>
                </c:if>
                <div class="bd-icon-btn position-relative">
                    <a href="<c:url value='/notifications'/>" style="color: inherit; text-decoration: none;">
                        <i class="bi bi-bell"></i>
                        <c:if test="${unreadCount != null && unreadCount > 0}">
                            <span class="position-absolute top-0 start-100 translate-middle badge rounded-pill bg-danger" style="font-size: 0.65rem;">${unreadCount}</span>
                        </c:if>
                    </a>
                </div>
                <img class="bd-avatar" src="<c:url value='/images/bankdash/avatar.jpg'/>" alt="profile">
            </div>
        </div>

        <div class="container-fluid py-4" style="background: var(--bd-grey-50);">

            <!-- ======================= CUSTOMER VIEW ======================= -->
            <c:if test="${userDto.role == 'CUSTOMER'}">
                <div class="row g-4 mb-4">
                    <!-- My Cards Carousel -->
                    <div class="col-lg-8">
                        <div class="d-flex justify-content-between align-items-center mb-3">
                            <h4 class="mb-0 fw-bold" style="color: #343C6A; font-size: 1.1rem;">My Accounts / Cards</h4>
                            <form action="<c:url value='/accounts/request'/>" method="post" class="d-flex gap-2">
                                <c:if test="${not empty _csrf}"><input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" /></c:if>
                                <select name="accountType" class="form-select form-select-sm" style="border-radius: 8px;" required>
                                    <option value="">New account type...</option>
                                    <option value="SAVINGS">Savings</option>
                                    <option value="CURRENT">Current</option>
                                    <option value="FIXED_DEPOSIT">Fixed Deposit</option>
                                </select>
                                <button class="btn btn-sm btn-primary" style="border-radius: 8px;" type="submit">Request</button>
                            </form>
                        </div>
                        <div class="bd-cards-scroll">
                            <c:forEach var="acc" items="${accounts}" varStatus="loop">
                                <div class="bd-credit-card ${loop.index % 2 == 0 ? 'blue' : 'white'}">
                                    <div class="bd-credit-card-top">
                                        <div>
                                            <p class="bd-credit-card-label">Balance</p>
                                            <p class="bd-credit-card-balance">LKR <fmt:formatNumber value="${acc.balance}" maxFractionDigits="2"/></p>
                                        </div>
                                        <img class="bd-credit-card-chip" src="<c:url value='/images/bankdash/${loop.index % 2 == 0 ? "chip_white.png" : "chip_black.png"}'/>" alt="chip">
                                    </div>
                                    <div class="bd-credit-card-mid">
                                        <div><span>ACCOUNT NO</span><strong>${acc.accountNumber}</strong></div>
                                        <div><span>TYPE</span><strong>${acc.accountType}</strong></div>
                                    </div>
                                    <div class="bd-credit-card-bottom">
                                        <span class="bd-credit-card-number text-uppercase">${acc.status}</span>
                                        <img class="logo" src="<c:url value='/images/bankdash/${loop.index % 2 == 0 ? "bank-logo.svg" : "bank-logo-alt.svg"}'/>" alt="bank logo">
                                    </div>
                                </div>
                            </c:forEach>
                            <c:if test="${empty accounts}">
                                <div class="bd-credit-card blue">
                                    <div class="bd-credit-card-top">
                                        <div>
                                            <p class="bd-credit-card-label">Balance</p>
                                            <p class="bd-credit-card-balance">No Accounts Found</p>
                                        </div>
                                        <img class="bd-credit-card-chip" src="<c:url value='/images/bankdash/chip_white.png'/>" alt="chip">
                                    </div>
                                    <div class="bd-credit-card-mid">
                                        <div><span>CARD HOLDER</span><strong>${userDto.fullName}</strong></div>
                                        <div><span>STATUS</span><strong>N/A</strong></div>
                                    </div>
                                    <div class="bd-credit-card-bottom">
                                        <span class="bd-credit-card-number">No Active Accounts</span>
                                        <img class="logo" src="<c:url value='/images/bankdash/bank-logo.svg'/>" alt="bank logo">
                                    </div>
                                </div>
                            </c:if>
                        </div>
                    </div>

                    <!-- Recent Transactions -->
                    <div class="col-lg-4">
                        <div class="d-flex justify-content-between align-items-center mb-3">
                            <h4 class="mb-0 fw-bold" style="color: #343C6A; font-size: 1.1rem;">Recent Transactions</h4>
                            <c:if test="${not empty recentTransactions}">
                                <a href="<c:url value='/reports/statement/download'/>" target="_blank" class="bd-see-all"><i class="bi bi-file-earmark-pdf me-1"></i> PDF Statement</a>
                            </c:if>
                        </div>
                        <div class="card border-0 p-3 shadow-sm" style="border-radius: 16px; min-height: 235px; max-height: 235px; overflow-y: auto;">
                            <div class="bd-tx-list">
                                <c:forEach var="txn" items="${recentTransactions}">
                                    <div class="bd-tx-item d-flex align-items-center justify-content-between py-2 border-bottom">
                                        <div class="d-flex align-items-center gap-2">
                                            <div style="width: 35px; height: 35px; border-radius: 50%; background: ${txn.type == 'TRANSFER' ? '#FFF4E5' : '#E8FFF3'}; display: flex; align-items: center; justify-content: center;">
                                                <i class="bi ${txn.type == 'TRANSFER' ? 'bi-arrow-left-right text-warning' : 'bi-wallet2 text-success'}" style="font-size: 14px;"></i>
                                            </div>
                                            <div>
                                                <div style="font-weight: 600; color: #343C6A; font-size: 0.85rem;">${txn.type}</div>
                                                <div style="color: #718EBF; font-size: 0.75rem;">Ref: ${txn.referenceNumber}</div>
                                            </div>
                                        </div>
                                        <div class="text-end">
                                            <div style="font-weight: 700; font-size: 0.85rem; color: ${txn.type == 'TRANSFER' ? '#FF4B4A' : '#41D9C9'}">
                                                ${txn.type == 'TRANSFER' ? '-' : '+'} LKR <fmt:formatNumber value="${txn.amount}" maxFractionDigits="2"/>
                                            </div>
                                            <div style="color: #718EBF; font-size: 0.7rem;">${txn.status}</div>
                                        </div>
                                    </div>
                                </c:forEach>
                                <c:if test="${empty recentTransactions}">
                                    <div class="text-center py-5 text-muted small">No recent transactions</div>
                                </c:if>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- Graphs and Loan Application Table -->
                <div class="row g-4 mb-4">
                    <div class="col-lg-8">
                        <div class="card border-0 p-4 shadow-sm" style="border-radius: 16px;">
                            <h4 class="fw-bold mb-3" style="color: #343C6A; font-size: 1.1rem;">My Loan Applications</h4>
                            <div class="table-responsive">
                                <table class="table align-middle">
                                    <thead>
                                        <tr>
                                            <th>Application No.</th>
                                            <th>Type</th>
                                            <th>Amount (Rs.)</th>
                                            <th>Status</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <c:forEach var="l" items="${loans}">
                                            <tr>
                                                <td class="fw-semibold">#${l.applicationNumber}</td>
                                                <td>${l.loanType}</td>
                                                <td><fmt:formatNumber value="${l.requestedAmount}" maxFractionDigits="2"/></td>
                                                <td>
                                                    <span class="badge ${l.status == 'APPROVED' ? 'bg-success' : (l.status == 'REJECTED' ? 'bg-danger' : 'bg-warning text-dark')}">${l.status}</span>
                                                </td>
                                            </tr>
                                        </c:forEach>
                                        <c:if test="${empty loans}">
                                            <tr><td colspan="4" class="text-muted text-center py-4">No loan applications yet.</td></tr>
                                        </c:if>
                                    </tbody>
                                </table>
                            </div>
                        </div>
                    </div>
                    <div class="col-lg-4">
                        <div class="card border-0 p-4 shadow-sm" style="border-radius: 16px;">
                            <h4 class="fw-bold mb-3" style="color: #343C6A; font-size: 1.1rem;">Quick Actions</h4>
                            <div class="d-grid gap-2">
                                <a href="<c:url value='/transfer'/>" class="btn btn-primary py-2.5"><i class="bi bi-arrow-left-right me-1"></i> Transfer Funds</a>
                                <a href="<c:url value='/loans'/>" class="btn btn-outline-primary py-2.5"><i class="bi bi-cash-coin me-1"></i> Apply for a Loan</a>
                            </div>
                        </div>
                    </div>
                </div>
            </c:if>

            <!-- ======================= STAFF / MANAGER / ADMIN VIEW ======================= -->
            <c:if test="${userDto.role != 'CUSTOMER'}">
                <!-- Summary statistics cards -->
                <div class="row g-3 mb-4">
                    <div class="col-md-3">
                        <div class="stat-card-custom shadow-sm">
                            <div class="icon-wrap" style="background: #E8FFF3; color: #16DBAA;"><i class="bi bi-people-fill"></i></div>
                            <div>
                                <div class="lbl">Total Customers</div>
                                <div class="val">${totalCustomers}</div>
                            </div>
                        </div>
                    </div>
                    <div class="col-md-3">
                        <div class="stat-card-custom shadow-sm">
                            <div class="icon-wrap" style="background: #E7EDFF; color: #4C49ED;"><i class="bi bi-wallet2"></i></div>
                            <div>
                                <div class="lbl">Total Accounts</div>
                                <div class="val">${totalAccounts}</div>
                            </div>
                        </div>
                    </div>
                    <div class="col-md-3">
                        <div class="stat-card-custom shadow-sm">
                            <div class="icon-wrap" style="background: #FEEFE1; color: #FC7900;"><i class="bi bi-cash-coin"></i></div>
                            <div>
                                <div class="lbl">Total Balance</div>
                                <div class="val" style="font-size: 1.1rem;">LKR <fmt:formatNumber value="${totalBalance}" maxFractionDigits="2"/></div>
                            </div>
                        </div>
                    </div>
                    <div class="col-md-3">
                        <div class="stat-card-custom shadow-sm">
                            <div class="icon-wrap" style="background: #FFE3E7; color: #FE5C73;"><i class="bi bi-clock-history"></i></div>
                            <div>
                                <div class="lbl">Today's Transactions</div>
                                <div class="val">${todayTransactions}</div>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- Quick Administrative Actions -->
                <h4 class="fw-bold mb-3" style="color: #343C6A; font-size: 1.1rem;">Quick Administrative Links</h4>
                <div class="row g-3 mb-4">
                    <c:if test="${userDto.role == 'BANK_OFFICER' || userDto.role == 'BANK_MANAGER'}">
                        <div class="col-6 col-md-3">
                            <a href="<c:url value='/accounts/manage'/>" class="quick-action-card">
                                <i class="bi bi-wallet-fill"></i>
                                <span>Manage Accounts</span>
                            </a>
                        </div>
                    </c:if>
                    <c:if test="${userDto.role == 'BANK_OFFICER' || userDto.role == 'COMPLIANCE_OFFICER' || userDto.role == 'BANK_MANAGER'}">
                        <div class="col-6 col-md-3">
                            <a href="<c:url value='/loans/review'/>" class="quick-action-card">
                                <i class="bi bi-file-earmark-check-fill"></i>
                                <span>Loan Review Queue</span>
                            </a>
                        </div>
                    </c:if>
                    <c:if test="${userDto.role == 'SYSTEM_ADMINISTRATOR' || userDto.role == 'BANK_MANAGER'}">
                        <div class="col-6 col-md-3">
                            <a href="<c:url value='/admin/audit-logs'/>" class="quick-action-card">
                                <i class="bi bi-shield-lock-fill"></i>
                                <span>Audit Logs</span>
                            </a>
                        </div>
                        <div class="col-6 col-md-3">
                            <a href="<c:url value='/admin/staff'/>" class="quick-action-card">
                                <i class="bi bi-people-fill"></i>
                                <span>Staff Management</span>
                            </a>
                        </div>
                    </c:if>
                    <div class="col-6 col-md-3">
                        <a href="<c:url value='/notifications'/>" class="quick-action-card">
                            <i class="bi bi-bell-fill"></i>
                            <span>Notifications</span>
                        </a>
                    </div>
                </div>

                <!-- Charts & Analytics -->
                <h4 class="fw-bold mb-3" style="color: #343C6A; font-size: 1.1rem;">Analytics</h4>
                <div class="row g-4 mb-4">
                    <div class="col-lg-6">
                        <div class="card border-0 p-4 shadow-sm" style="border-radius: 16px;">
                            <h5 class="fw-bold mb-3" style="color: #343C6A; font-size: 0.95rem;">Daily Transaction Volume (Last 7 Days)</h5>
                            <canvas id="dailyTxnChart" height="180"></canvas>
                        </div>
                    </div>
                    <div class="col-lg-6">
                        <div class="card border-0 p-4 shadow-sm" style="border-radius: 16px;">
                            <h5 class="fw-bold mb-3" style="color: #343C6A; font-size: 0.95rem;">Monthly Deposits vs Withdrawals</h5>
                            <canvas id="depositsWithdrawalsChart" height="180"></canvas>
                        </div>
                    </div>
                    <div class="col-lg-6">
                        <div class="card border-0 p-4 shadow-sm" style="border-radius: 16px;">
                            <h5 class="fw-bold mb-3" style="color: #343C6A; font-size: 0.95rem;">Weekly Transfer Totals</h5>
                            <canvas id="weeklyTransferChart" height="180"></canvas>
                        </div>
                    </div>
                    <div class="col-lg-4">
                        <div class="card border-0 p-4 shadow-sm" style="border-radius: 16px;">
                            <h5 class="fw-bold mb-3" style="color: #343C6A; font-size: 0.95rem;">Account Opening Trend</h5>
                            <canvas id="accountOpeningChart" height="200"></canvas>
                        </div>
                    </div>
                    <div class="col-lg-4">
                        <div class="card border-0 p-4 shadow-sm" style="border-radius: 16px;">
                            <h5 class="fw-bold mb-3" style="color: #343C6A; font-size: 0.95rem;">Customer Growth Trend</h5>
                            <canvas id="customerGrowthChart" height="200"></canvas>
                        </div>
                    </div>
                </div>

                <!-- Pending Approvals & Transactions List -->
                <div class="row g-4 mb-4">
                    <div class="col-lg-8">
                        <div class="card border-0 p-4 shadow-sm" style="border-radius: 16px;">
                            <h4 class="fw-bold mb-3" style="color: #343C6A; font-size: 1.1rem;">Pending Account Requests</h4>
                            <div class="table-responsive">
                                <table class="table align-middle">
                                    <thead>
                                        <tr>
                                            <th>Account No.</th>
                                            <th>Customer</th>
                                            <th>Type</th>
                                            <th>Status</th>
                                            <th>Action</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <c:forEach var="a" items="${pendingAccounts}">
                                            <tr>
                                                <td class="fw-semibold">${a.accountNumber}</td>
                                                <td>${a.customer.fullName}</td>
                                                <td>${a.accountType}</td>
                                                <td><span class="badge bg-warning text-dark">${a.status}</span></td>
                                                <td>
                                                    <a href="<c:url value='/accounts/manage'/>" class="btn btn-sm btn-gold">Review</a>
                                                </td>
                                            </tr>
                                        </c:forEach>
                                        <c:if test="${empty pendingAccounts}">
                                            <tr><td colspan="5" class="text-muted text-center py-4">No pending account approvals.</td></tr>
                                        </c:if>
                                    </tbody>
                                </table>
                            </div>
                        </div>
                    </div>
                    <div class="col-lg-4">
                        <div class="card border-0 p-4 shadow-sm" style="border-radius: 16px;">
                            <h4 class="fw-bold mb-3" style="color: #343C6A; font-size: 1.1rem;">Operational Reports</h4>
                            <p class="text-muted small">Download systems information logs as PDF or CSV.</p>
                            <div class="d-grid gap-2">
                                <div class="btn-group" role="group">
                                    <a href="<c:url value='/reports/transactions/daily'/>" target="_blank" class="btn btn-sm btn-outline-secondary text-start py-2"><i class="bi bi-file-earmark-pdf me-2 text-danger"></i> Daily Transactions</a>
                                    <a href="<c:url value='/reports/transactions/daily/csv'/>" class="btn btn-sm btn-outline-secondary py-2" title="Download CSV"><i class="bi bi-filetype-csv text-success"></i></a>
                                </div>
                                <div class="btn-group" role="group">
                                    <a href="<c:url value='/reports/accounts/active'/>" target="_blank" class="btn btn-sm btn-outline-secondary text-start py-2"><i class="bi bi-file-earmark-pdf me-2 text-danger"></i> Active Accounts</a>
                                    <a href="<c:url value='/reports/accounts/active/csv'/>" class="btn btn-sm btn-outline-secondary py-2" title="Download CSV"><i class="bi bi-filetype-csv text-success"></i></a>
                                </div>
                                <div class="btn-group" role="group">
                                    <a href="<c:url value='/reports/accounts/frozen'/>" target="_blank" class="btn btn-sm btn-outline-secondary text-start py-2"><i class="bi bi-file-earmark-pdf me-2 text-danger"></i> Frozen Accounts</a>
                                    <a href="<c:url value='/reports/accounts/frozen/csv'/>" class="btn btn-sm btn-outline-secondary py-2" title="Download CSV"><i class="bi bi-filetype-csv text-success"></i></a>
                                </div>
                                <c:if test="${userDto.role == 'SYSTEM_ADMINISTRATOR' || userDto.role == 'BANK_MANAGER'}">
                                    <div class="btn-group" role="group">
                                        <a href="<c:url value='/reports/audit/logs'/>" target="_blank" class="btn btn-sm btn-outline-secondary text-start py-2"><i class="bi bi-file-earmark-pdf me-2 text-danger"></i> Audit Log</a>
                                        <a href="<c:url value='/reports/audit/logs/csv'/>" class="btn btn-sm btn-outline-secondary py-2" title="Download CSV"><i class="bi bi-filetype-csv text-success"></i></a>
                                    </div>
                                </c:if>
                            </div>
                        </div>
                    </div>
                </div>
            </c:if>

        </div>

        <!-- Footers -->
        <div class="bd-footer">
            <span>&copy; 2026 SmartBank Connect Banking System. All Rights Reserved.</span>
        </div>
    </main>
</div>

<c:if test="${not empty userDto && userDto.role != 'CUSTOMER'}">
<script src="https://cdn.jsdelivr.net/npm/chart.js@4.4.4/dist/chart.umd.min.js"></script>
<script>
    var sbChartPalette = { primary: '#1814F3', accent: '#4C49ED', green: '#16DBAA', orange: '#FC7900', red: '#FE5C73', grid: '#E6EFF5', text: '#718EBF' };
    Chart.defaults.font.family = "'Inter', sans-serif";
    Chart.defaults.color = sbChartPalette.text;

    new Chart(document.getElementById('dailyTxnChart'), {
        type: 'line',
        data: {
            labels: <c:out value="${dailyTxnLabels}" escapeXml="false"/>,
            datasets: [{
                label: 'Transactions',
                data: <c:out value="${dailyTxnData}" escapeXml="false"/>,
                borderColor: sbChartPalette.primary,
                backgroundColor: 'rgba(24,20,243,0.08)',
                tension: 0.35,
                fill: true,
                pointRadius: 3
            }]
        },
        options: { plugins: { legend: { display: false } }, scales: { y: { beginAtZero: true, grid: { color: sbChartPalette.grid } }, x: { grid: { display: false } } } }
    });

    new Chart(document.getElementById('depositsWithdrawalsChart'), {
        type: 'bar',
        data: {
            labels: <c:out value="${monthLabels}" escapeXml="false"/>,
            datasets: [
                { label: 'Deposits', data: <c:out value="${monthlyDepositData}" escapeXml="false"/>, backgroundColor: sbChartPalette.green, borderRadius: 6 },
                { label: 'Withdrawals', data: <c:out value="${monthlyWithdrawalData}" escapeXml="false"/>, backgroundColor: sbChartPalette.red, borderRadius: 6 }
            ]
        },
        options: { scales: { y: { beginAtZero: true, grid: { color: sbChartPalette.grid } }, x: { grid: { display: false } } } }
    });

    new Chart(document.getElementById('weeklyTransferChart'), {
        type: 'bar',
        data: {
            labels: <c:out value="${dailyTxnLabels}" escapeXml="false"/>,
            datasets: [{
                label: 'Transfer totals (LKR)',
                data: <c:out value="${weeklyTransferData}" escapeXml="false"/>,
                backgroundColor: sbChartPalette.accent,
                borderRadius: 6
            }]
        },
        options: { plugins: { legend: { display: false } }, scales: { y: { beginAtZero: true, grid: { color: sbChartPalette.grid } }, x: { grid: { display: false } } } }
    });

    new Chart(document.getElementById('loanRatioChart'), {
        type: 'pie',
        data: {
            labels: ['Approved', 'Rejected', 'Pending'],
            datasets: [{
                data: [${loanApprovedCount}, ${loanRejectedCount}, ${loanPendingCount}],
                backgroundColor: [sbChartPalette.green, sbChartPalette.red, sbChartPalette.orange]
            }]
        },
        options: { plugins: { legend: { position: 'bottom' } } }
    });

    new Chart(document.getElementById('accountOpeningChart'), {
        type: 'line',
        data: {
            labels: <c:out value="${monthLabels}" escapeXml="false"/>,
            datasets: [{
                label: 'Accounts Opened',
                data: <c:out value="${accountOpeningData}" escapeXml="false"/>,
                borderColor: sbChartPalette.accent,
                backgroundColor: 'rgba(76,73,237,0.08)',
                tension: 0.35,
                fill: true,
                pointRadius: 3
            }]
        },
        options: { plugins: { legend: { display: false } }, scales: { y: { beginAtZero: true, grid: { color: sbChartPalette.grid } }, x: { grid: { display: false } } } }
    });

    new Chart(document.getElementById('customerGrowthChart'), {
        type: 'line',
        data: {
            labels: <c:out value="${customerGrowthLabels}" escapeXml="false"/>,
            datasets: [{
                label: 'New Customers',
                data: <c:out value="${accountOpeningData}" escapeXml="false"/>,
                borderColor: sbChartPalette.orange,
                backgroundColor: 'rgba(252,121,0,0.08)',
                tension: 0.35,
                fill: true,
                pointRadius: 3
            }]
        },
        options: { plugins: { legend: { display: false } }, scales: { y: { beginAtZero: true, grid: { color: sbChartPalette.grid } }, x: { grid: { display: false } } } }
    });
</script>
</c:if>
</body>
</html>
