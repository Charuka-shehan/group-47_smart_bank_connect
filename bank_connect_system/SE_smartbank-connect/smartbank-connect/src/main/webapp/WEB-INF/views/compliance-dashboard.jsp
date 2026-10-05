<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <title>Compliance Dashboard | SmartBank Connect</title>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css" rel="stylesheet">
    <link href="<c:url value='/css/bankdash.css'/>" rel="stylesheet">
</head>
<body>
<div class="bd-root">
    <!-- SIDEBAR -->
    <aside class="bd-sidebar">
        <div class="bd-sidebar-logo">
            <i class="bi bi-bank2 text-white" style="font-size: 1.5rem;"></i>
            <span class="ms-2">SmartBank</span>
        </div>
        <ul class="bd-nav">
            <li class="bd-nav-item active"><a href="<c:url value='/compliance/dashboard'/>"><i class="bi bi-house-door-fill"></i> Dashboard</a></li>
            <li class="bd-nav-item"><a href="<c:url value='/notifications'/>"><i class="bi bi-bell"></i> Notifications</a></li>
            <li class="bd-nav-item">
                <form action="<c:url value='/logout'/>" method="post" id="logoutForm" style="display:none;"><c:if test="${not empty _csrf}"><input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" /></c:if></form>
                <a href="javascript:document.getElementById('logoutForm').submit();"><i class="bi bi-box-arrow-right"></i> Log Out</a>
            </li>
        </ul>
    </aside>

    <!-- MAIN CONTENT -->
    <main class="bd-main">
        <div class="bd-topbar">
            <h1>Compliance & AML Console</h1>
            <div class="bd-welcome">Role: <strong>${dashboardRole}</strong> | User: <strong><c:out value="${userDto.fullName}"/></strong></div>
        </div>

        <div class="container-fluid mt-4">
            <div class="row">
                <div class="col-md-8">
                    <div class="card p-4 mb-4 shadow-sm">
                        <h4 class="mb-3">KYC / AML Review Queue</h4>
                        <div class="table-responsive">
                            <table class="table align-middle">
                                <thead>
                                    <tr>
                                        <th>Customer</th>
                                        <th>Risk level</th>
                                        <th>AML Status</th>
                                        <th>Actions</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <tr>
                                        <td>Charuka Shehan</td>
                                        <td><span class="badge bg-success">LOW</span></td>
                                        <td><span class="badge bg-info">VERIFIED</span></td>
                                        <td>
                                            <button class="btn btn-sm btn-outline-secondary">Details</button>
                                        </td>
                                    </tr>
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>
                <div class="col-md-4">
                    <div class="card p-4 shadow-sm mb-4">
                        <h5>Compliance Actions</h5>
                        <p class="text-muted small">Perform AML audits and view risk patterns.</p>
                        <div class="d-grid gap-2">
                            <button class="btn btn-sm btn-primary">Run KYC Verification</button>
                            <a href="<c:url value='/reports/audit-logs/download'/>" target="_blank" class="btn btn-sm btn-outline-secondary"><i class="bi bi-file-pdf"></i> Download Audit Report</a>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </main>
</div>
</body>
</html>
