<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <title>Customer Relations Dashboard | SmartBank Connect</title>
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
            <li class="bd-nav-item active"><a href="<c:url value='/customer-relations/dashboard'/>"><i class="bi bi-house-door-fill"></i> Dashboard</a></li>
            <li class="bd-nav-item"><a href="<c:url value='/notifications'/>"><i class="bi bi-bell"></i> Support Notifications</a></li>
            <li class="bd-nav-item">
                <form action="<c:url value='/logout'/>" method="post" id="logoutForm" style="display:none;"><c:if test="${not empty _csrf}"><input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" /></c:if></form>
                <a href="javascript:document.getElementById('logoutForm').submit();"><i class="bi bi-box-arrow-right"></i> Log Out</a>
            </li>
        </ul>
    </aside>

    <!-- MAIN CONTENT -->
    <main class="bd-main">
        <div class="bd-topbar">
            <h1>Customer Relations Console</h1>
            <div class="bd-welcome">Role: <strong>${dashboardRole}</strong> | User: <strong><c:out value="${userDto.fullName}"/></strong></div>
        </div>

        <div class="container-fluid mt-4">
            <div class="row">
                <div class="col-md-8">
                    <div class="card p-4 mb-4 shadow-sm">
                        <h4 class="mb-3">Recent Customer Support Requests</h4>
                        <div class="alert alert-info py-2">All customer communication interfaces are active.</div>
                        <ul class="list-group list-group-flush">
                            <li class="list-group-item d-flex justify-content-between align-items-center">
                                <div>
                                    <strong>Statement Request</strong>
                                    <div class="small text-muted">Customer: Samantha Perera</div>
                                </div>
                                <span class="badge bg-secondary">Resolved</span>
                            </li>
                        </ul>
                    </div>
                </div>
                <div class="col-md-4">
                    <div class="card p-4 shadow-sm mb-4">
                        <h5>Customer Relations</h5>
                        <p class="text-muted small">Access read-only customer records for support validation.</p>
                        <button class="btn btn-primary btn-sm"><i class="bi bi-search"></i> Search Customer Records</button>
                    </div>
                </div>
            </div>
        </div>
    </main>
</div>
</body>
</html>
