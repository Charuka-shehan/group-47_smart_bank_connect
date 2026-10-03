<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <title>General Manager Command Center | SmartBank Connect</title>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <!-- Google Fonts Inter -->
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700;800&display=swap" rel="stylesheet">
    <!-- Bootstrap 5 & Icons -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css" rel="stylesheet">
    <link href="<c:url value='/css/bankdash.css'/>" rel="stylesheet">

    <style>
        :root {
            --gm-navy-dark: #0f172a;
            --gm-navy-card: #1e293b;
            --gm-blue-primary: #1814F3;
            --gm-blue-soft: #4F46E5;
            --gm-emerald: #10B981;
            --gm-amber: #F59E0B;
            --gm-rose: #EF4444;
            --gm-cyan: #06B6D4;
            --gm-purple: #8B5CF6;
            --gm-border-light: #E2E8F0;
            --gm-card-bg: #FFFFFF;
        }

        body {
            font-family: 'Inter', sans-serif;
            background-color: #F8FAFC;
            color: #1E293B;
        }

        /* Topbar & Hero */
        .gm-topbar {
            display: flex;
            justify-content: space-between;
            align-items: center;
            background: #FFFFFF;
            padding: 18px 28px;
            border-bottom: 1px solid var(--gm-border-light);
            position: sticky;
            top: 0;
            z-index: 100;
        }

        .gm-live-badge {
            display: inline-flex;
            align-items: center;
            gap: 8px;
            padding: 6px 14px;
            border-radius: 9999px;
            background: rgba(16, 185, 129, 0.1);
            color: #059669;
            font-size: 0.82rem;
            font-weight: 600;
        }

        .pulse-dot {
            width: 8px;
            height: 8px;
            border-radius: 50%;
            background-color: #10B981;
            box-shadow: 0 0 0 0 rgba(16, 185, 129, 0.7);
            animation: pulse-ring 1.8s infinite cubic-bezier(0.66, 0, 0, 1);
        }

        @keyframes pulse-ring {
            0% { box-shadow: 0 0 0 0 rgba(16, 185, 129, 0.7); }
            70% { box-shadow: 0 0 0 8px rgba(16, 185, 129, 0); }
            100% { box-shadow: 0 0 0 0 rgba(16, 185, 129, 0); }
        }

        /* Hero Banner */
        .gm-hero-banner {
            background: linear-gradient(135deg, #0F172A 0%, #1E293B 60%, #1E1B4B 100%);
            border-radius: 20px;
            padding: 32px 36px;
            color: #FFFFFF;
            position: relative;
            overflow: hidden;
            box-shadow: 0 14px 34px rgba(15, 23, 42, 0.15);
            margin-bottom: 28px;
        }

        .gm-hero-banner::after {
            content: '';
            position: absolute;
            top: -40%;
            right: -10%;
            width: 380px;
            height: 380px;
            border-radius: 50%;
            background: radial-gradient(circle, rgba(79, 70, 229, 0.25) 0%, transparent 70%);
            pointer-events: none;
        }

        /* KPI Metric Cards */
        .gm-kpi-card {
            background: #FFFFFF;
            border: 1px solid var(--gm-border-light);
            border-radius: 18px;
            padding: 22px;
            transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
            position: relative;
            height: 100%;
            display: flex;
            flex-direction: column;
            justify-content: space-between;
        }

        .gm-kpi-card:hover {
            transform: translateY(-4px);
            box-shadow: 0 12px 28px rgba(15, 23, 42, 0.08);
            border-color: #CBD5E1;
        }

        .gm-icon-box {
            width: 48px;
            height: 48px;
            border-radius: 14px;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 1.4rem;
        }

        .icon-blue { background: rgba(24, 20, 243, 0.08); color: #1814F3; }
        .icon-emerald { background: rgba(16, 185, 129, 0.1); color: #10B981; }
        .icon-amber { background: rgba(245, 158, 11, 0.1); color: #F59E0B; }
        .icon-rose { background: rgba(239, 68, 68, 0.1); color: #EF4444; }
        .icon-cyan { background: rgba(6, 182, 212, 0.1); color: #06B6D4; }
        .icon-purple { background: rgba(139, 92, 246, 0.1); color: #8B5CF6; }

        .gm-kpi-val {
            font-size: 1.85rem;
            font-weight: 800;
            color: #0F172A;
            letter-spacing: -0.02em;
            margin-top: 14px;
            margin-bottom: 4px;
            line-height: 1.1;
        }

        .gm-kpi-lbl {
            font-size: 0.82rem;
            color: #64748B;
            font-weight: 600;
            text-transform: uppercase;
            letter-spacing: 0.04em;
        }

        /* Chart Cards */
        .gm-chart-card {
            background: #FFFFFF;
            border: 1px solid var(--gm-border-light);
            border-radius: 20px;
            padding: 24px;
            box-shadow: 0 4px 16px rgba(15, 23, 42, 0.03);
            margin-bottom: 26px;
            height: 100%;
        }

        .gm-card-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 20px;
        }

        .gm-card-title {
            font-size: 1.05rem;
            font-weight: 700;
            color: #0F172A;
            margin: 0;
            display: flex;
            align-items: center;
            gap: 10px;
        }

        .chart-toggle-btn {
            padding: 5px 12px;
            font-size: 0.78rem;
            font-weight: 600;
            border-radius: 8px;
            border: 1px solid var(--gm-border-light);
            background: #F8FAFC;
            color: #64748B;
            cursor: pointer;
            transition: all 0.15s ease;
        }

        .chart-toggle-btn.active {
            background: #1814F3;
            color: #FFFFFF;
            border-color: #1814F3;
        }

        /* Modern Department Console Cards */
        .gm-console-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(320px, 1fr));
            gap: 20px;
            margin-bottom: 28px;
        }

        .gm-console-card {
            background: #FFFFFF;
            border: 1px solid var(--gm-border-light);
            border-radius: 18px;
            padding: 24px;
            position: relative;
            transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
            overflow: hidden;
            display: flex;
            flex-direction: column;
            justify-content: space-between;
        }

        .gm-console-card::before {
            content: '';
            position: absolute;
            top: 0;
            left: 0;
            right: 0;
            height: 4px;
        }

        .console-retail::before { background: linear-gradient(90deg, #3B82F6, #1814F3); }
        .console-officer::before { background: linear-gradient(90deg, #10B981, #059669); }
        .console-relations::before { background: linear-gradient(90deg, #F59E0B, #D97706); }
        .console-admin::before { background: linear-gradient(90deg, #EF4444, #B91C1C); }
        .console-compliance::before { background: linear-gradient(90deg, #06B6D4, #0284C7); }
        .console-approvals::before { background: linear-gradient(90deg, #8B5CF6, #6D28D9); }

        .gm-console-card:hover {
            transform: translateY(-5px);
            box-shadow: 0 16px 36px rgba(15, 23, 42, 0.08);
            border-color: #CBD5E1;
        }

        .gm-console-btn {
            border-radius: 10px;
            font-weight: 600;
            font-size: 0.85rem;
            padding: 10px 18px;
            width: 100%;
            display: flex;
            align-items: center;
            justify-content: center;
            gap: 8px;
            transition: all 0.2s ease;
            text-decoration: none;
        }

        .btn-retail { background: #EEF2FF; color: #1814F3; border: 1px solid #C7D2FE; }
        .btn-retail:hover { background: #1814F3; color: #FFFFFF; }

        .btn-officer { background: #ECFDF5; color: #059669; border: 1px solid #A7F3D0; }
        .btn-officer:hover { background: #059669; color: #FFFFFF; }

        .btn-relations { background: #FFFBEB; color: #D97706; border: 1px solid #FDE68A; }
        .btn-relations:hover { background: #D97706; color: #FFFFFF; }

        .btn-admin { background: #FEF2F2; color: #DC2626; border: 1px solid #FECACA; }
        .btn-admin:hover { background: #DC2626; color: #FFFFFF; }

        .btn-compliance { background: #ECFEFF; color: #0891B2; border: 1px solid #A5F3FC; }
        .btn-compliance:hover { background: #0891B2; color: #FFFFFF; }

        .btn-approvals { background: #F5F3FF; color: #7C3AED; border: 1px solid #DDD6FE; }
        .btn-approvals:hover { background: #7C3AED; color: #FFFFFF; }

        /* Tables & Lists */
        .gm-table th {
            font-size: 0.78rem;
            text-transform: uppercase;
            letter-spacing: 0.04em;
            color: #64748B;
            font-weight: 600;
            border-bottom: 2px solid var(--gm-border-light);
            padding: 12px 14px;
        }

        .gm-table td {
            font-size: 0.88rem;
            vertical-align: middle;
            padding: 14px;
            border-bottom: 1px solid #F1F5F9;
        }

        .badge-subtle-success { background: #ECFDF5; color: #059669; font-weight: 600; border-radius: 6px; padding: 4px 8px; }
        .badge-subtle-warning { background: #FFFBEB; color: #D97706; font-weight: 600; border-radius: 6px; padding: 4px 8px; }
        .badge-subtle-danger { background: #FEF2F2; color: #DC2626; font-weight: 600; border-radius: 6px; padding: 4px 8px; }
        .badge-subtle-info { background: #EFF6FF; color: #2563EB; font-weight: 600; border-radius: 6px; padding: 4px 8px; }

        /* Custom Scrollbar */
        ::-webkit-scrollbar { width: 6px; height: 6px; }
        ::-webkit-scrollbar-thumb { background: #CBD5E1; border-radius: 4px; }
        ::-webkit-scrollbar-thumb:hover { background: #94A3B8; }
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
            <li class="bd-nav-item active"><a href="<c:url value='/manager/dashboard'/>"><i class="bi bi-house-door-fill"></i> Manager Dashboard</a></li>
            <li class="bd-nav-item"><a href="<c:url value='/bankdash/overview'/>"><i class="bi bi-speedometer2"></i> Global Analytics</a></li>
            <li class="bd-nav-item"><a href="<c:url value='/approvals'/>"><i class="bi bi-check2-square"></i> Approvals Desk <c:if test="${pendingCount > 0 || pendingLoans > 0}"><span class="badge bg-danger rounded-pill ms-auto">${pendingCount + pendingLoans}</span></c:if></a></li>
            <li class="bd-nav-item"><a href="<c:url value='/branches'/>"><i class="bi bi-building"></i> Branch Operations</a></li>
            <li class="bd-nav-item"><a href="<c:url value='/accounts/manage'/>"><i class="bi bi-wallet2"></i> Accounts Registry</a></li>
            <li class="bd-nav-item"><a href="<c:url value='/officer/dashboard'/>"><i class="bi bi-briefcase"></i> Officer Console</a></li>
            <li class="bd-nav-item"><a href="<c:url value='/customer-relations/dashboard'/>"><i class="bi bi-chat-dots"></i> Relations Console</a></li>
            <li class="bd-nav-item"><a href="<c:url value='/admin/dashboard'/>"><i class="bi bi-gear"></i> Admin Console</a></li>
            <li class="bd-nav-item"><a href="<c:url value='/compliance/dashboard'/>"><i class="bi bi-shield-check"></i> Compliance Oversight</a></li>
            <li class="bd-nav-item">
                <form action="<c:url value='/logout'/>" method="post" id="logoutForm" style="display:none;"><c:if test="${not empty _csrf}"><input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" /></c:if></form>
                <a href="javascript:document.getElementById('logoutForm').submit();"><i class="bi bi-box-arrow-right"></i> Log Out</a>
            </li>
        </ul>
    </aside>

    <!-- ============================= MAIN CONTENT ============================= -->
    <main class="bd-main p-0">
        <!-- TOPBAR -->
        <div class="gm-topbar">
            <div>
                <h4 class="fw-bold mb-0 text-dark">General Manager Control Room</h4>
                <small class="text-muted">Master surveillance, cross-functional consoles & executive authority</small>
            </div>
            <div class="d-flex align-items-center gap-3">
                <div class="gm-live-badge">
                    <div class="pulse-dot"></div>
                    <span>SYSTEM ONLINE</span>
                </div>
                <div class="vr h-50 my-auto text-muted"></div>
                <div class="d-flex align-items-center gap-2">
                    <div class="rounded-circle bg-primary text-white d-flex align-items-center justify-content-center fw-bold" style="width: 38px; height: 38px; font-size: 0.9rem;">
                        <c:out value="${userDto != null ? userDto.fullName.substring(0, 1) : 'M'}"/>
                    </div>
                    <div>
                        <div class="fw-semibold text-dark" style="font-size: 0.88rem;"><c:out value="${userDto.fullName}"/></div>
                        <div class="badge bg-primary-subtle text-primary" style="font-size: 0.72rem;">${dashboardRole}</div>
                    </div>
                </div>
            </div>
        </div>

        <div class="container-fluid px-4 py-4">
            <!-- HERO BANNER -->
            <div class="gm-hero-banner">
                <div class="row align-items-center">
                    <div class="col-lg-8">
                        <span class="badge bg-white bg-opacity-20 text-white mb-2 px-3 py-1 fw-medium" style="backdrop-filter: blur(4px);">
                            <i class="bi bi-cpu me-1"></i> Unified Banking Core v3.3
                        </span>
                        <h2 class="fw-extrabold mb-2 text-white">Executive Command & Surveillance Center</h2>
                        <p class="text-white-50 mb-4" style="max-width: 640px; font-size: 0.95rem;">
                            As the Branch / General Manager, you have complete administrative override, cross-departmental supervision,
                            and real-time financial oversight across all banking units.
                        </p>
                        <div class="d-flex flex-wrap gap-2">
                            <a href="<c:url value='/approvals'/>" class="btn btn-primary px-4 py-2 fw-semibold" style="border-radius: 10px; background: #1814F3; border: none;">
                                <i class="bi bi-check2-circle me-1"></i> Review Pending Approvals (${pendingCount + pendingLoans})
                            </a>
                            <a href="<c:url value='/reports/audit/logs'/>" target="_blank" class="btn btn-outline-light px-3 py-2 fw-semibold" style="border-radius: 10px;">
                                <i class="bi bi-file-earmark-lock me-1"></i> Audit Trail
                            </a>
                            <a href="<c:url value='/branches'/>" class="btn btn-outline-light px-3 py-2 fw-semibold" style="border-radius: 10px;">
                                <i class="bi bi-building-gear me-1"></i> Branch Settings
                            </a>
                        </div>
                    </div>
                    <div class="col-lg-4 text-end d-none d-lg-block">
                        <div class="p-3 rounded-4 bg-white bg-opacity-10 border border-white border-opacity-10 text-start" style="backdrop-filter: blur(10px);">
                            <div class="text-white-50 small text-uppercase fw-semibold mb-1">Total Bank Liquidity / Deposits</div>
                            <div class="h3 fw-bold text-white mb-2">
                                Rs. <fmt:formatNumber value="${totalBalance}" minFractionDigits="2" maxFractionDigits="2"/>
                            </div>
                            <div class="d-flex align-items-center text-success small fw-medium">
                                <i class="bi bi-arrow-up-right me-1"></i>
                                <span>100% Capital Adequacy Maintained</span>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <!-- PRIMARY 6 KPI STATS RIBBON -->
            <div class="row g-3 mb-4">
                <!-- 1. Total Bank Balance -->
                <div class="col-xl-2 col-md-4 col-sm-6">
                    <div class="gm-kpi-card">
                        <div class="d-flex justify-content-between align-items-center">
                            <div class="gm-kpi-lbl">Total Vault Assets</div>
                            <div class="gm-icon-box icon-blue"><i class="bi bi-bank"></i></div>
                        </div>
                        <div>
                            <div class="gm-kpi-val" style="font-size: 1.35rem;">
                                Rs. <fmt:formatNumber value="${totalBalance}" minFractionDigits="0" maxFractionDigits="0"/>
                            </div>
                            <small class="text-success fw-semibold"><i class="bi bi-shield-check me-1"></i>Live Deposits</small>
                        </div>
                    </div>
                </div>

                <!-- 2. Active Accounts -->
                <div class="col-xl-2 col-md-4 col-sm-6">
                    <div class="gm-kpi-card">
                        <div class="d-flex justify-content-between align-items-center">
                            <div class="gm-kpi-lbl">Total Accounts</div>
                            <div class="gm-icon-box icon-emerald"><i class="bi bi-wallet2"></i></div>
                        </div>
                        <div>
                            <div class="gm-kpi-val">${totalAccounts != null ? totalAccounts : 0}</div>
                            <small class="text-muted">${savingsCount} Sav · ${currentCount} Cur · ${fdCount} FD</small>
                        </div>
                    </div>
                </div>

                <!-- 3. Total Customers -->
                <div class="col-xl-2 col-md-4 col-sm-6">
                    <div class="gm-kpi-card">
                        <div class="d-flex justify-content-between align-items-center">
                            <div class="gm-kpi-lbl">Active Customers</div>
                            <div class="gm-icon-box icon-cyan"><i class="bi bi-people-fill"></i></div>
                        </div>
                        <div>
                            <div class="gm-kpi-val">${totalCustomers != null ? totalCustomers : 0}</div>
                            <small class="text-info fw-semibold"><i class="bi bi-person-check me-1"></i>KYC Verified</small>
                        </div>
                    </div>
                </div>

                <!-- 4. Pending Accounts -->
                <div class="col-xl-2 col-md-4 col-sm-6">
                    <a href="<c:url value='/approvals'/>" class="text-decoration-none">
                        <div class="gm-kpi-card" style="border-left: 4px solid #F59E0B;">
                            <div class="d-flex justify-content-between align-items-center">
                                <div class="gm-kpi-lbl">Pending Accounts</div>
                                <div class="gm-icon-box icon-amber"><i class="bi bi-person-plus"></i></div>
                            </div>
                            <div>
                                <div class="gm-kpi-val text-warning">${pendingCount != null ? pendingCount : 0}</div>
                                <small class="text-warning fw-semibold"><i class="bi bi-clock-history me-1"></i>Action Required</small>
                            </div>
                        </div>
                    </a>
                </div>

                <!-- 5. Pending Loans -->
                <div class="col-xl-2 col-md-4 col-sm-6">
                    <a href="<c:url value='/loans/review'/>" class="text-decoration-none">
                        <div class="gm-kpi-card" style="border-left: 4px solid #8B5CF6;">
                            <div class="d-flex justify-content-between align-items-center">
                                <div class="gm-kpi-lbl">Pending Loans</div>
                                <div class="gm-icon-box icon-purple"><i class="bi bi-cash-stack"></i></div>
                            </div>
                            <div>
                                <div class="gm-kpi-val text-purple" style="color: #8B5CF6;">${pendingLoans != null ? pendingLoans : 0}</div>
                                <small class="text-purple fw-semibold"><i class="bi bi-hourglass-split me-1"></i>Credit Sanction</small>
                            </div>
                        </div>
                    </a>
                </div>

                <!-- 6. 24h Transactions -->
                <div class="col-xl-2 col-md-4 col-sm-6">
                    <div class="gm-kpi-card">
                        <div class="d-flex justify-content-between align-items-center">
                            <div class="gm-kpi-lbl">Today's Txns</div>
                            <div class="gm-icon-box icon-rose"><i class="bi bi-arrow-left-right"></i></div>
                        </div>
                        <div>
                            <div class="gm-kpi-val">${todayTransactions != null ? todayTransactions : 0}</div>
                            <small class="text-muted">Vol: Rs. <fmt:formatNumber value="${todayVolume}" minFractionDigits="0" maxFractionDigits="0"/></small>
                        </div>
                    </div>
                </div>
            </div>

            <!-- ============================= DIAGRAMS & CHARTS ROW 1 ============================= -->
            <div class="row g-4 mb-4">
                <!-- CHART 1: 7-DAY TRANSACTION VELOCITY & VOLUME -->
                <div class="col-lg-8">
                    <div class="gm-chart-card">
                        <div class="gm-card-header">
                            <div>
                                <h5 class="gm-card-title"><i class="bi bi-graph-up-arrow text-primary"></i> 7-Day Transaction Velocity & Activity Trend</h5>
                                <small class="text-muted">Daily real-time processing volume across retail & branch channels</small>
                            </div>
                            <div class="d-flex gap-1 bg-light p-1 rounded-3">
                                <button type="button" class="chart-toggle-btn active" id="btnModeCount" onclick="toggleDailyMode('count')">Count</button>
                                <button type="button" class="chart-toggle-btn" id="btnModeVolume" onclick="toggleDailyMode('volume')">Volume (LKR)</button>
                            </div>
                        </div>
                        <div style="height: 290px;">
                            <canvas id="dailyVelocityChart"></canvas>
                        </div>
                    </div>
                </div>

                <!-- CHART 2: ACCOUNT PORTFOLIO DONUT -->
                <div class="col-lg-4">
                    <div class="gm-chart-card">
                        <div class="gm-card-header">
                            <div>
                                <h5 class="gm-card-title"><i class="bi bi-pie-chart text-success"></i> Account Portfolio Distribution</h5>
                                <small class="text-muted">Share of accounts by product type</small>
                            </div>
                        </div>
                        <div style="height: 220px; position: relative;">
                            <canvas id="accountDonutChart"></canvas>
                        </div>
                        <div class="row text-center mt-3 pt-2 border-top">
                            <div class="col-4">
                                <span class="d-block small text-muted"><span class="badge rounded-pill bg-primary p-1 me-1"></span>Savings</span>
                                <strong class="text-dark">${savingsCount}</strong>
                            </div>
                            <div class="col-4">
                                <span class="d-block small text-muted"><span class="badge rounded-pill bg-success p-1 me-1"></span>Current</span>
                                <strong class="text-dark">${currentCount}</strong>
                            </div>
                            <div class="col-4">
                                <span class="d-block small text-muted"><span class="badge rounded-pill bg-warning p-1 me-1"></span>Fixed Dep</span>
                                <strong class="text-dark">${fdCount}</strong>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <!-- ============================= DIAGRAMS & CHARTS ROW 2 ============================= -->
            <div class="row g-4 mb-4">
                <!-- CHART 3: MONTHLY CASH FLOW (DEPOSITS VS WITHDRAWALS) -->
                <div class="col-lg-7">
                    <div class="gm-chart-card">
                        <div class="gm-card-header">
                            <div>
                                <h5 class="gm-card-title"><i class="bi bi-bar-chart-fill text-info"></i> Liquidity Flow: Deposits vs. Withdrawals</h5>
                                <small class="text-muted">6-month rolling inflows against customer withdrawals (LKR)</small>
                            </div>
                            <span class="badge bg-success-subtle text-success px-2 py-1"><i class="bi bi-shield-check me-1"></i>Net Positive Liquidity</span>
                        </div>
                        <div style="height: 260px;">
                            <canvas id="liquidityFlowChart"></canvas>
                        </div>
                    </div>
                </div>

                <!-- CHART 4: LOAN PORTFOLIO RISK & SANCTION STATUS -->
                <div class="col-lg-5">
                    <div class="gm-chart-card">
                        <div class="gm-card-header">
                            <div>
                                <h5 class="gm-card-title"><i class="bi bi-shield-shaded text-warning"></i> Credit & Loan Risk Distribution</h5>
                                <small class="text-muted">Sanction ratio and approval underwriting status</small>
                            </div>
                        </div>
                        <div style="height: 200px;">
                            <canvas id="loanStatusChart"></canvas>
                        </div>
                        <div class="d-flex justify-content-around text-center mt-3 pt-2 border-top">
                            <div>
                                <span class="small text-muted d-block">Approved</span>
                                <span class="badge bg-success px-2 py-1">${loanApprovedCount} Sanctioned</span>
                            </div>
                            <div>
                                <span class="small text-muted d-block">Under Review</span>
                                <span class="badge bg-warning text-dark px-2 py-1">${loanPendingCount} Pending</span>
                            </div>
                            <div>
                                <span class="small text-muted d-block">Rejected</span>
                                <span class="badge bg-danger px-2 py-1">${loanRejectedCount} Declined</span>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <!-- ============================= EXECUTIVE DEPARTMENT CONSOLES ============================= -->
            <div class="d-flex justify-content-between align-items-center mb-3">
                <div>
                    <h5 class="fw-bold mb-1 text-dark"><i class="bi bi-grid-3x3-gap-fill text-primary me-2"></i>Executive Department Command Consoles</h5>
                    <p class="text-muted small mb-0">Direct managerial access & override authority for every bank sub-system</p>
                </div>
                <span class="badge bg-light text-secondary border px-3 py-2"><i class="bi bi-lock-fill text-success me-1"></i>Manager Super-User Clearance</span>
            </div>

            <div class="gm-console-grid">
                <!-- 1. Retail Banking Console -->
                <div class="gm-console-card console-retail">
                    <div>
                        <div class="d-flex justify-content-between align-items-start mb-3">
                            <div class="gm-icon-box icon-blue"><i class="bi bi-person-badge-fill"></i></div>
                            <span class="badge badge-subtle-info">Customer Facing</span>
                        </div>
                        <h5 class="fw-bold mb-1 text-dark">Retail Banking</h5>
                        <p class="text-muted small mb-3">Audit customer dashboards, verify debit cards, and inspect self-service transfers.</p>
                        <div class="d-flex gap-3 text-muted small mb-4">
                            <span><strong class="text-dark">${totalCustomers}</strong> Customers</span>
                            <span>&bull;</span>
                            <span><strong class="text-dark">${totalAccounts}</strong> Accounts</span>
                        </div>
                    </div>
                    <a href="<c:url value='/customer/dashboard'/>" class="gm-console-btn btn-retail">
                        <span>Open Customer Console</span>
                        <i class="bi bi-arrow-right"></i>
                    </a>
                </div>

                <!-- 2. Back-Office Operations -->
                <div class="gm-console-card console-officer">
                    <div>
                        <div class="d-flex justify-content-between align-items-start mb-3">
                            <div class="gm-icon-box icon-emerald"><i class="bi bi-briefcase-fill"></i></div>
                            <span class="badge badge-subtle-success">Core Operations</span>
                        </div>
                        <h5 class="fw-bold mb-1 text-dark">Back-Office Operations</h5>
                        <p class="text-muted small mb-3">Account onboarding verification, teller postings, document scrutiny, and KYC.</p>
                        <div class="d-flex gap-3 text-muted small mb-4">
                            <span><strong class="text-dark">${pendingCount}</strong> Pending Accounts</span>
                            <span>&bull;</span>
                            <span><strong class="text-dark">${totalStaff}</strong> Staff Active</span>
                        </div>
                    </div>
                    <a href="<c:url value='/officer/dashboard'/>" class="gm-console-btn btn-officer">
                        <span>Open Officer Console</span>
                        <i class="bi bi-arrow-right"></i>
                    </a>
                </div>

                <!-- 3. Customer Relations & Support -->
                <div class="gm-console-card console-relations">
                    <div>
                        <div class="d-flex justify-content-between align-items-start mb-3">
                            <div class="gm-icon-box icon-amber"><i class="bi bi-chat-dots-fill"></i></div>
                            <span class="badge badge-subtle-warning">CRM & Support</span>
                        </div>
                        <h5 class="fw-bold mb-1 text-dark">Relations & Support</h5>
                        <p class="text-muted small mb-3">Support communications, feedback resolution, customer relationship logs, and tickets.</p>
                        <div class="d-flex gap-3 text-muted small mb-4">
                            <span><strong class="text-dark">${unreadCount != null ? unreadCount : 0}</strong> Unread Alerts</span>
                            <span>&bull;</span>
                            <span>CRE Desk Live</span>
                        </div>
                    </div>
                    <a href="<c:url value='/customer-relations/dashboard'/>" class="gm-console-btn btn-relations">
                        <span>Open Relations Console</span>
                        <i class="bi bi-arrow-right"></i>
                    </a>
                </div>

                <!-- 4. Security & Admin -->
                <div class="gm-console-card console-admin">
                    <div>
                        <div class="d-flex justify-content-between align-items-start mb-3">
                            <div class="gm-icon-box icon-rose"><i class="bi bi-shield-lock-fill"></i></div>
                            <span class="badge badge-subtle-danger">System Governance</span>
                        </div>
                        <h5 class="fw-bold mb-1 text-dark">Security & Admin</h5>
                        <p class="text-muted small mb-3">Role-based access control, user lockouts, system logs, security parameters, and maintenance.</p>
                        <div class="d-flex gap-3 text-muted small mb-4">
                            <span><strong class="text-dark">Zero</strong> Active Lockouts</span>
                            <span>&bull;</span>
                            <span>Audit Logging Active</span>
                        </div>
                    </div>
                    <a href="<c:url value='/admin/dashboard'/>" class="gm-console-btn btn-admin">
                        <span>Open Admin Console</span>
                        <i class="bi bi-arrow-right"></i>
                    </a>
                </div>

                <!-- 5. AML & Risk Compliance -->
                <div class="gm-console-card console-compliance">
                    <div>
                        <div class="d-flex justify-content-between align-items-start mb-3">
                            <div class="gm-icon-box icon-cyan"><i class="bi bi-shield-check"></i></div>
                            <span class="badge badge-subtle-info">Risk & Regulatory</span>
                        </div>
                        <h5 class="fw-bold mb-1 text-dark">AML & Risk Compliance</h5>
                        <p class="text-muted small mb-3">Anti-Money Laundering monitoring, transaction threshold screening, CTR / STR reports.</p>
                        <div class="d-flex gap-3 text-muted small mb-4">
                            <span>Threshold: <strong class="text-dark">Rs. 50,000</strong></span>
                            <span>&bull;</span>
                            <span>Audit Pass</span>
                        </div>
                    </div>
                    <a href="<c:url value='/compliance/dashboard'/>" class="gm-console-btn btn-compliance">
                        <span>Open Compliance Console</span>
                        <i class="bi bi-arrow-right"></i>
                    </a>
                </div>

                <!-- 6. Executive Approvals Desk -->
                <div class="gm-console-card console-approvals">
                    <div>
                        <div class="d-flex justify-content-between align-items-start mb-3">
                            <div class="gm-icon-box icon-purple"><i class="bi bi-check2-all"></i></div>
                            <span class="badge badge-subtle-warning">Action Hub</span>
                        </div>
                        <h5 class="fw-bold mb-1 text-dark">Executive Approvals Desk</h5>
                        <p class="text-muted small mb-3">Dual-signoff queue for sensitive account freezes, high-value transactions, and closures.</p>
                        <div class="d-flex gap-3 text-muted small mb-4">
                            <span><strong class="text-purple">${pendingApprovalsCount != null ? pendingApprovalsCount : 0}</strong> Maker-Checker Tasks</span>
                        </div>
                    </div>
                    <a href="<c:url value='/approvals'/>" class="gm-console-btn btn-approvals">
                        <span>Open Approvals Desk</span>
                        <i class="bi bi-arrow-right"></i>
                    </a>
                </div>
            </div>

            <!-- ============================= LIVE ACTIVITY & PENDING TASKS ============================= -->
            <div class="row g-4">
                <!-- RECENT TRANSACTIONS STREAM -->
                <div class="col-lg-7">
                    <div class="gm-chart-card">
                        <div class="gm-card-header">
                            <div>
                                <h5 class="gm-card-title"><i class="bi bi-activity text-primary"></i> Real-Time Bank Transaction Stream</h5>
                                <small class="text-muted">Live journal of transfers, card payments and branch postings</small>
                            </div>
                            <a href="<c:url value='/history'/>" class="btn btn-sm btn-outline-secondary">View Full Ledger</a>
                        </div>
                        <div class="table-responsive">
                            <table class="table gm-table align-middle">
                                <thead>
                                    <tr>
                                        <th>Ref / ID</th>
                                        <th>Type</th>
                                        <th>Account</th>
                                        <th>Amount (LKR)</th>
                                        <th>Timestamp</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <c:choose>
                                        <c:when test="${not empty recentTransactions}">
                                            <c:forEach var="tx" items="${recentTransactions}">
                                                <tr>
                                                    <td>
                                                        <span class="fw-semibold text-dark font-monospace" style="font-size: 0.8rem;">
                                                            <c:out value="${tx.referenceNumber != null ? tx.referenceNumber : ('TXN-' + tx.id)}"/>
                                                        </span>
                                                    </td>
                                                    <td>
                                                        <c:choose>
                                                            <c:when test="${tx.type == 'DEPOSIT'}"><span class="badge badge-subtle-success">DEPOSIT</span></c:when>
                                                            <c:when test="${tx.type == 'WITHDRAWAL'}"><span class="badge badge-subtle-danger">WITHDRAWAL</span></c:when>
                                                            <c:when test="${tx.type == 'TRANSFER'}"><span class="badge badge-subtle-info">TRANSFER</span></c:when>
                                                            <c:otherwise><span class="badge bg-secondary">${tx.type}</span></c:otherwise>
                                                        </c:choose>
                                                    </td>
                                                    <td class="text-muted small">
                                                        <c:out value="${tx.account != null ? tx.account.accountNumber : 'N/A'}"/>
                                                    </td>
                                                    <td class="fw-bold ${tx.type == 'DEPOSIT' ? 'text-success' : 'text-dark'}">
                                                        ${tx.type == 'DEPOSIT' ? '+' : '-'} Rs. <fmt:formatNumber value="${tx.amount}" minFractionDigits="2" maxFractionDigits="2"/>
                                                    </td>
                                                    <td class="text-muted small">
                                                        ${tx.createdAt != null ? tx.createdAt.toLocalDate() : 'Today'}
                                                    </td>
                                                </tr>
                                            </c:forEach>
                                        </c:when>
                                        <c:otherwise>
                                            <tr>
                                                <td colspan="5" class="text-center py-4 text-muted">
                                                    <i class="bi bi-inbox d-block mb-1 text-secondary" style="font-size: 1.5rem;"></i>
                                                    No system transactions recorded yet.
                                                </td>
                                            </tr>
                                        </c:otherwise>
                                    </c:choose>
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>

                <!-- PENDING MANAGER AUTHORIZATIONS -->
                <div class="col-lg-5">
                    <div class="gm-chart-card">
                        <div class="gm-card-header">
                            <div>
                                <h5 class="gm-card-title"><i class="bi bi-exclamation-circle text-warning"></i> Action Required: Pending Sign-Offs</h5>
                                <small class="text-muted">High priority approvals needing Branch Manager authorization</small>
                            </div>
                            <span class="badge bg-danger rounded-pill">${pendingCount + pendingLoans} Urgent</span>
                        </div>

                        <c:choose>
                            <c:when test="${pendingCount > 0 || pendingLoans > 0}">
                                <div class="list-group list-group-flush">
                                    <c:forEach var="acc" items="${pendingAccountsList}">
                                        <div class="list-group-item px-0 py-3 d-flex justify-content-between align-items-center">
                                            <div class="d-flex align-items-center gap-3">
                                                <div class="gm-icon-box icon-amber"><i class="bi bi-person-check"></i></div>
                                                <div>
                                                    <div class="fw-semibold text-dark">New Account: <c:out value="${acc.accountNumber}"/></div>
                                                    <small class="text-muted"><c:out value="${acc.customer != null ? acc.customer.fullName : 'Customer'}"/> &bull; ${acc.accountType}</small>
                                                </div>
                                            </div>
                                            <a href="<c:url value='/approvals'/>" class="btn btn-sm btn-outline-primary px-3">Review</a>
                                        </div>
                                    </c:forEach>

                                    <c:forEach var="ln" items="${pendingLoansList}">
                                        <div class="list-group-item px-0 py-3 d-flex justify-content-between align-items-center">
                                            <div class="d-flex align-items-center gap-3">
                                                <div class="gm-icon-box icon-purple"><i class="bi bi-cash-coin"></i></div>
                                                <div>
                                                    <div class="fw-semibold text-dark">Loan Sanction: Rs. <fmt:formatNumber value="${ln.requestedAmount}" minFractionDigits="0"/></div>
                                                    <small class="text-muted">${ln.loanType} &bull; Status: ${ln.status}</small>
                                                </div>
                                            </div>
                                            <a href="<c:url value='/loans/review'/>" class="btn btn-sm btn-outline-primary px-3">Sanction</a>
                                        </div>
                                    </c:forEach>
                                </div>
                            </c:when>
                            <c:otherwise>
                                <div class="text-center py-5">
                                    <div class="gm-icon-box icon-emerald mx-auto mb-3" style="width: 58px; height: 58px; font-size: 1.8rem;">
                                        <i class="bi bi-check2-all"></i>
                                    </div>
                                    <h6 class="fw-bold text-dark mb-1">Queue Clear</h6>
                                    <p class="text-muted small mb-0">All accounts, loan requests, and administrative operations are up to date.</p>
                                </div>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>
            </div>

        </div><!-- /container-fluid -->

        <!-- FOOTER -->
        <footer class="text-center py-4 text-muted small border-top mt-4 bg-white">
            <span>&copy; 2026 SmartBank Connect. Executive Banking Platform. Authorized Personnel Only.</span>
        </footer>
    </main>
</div>

<!-- ============================= CHART.JS SCRIPTS ============================= -->
<script src="https://cdn.jsdelivr.net/npm/chart.js@4.4.4/dist/chart.umd.min.js"></script>
<script>
    document.addEventListener("DOMContentLoaded", function() {
        // Theme Colors
        var palette = {
            primary: '#1814F3',
            primaryLight: 'rgba(24, 20, 243, 0.08)',
            emerald: '#10B981',
            emeraldLight: 'rgba(16, 185, 129, 0.12)',
            amber: '#F59E0B',
            rose: '#EF4444',
            cyan: '#06B6D4',
            purple: '#8B5CF6',
            grid: '#F1F5F9',
            text: '#64748B'
        };

        Chart.defaults.font.family = "'Inter', sans-serif";
        Chart.defaults.color = palette.text;

        // Data series from server with safe fallbacks
        var dailyLabels = ${dailyTxnLabels != null ? dailyTxnLabels : "['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun']"};
        var dailyCounts = ${dailyTxnCounts != null ? dailyTxnCounts : "[4, 8, 5, 9, 14, 11, 15]"};
        var dailyVolumes = ${dailyTxnVolumes != null ? dailyTxnVolumes : "[45000, 82000, 50000, 95000, 140000, 110000, 150000]"};

        var monthLabels = ${monthLabels != null ? monthLabels : "['Apr 2026', 'May 2026', 'Jun 2026', 'Jul 2026', 'Aug 2026', 'Sep 2026']"};
        var monthlyDeposits = ${monthlyDepositData != null ? monthlyDepositData : "[120000, 180000, 220000, 190000, 240000, 280000]"};
        var monthlyWithdrawals = ${monthlyWithdrawalData != null ? monthlyWithdrawalData : "[60000, 90000, 110000, 95000, 120000, 130000]"};

        var accountDist = ${accountTypeDistribution != null ? accountTypeDistribution : "[1, 0, 0]"};
        if (accountDist.reduce((a, b) => a + b, 0) === 0) {
            accountDist = [1, 0, 0];
        }

        var loanDist = ${loanStatusDistribution != null ? loanStatusDistribution : "[1, 0, 0]"};
        if (loanDist.reduce((a, b) => a + b, 0) === 0) {
            loanDist = [1, 0, 0];
        }

        // ======================== 1. DAILY VELOCITY CHART ========================
        var ctxVelocity = document.getElementById('dailyVelocityChart').getContext('2d');
        var velocityChart = new Chart(ctxVelocity, {
            type: 'line',
            data: {
                labels: dailyLabels,
                datasets: [{
                    label: 'Transaction Count',
                    data: dailyCounts,
                    borderColor: palette.primary,
                    backgroundColor: palette.primaryLight,
                    borderWidth: 2.8,
                    tension: 0.35,
                    fill: true,
                    pointBackgroundColor: '#FFFFFF',
                    pointBorderColor: palette.primary,
                    pointBorderWidth: 2,
                    pointRadius: 4,
                    pointHoverRadius: 6
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: { display: false },
                    tooltip: {
                        backgroundColor: '#0F172A',
                        padding: 10,
                        titleFont: { size: 12, weight: 'bold' },
                        bodyFont: { size: 13 }
                    }
                },
                scales: {
                    y: {
                        beginAtZero: true,
                        grid: { color: palette.grid, drawBorder: false },
                        ticks: { precision: 0 }
                    },
                    x: {
                        grid: { display: false, drawBorder: false }
                    }
                }
            }
        });

        window.toggleDailyMode = function(mode) {
            document.getElementById('btnModeCount').classList.toggle('active', mode === 'count');
            document.getElementById('btnModeVolume').classList.toggle('active', mode === 'volume');

            if (mode === 'volume') {
                velocityChart.data.datasets[0].label = 'Transaction Volume (LKR)';
                velocityChart.data.datasets[0].data = dailyVolumes;
                velocityChart.data.datasets[0].borderColor = palette.purple;
                velocityChart.data.datasets[0].backgroundColor = 'rgba(139, 92, 246, 0.08)';
                velocityChart.options.scales.y.ticks.callback = function(val) {
                    return 'Rs. ' + (val >= 1000 ? (val / 1000).toFixed(0) + 'k' : val);
                };
            } else {
                velocityChart.data.datasets[0].label = 'Transaction Count';
                velocityChart.data.datasets[0].data = dailyCounts;
                velocityChart.data.datasets[0].borderColor = palette.primary;
                velocityChart.data.datasets[0].backgroundColor = palette.primaryLight;
                velocityChart.options.scales.y.ticks.callback = function(val) { return val; };
            }
            velocityChart.update();
        };

        // ======================== 2. ACCOUNT PORTFOLIO DONUT ========================
        var ctxDonut = document.getElementById('accountDonutChart').getContext('2d');
        new Chart(ctxDonut, {
            type: 'doughnut',
            data: {
                labels: ['Savings Account', 'Current Account', 'Fixed Deposit'],
                datasets: [{
                    data: accountDist,
                    backgroundColor: [palette.primary, palette.emerald, palette.amber],
                    borderWidth: 3,
                    borderColor: '#FFFFFF',
                    hoverOffset: 4
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                cutout: '72%',
                plugins: {
                    legend: { display: false },
                    tooltip: {
                        backgroundColor: '#0F172A',
                        padding: 10
                    }
                }
            }
        });

        // ======================== 3. LIQUIDITY FLOW BAR CHART ========================
        var ctxLiquidity = document.getElementById('liquidityFlowChart').getContext('2d');
        new Chart(ctxLiquidity, {
            type: 'bar',
            data: {
                labels: monthLabels,
                datasets: [
                    {
                        label: 'Deposits (Inflows)',
                        data: monthlyDeposits,
                        backgroundColor: palette.emerald,
                        borderRadius: 6,
                        barPercentage: 0.6,
                        categoryPercentage: 0.8
                    },
                    {
                        label: 'Withdrawals (Outflows)',
                        data: monthlyWithdrawals,
                        backgroundColor: palette.rose,
                        borderRadius: 6,
                        barPercentage: 0.6,
                        categoryPercentage: 0.8
                    }
                ]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: {
                        position: 'top',
                        align: 'end',
                        labels: { boxWidth: 12, usePointStyle: true, pointStyle: 'circle' }
                    },
                    tooltip: {
                        callbacks: {
                            label: function(c) {
                                return c.dataset.label + ': Rs. ' + Number(c.raw).toLocaleString();
                            }
                        }
                    }
                },
                scales: {
                    y: {
                        beginAtZero: true,
                        grid: { color: palette.grid, drawBorder: false },
                        ticks: {
                            callback: function(val) {
                                return 'Rs. ' + (val >= 1000 ? (val / 1000).toFixed(0) + 'k' : val);
                            }
                        }
                    },
                    x: {
                        grid: { display: false, drawBorder: false }
                    }
                }
            }
        });

        // ======================== 4. LOAN STATUS DOUGHNUT ========================
        var ctxLoans = document.getElementById('loanStatusChart').getContext('2d');
        new Chart(ctxLoans, {
            type: 'doughnut',
            data: {
                labels: ['Approved', 'Pending Underwriting', 'Rejected'],
                datasets: [{
                    data: loanDist,
                    backgroundColor: [palette.emerald, palette.amber, palette.rose],
                    borderWidth: 3,
                    borderColor: '#FFFFFF'
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                cutout: '68%',
                plugins: {
                    legend: { display: false }
                }
            }
        });
    });
</script>
</body>
</html>
