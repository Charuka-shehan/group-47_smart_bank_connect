<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <title>Manage Accounts | SmartBank Connect</title>
    <jsp:include page="../common/app-head.jsp"/>
</head>
<body>
<div class="sb-shell">
    <jsp:include page="../common/sidebar.jsp"/>
    <div class="sb-main">
        <div class="sb-topbar"><h5 class="mb-0">Account Management</h5></div>
        <div class="sb-content">
            <c:if test="${not empty success}"><div class="alert alert-success">${success}</div></c:if>
            <c:if test="${not empty error}"><div class="alert alert-danger">${error}</div></c:if>

            <sec:authorize access="hasAnyRole('BANK_OFFICER','SYSTEM_ADMINISTRATOR')">
            <div class="sb-card mb-4">
                <div class="d-flex justify-content-between align-items-center mb-3">
                    <h6 class="mb-0">Account Requests</h6>
                    <a href="<c:url value='/accounts/open'/>" class="btn btn-primary btn-sm">
                        <i class="bi bi-folder-plus"></i> Open New Account
                    </a>
                </div>
                <form action="<c:url value='/accounts/manage/create'/>" method="post" class="row g-2">
                    <c:if test="${not empty _csrf}"><input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" /></c:if>
                    <div class="col-md-5">
                        <select name="customerId" class="form-select" required>
                            <option value="">Select existing customer...</option>
                            <c:forEach var="cust" items="${customers}">
                                <option value="${cust.id}">${cust.fullName} (${cust.email})</option>
                            </c:forEach>
                        </select>
                    </div>
                    <div class="col-md-4">
                        <select name="accountType" class="form-select" required>
                            <option value="">Account type...</option>
                            <option value="SAVINGS">Savings</option>
                            <option value="CURRENT">Current</option>
                            <option value="FIXED_DEPOSIT">Fixed Deposit</option>
                        </select>
                    </div>
                    <div class="col-md-3">
                        <button type="submit" class="btn btn-gold w-100">Submit Request</button>
                    </div>
                </form>
            </div>
            </sec:authorize>

            <div class="sb-card">
                <h6 class="mb-3">All Accounts</h6>
                <div class="table-responsive">
                    <table class="table align-middle">
                        <thead><tr><th>Account No.</th><th>Customer</th><th>Type</th><th>Balance (Rs.)</th><th>Status</th><th>Actions</th></tr></thead>
                        <tbody>
                        <c:forEach var="a" items="${accounts}">
                            <tr>
                                <td>${a.accountNumber}</td>
                                <td>${a.customer.fullName}</td>
                                <td>${a.accountType}</td>
                                <td><fmt:formatNumber value="${a.balance}" maxFractionDigits="2"/></td>
                                <td><span class="sb-badge sb-badge-${a.status.toString().toLowerCase()}">${a.status}</span></td>
                                <td class="d-flex gap-1 flex-wrap">
                                    <sec:authorize access="hasAnyRole('BANK_MANAGER','SYSTEM_ADMINISTRATOR')">
                                        <c:if test="${a.status == 'PENDING_APPROVAL'}">
                                            <form action="<c:url value='/accounts/manage/${a.id}/approve'/>" method="post" class="d-inline">
                                                <c:if test="${not empty _csrf}"><input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" /></c:if>
                                                <button class="btn btn-sm btn-success">Approve</button>
                                            </form>
                                            <form action="<c:url value='/accounts/manage/${a.id}/reject'/>" method="post" class="d-inline"
                                                  onsubmit="var r = prompt('Enter rejection reason:'); if (!r) return false; this.querySelector('input[name=reason]').value = r;">
                                                <c:if test="${not empty _csrf}"><input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" /></c:if>
                                                <input type="hidden" name="reason" value="">
                                                <button type="submit" class="btn btn-sm btn-outline-danger">Reject</button>
                                            </form>
                                        </c:if>
                                    </sec:authorize>
                                    <sec:authorize access="hasAnyRole('BANK_OFFICER','BANK_MANAGER','SYSTEM_ADMINISTRATOR')">
                                        <button type="button" class="btn btn-sm btn-outline-primary" data-bs-toggle="modal" data-bs-target="#editModal${a.id}">
                                            <i class="bi bi-pencil"></i> Edit
                                        </button>
                                    </sec:authorize>
                                    <c:if test="${a.status == 'ACTIVE'}">
                                        <form action="<c:url value='/accounts/manage/${a.id}/freeze'/>" method="post">
                                                <c:if test="${not empty _csrf}"><input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" /></c:if>
                                            <button class="btn btn-sm btn-outline-warning">Freeze</button>
                                        </form>
                                        <sec:authorize access="hasRole('BANK_OFFICER')">
                                        <form action="<c:url value='/accounts/manage/${a.id}/request-close'/>" method="post">
                                                <c:if test="${not empty _csrf}"><input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" /></c:if>
                                            <button class="btn btn-sm btn-outline-secondary">Request Close</button>
                                        </form>
                                        </sec:authorize>
                                    </c:if>
                                    <c:if test="${a.status == 'FROZEN'}">
                                        <form action="<c:url value='/accounts/manage/${a.id}/unfreeze'/>" method="post">
                                                <c:if test="${not empty _csrf}"><input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" /></c:if>
                                            <button class="btn btn-sm btn-outline-success">Unfreeze</button>
                                        </form>
                                    </c:if>
                                    <sec:authorize access="hasAnyRole('BANK_MANAGER','SYSTEM_ADMINISTRATOR')">
                                        <c:if test="${a.status != 'CLOSED'}">
                                            <form action="<c:url value='/accounts/manage/${a.id}/close'/>" method="post"
                                                  onsubmit="return confirm('Close this account?');">
                                                <c:if test="${not empty _csrf}"><input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" /></c:if>
                                                <button class="btn btn-sm btn-outline-warning">Close</button>
                                            </form>
                                        </c:if>
                                        <form action="<c:url value='/accounts/manage/${a.id}/delete'/>" method="post"
                                              onsubmit="return confirm('Permanently delete account ${a.accountNumber}? This will remove the account and all related records from the database. This action cannot be undone.');">
                                            <c:if test="${not empty _csrf}"><input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" /></c:if>
                                            <button type="submit" class="btn btn-sm btn-danger" title="Permanently Delete Account">
                                                <i class="bi bi-trash"></i> Delete
                                            </button>
                                        </form>
                                    </sec:authorize>

                                    <!-- Edit Account Modal -->
                                    <div class="modal fade" id="editModal${a.id}" tabindex="-1" aria-labelledby="editModalLabel${a.id}" aria-hidden="true">
                                        <div class="modal-dialog">
                                            <div class="modal-content">
                                                <div class="modal-header">
                                                    <h6 class="modal-title" id="editModalLabel${a.id}">Request Update: ${a.accountNumber}</h6>
                                                    <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                                                </div>
                                                <form action="<c:url value='/accounts/manage/${a.id}/request-update'/>" method="post">
                                                    <c:if test="${not empty _csrf}"><input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" /></c:if>
                                                    <div class="modal-body text-start">
                                                        <div class="mb-3">
                                                            <label class="form-label">Account Type</label>
                                                            <select name="accountType" class="form-select">
                                                                <option value="SAVINGS" ${a.accountType == 'SAVINGS' ? 'selected' : ''}>Savings</option>
                                                                <option value="CURRENT" ${a.accountType == 'CURRENT' ? 'selected' : ''}>Current</option>
                                                                <option value="FIXED_DEPOSIT" ${a.accountType == 'FIXED_DEPOSIT' ? 'selected' : ''}>Fixed Deposit</option>
                                                            </select>
                                                        </div>
                                                        <div class="mb-3">
                                                            <label class="form-label">Branch</label>
                                                            <select name="branchId" class="form-select">
                                                                <option value="">-- Keep Current --</option>
                                                                <c:forEach var="b" items="${branches}">
                                                                    <option value="${b.id}" ${a.branch != null && a.branch.id == b.id ? 'selected' : ''}>${b.name}</option>
                                                                </c:forEach>
                                                            </select>
                                                        </div>
                                                        <div class="mb-3">
                                                            <label class="form-label">Occupation</label>
                                                            <input type="text" name="occupation" class="form-control" value="${a.occupation}">
                                                        </div>
                                                        <div class="mb-3">
                                                            <label class="form-label">Monthly Income (Rs.)</label>
                                                            <input type="number" step="0.01" name="monthlyIncome" class="form-control" value="${a.monthlyIncome}">
                                                        </div>
                                                        <div class="mb-3">
                                                            <label class="form-label">Nominee Name</label>
                                                            <input type="text" name="nomineeName" class="form-control" value="${a.nomineeName}">
                                                        </div>
                                                        <div class="mb-3">
                                                            <label class="form-label">Nominee Relationship</label>
                                                            <input type="text" name="nomineeRelationship" class="form-control" value="${a.nomineeRelationship}">
                                                        </div>
                                                        <div class="alert alert-info py-2 small mb-0">
                                                            <i class="bi bi-info-circle me-1"></i> Dual Authorization: Changes will be submitted for Manager approval before being applied to the database.
                                                        </div>
                                                    </div>
                                                    <div class="modal-footer">
                                                        <button type="button" class="btn btn-secondary btn-sm" data-bs-dismiss="modal">Cancel</button>
                                                        <button type="submit" class="btn btn-primary btn-sm">Submit Update Request</button>
                                                    </div>
                                                </form>
                                            </div>
                                        </div>
                                    </div>
                                </td>
                            </tr>
                        </c:forEach>
                        <c:if test="${empty accounts}"><tr><td colspan="6" class="text-muted text-center py-3">No accounts in the system yet.</td></tr></c:if>
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
