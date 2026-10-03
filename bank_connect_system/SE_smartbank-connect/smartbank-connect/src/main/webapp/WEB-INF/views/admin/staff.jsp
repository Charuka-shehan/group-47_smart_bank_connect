<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <title>Staff Management | SmartBank Connect</title>
    <jsp:include page="../common/app-head.jsp"/>
</head>
<body>
<div class="sb-shell">
    <jsp:include page="../common/sidebar.jsp"/>
    <div class="sb-main">
        <div class="sb-topbar"><h5 class="mb-0">Staff Management</h5></div>
        <div class="sb-content">
            <div class="sb-card" style="max-width: 520px;">
                <h6 class="mb-3">Create Staff Account</h6>
                <c:if test="${not empty error}"><div class="alert alert-danger py-2">${error}</div></c:if>
                <c:if test="${not empty success}"><div class="alert alert-success py-2">${success}</div></c:if>
                <form action="<c:url value='/admin/staff/create'/>" method="post">
<c:if test="${not empty _csrf}"><input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" /></c:if>
                    <div class="mb-3">
                        <label class="form-label">Full Name</label>
                        <input type="text" name="fullName" class="form-control" required>
                    </div>
                    <div class="mb-3">
                        <label class="form-label">Email Address</label>
                        <input type="email" name="email" class="form-control" required>
                    </div>
                    <div class="mb-3">
                        <label class="form-label">Temporary Password</label>
                        <input type="password" name="password" minlength="6" class="form-control" required>
                    </div>
                    <div class="row">
                        <div class="col-6 mb-3">
                            <label class="form-label">NIC Number</label>
                            <input type="text" name="nic" class="form-control" required>
                        </div>
                        <div class="col-6 mb-3">
                            <label class="form-label">Employee ID</label>
                            <input type="text" name="employeeId" class="form-control" required>
                        </div>
                    </div>
                    <div class="row">
                        <div class="col-6 mb-3">
                            <label class="form-label">Phone Number</label>
                            <input type="text" name="phone" placeholder="0773117384" class="form-control" required>
                        </div>
                        <div class="col-6 mb-3">
                            <label class="form-label">Branch</label>
                            <input type="text" name="branch" class="form-control" required>
                        </div>
                    </div>
                    <div class="mb-3">
                        <label class="form-label">Role</label>
                        <select name="role" class="form-select" required>
                            <c:forEach var="r" items="${roles}">
                                <option value="${r}">${r}</option>
                            </c:forEach>
                        </select>
                    </div>
                    <button type="submit" class="btn btn-gold w-100 py-2">Create Staff Account</button>
                </form>
                <p class="form-text mt-2">Newly created staff can sign in immediately in this prototype. Production deployments can require manager approval.</p>
            </div>

            <sec:authorize access="hasRole('BANK_OFFICER')">
            <div class="sb-card mt-4" style="max-width: 520px;">
                <h6 class="mb-3">Request Staff Account (Manager approval required)</h6>
                <form action="<c:url value='/admin/staff/request-create'/>" method="post">
                    <c:if test="${not empty _csrf}"><input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" /></c:if>
                    <div class="mb-3"><label class="form-label">Full Name</label><input type="text" name="fullName" class="form-control" required></div>
                    <div class="mb-3"><label class="form-label">Email Address</label><input type="email" name="email" class="form-control" required></div>
                    <div class="mb-3"><label class="form-label">Temporary Password</label><input type="password" name="password" minlength="8" class="form-control" required></div>
                    <div class="row">
                        <div class="col-6 mb-3"><label class="form-label">NIC Number</label><input type="text" name="nic" class="form-control" required></div>
                        <div class="col-6 mb-3"><label class="form-label">Employee ID</label><input type="text" name="employeeId" class="form-control" required></div>
                    </div>
                    <div class="row">
                        <div class="col-6 mb-3"><label class="form-label">Phone Number</label><input type="text" name="phone" class="form-control" required></div>
                        <div class="col-6 mb-3"><label class="form-label">Branch</label><input type="text" name="branch" class="form-control" required></div>
                    </div>
                    <div class="mb-3">
                        <label class="form-label">Role</label>
                        <select name="role" class="form-select" required>
                            <c:forEach var="r" items="${roles}"><option value="${r}">${r}</option></c:forEach>
                        </select>
                    </div>
                    <button type="submit" class="btn btn-outline-primary w-100 py-2">Submit for manager approval</button>
                </form>
            </div>
            </sec:authorize>

            <div class="sb-card mt-4">
                <h6 class="mb-3">All users</h6>
                <div class="table-responsive">
                    <table class="table align-middle">
                        <thead><tr><th>Name</th><th>Email</th><th>Role</th><th>Status</th><th></th></tr></thead>
                        <tbody>
                        <c:forEach var="u" items="${users}">
                            <tr>
                                <td><c:out value="${u.fullName}"/></td>
                                <td><c:out value="${u.email}"/></td>
                                <td><c:out value="${u.role}"/></td>
                                <td><c:out value="${u.enabled ? 'ENABLED' : 'DISABLED'}"/></td>
                                <td class="d-flex gap-1 flex-wrap">
                                    <sec:authorize access="hasAnyRole('BANK_MANAGER','SYSTEM_ADMINISTRATOR')">
                                        <form method="post" action="<c:url value='/admin/users/${u.id}/toggle'/>" class="d-inline">
                                            <c:if test="${not empty _csrf}"><input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" /></c:if>
                                            <button class="btn btn-sm btn-outline-secondary">${u.enabled ? 'Disable' : 'Enable'}</button>
                                        </form>
                                    </sec:authorize>
                                    <form method="post" action="<c:url value='/admin/staff/${u.id}/request-role-update'/>" class="d-flex gap-1"
                                          onsubmit="var nr=prompt('New role (e.g. BANK_OFFICER, BANK_MANAGER):'); if(!nr) return false; this.querySelector('[name=newRole]').value=nr;">
                                        <c:if test="${not empty _csrf}"><input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" /></c:if>
                                        <input type="hidden" name="newRole" value="">
                                        <button class="btn btn-sm btn-outline-primary">Request role change</button>
                                    </form>
                                    <form method="post" action="<c:url value='/admin/staff/${u.id}/request-deletion'/>"
                                          onsubmit="var rsn=prompt('Reason for deletion request:'); if(!rsn) return false; this.querySelector('[name=reason]').value=rsn;">
                                        <c:if test="${not empty _csrf}"><input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" /></c:if>
                                        <input type="hidden" name="reason" value="">
                                        <button class="btn btn-sm btn-outline-danger">Request deletion</button>
                                    </form>
                                </td>
                            </tr>
                        </c:forEach>
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
