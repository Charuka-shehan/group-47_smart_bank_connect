<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <title>My Loans | SmartBank Connect</title>
    <jsp:include page="../common/app-head.jsp"/>
</head>
<body>
<div class="sb-shell">
    <jsp:include page="../common/sidebar.jsp"/>
    <div class="sb-main">
        <div class="sb-topbar">
            <h5 class="mb-0">My Loan Applications</h5>
            <a href="<c:url value='/loans/apply'/>" class="btn btn-gold btn-sm">+ New Application</a>
        </div>
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
                        <thead><tr><th>Application No.</th><th>Type</th><th>Amount (Rs.)</th><th>Status</th><th>Remarks</th><th class="text-end">Actions</th></tr></thead>
                        <tbody>
                        <c:forEach var="l" items="${loans}">
                            <tr>
                                <td>${l.applicationNumber}</td>
                                <td>${l.loanType}</td>
                                <td><fmt:formatNumber value="${l.requestedAmount}" maxFractionDigits="2"/></td>
                                <td><span class="sb-badge sb-badge-${l.status.toString().toLowerCase()}">${l.status}</span></td>
                                <td>${l.remarks}</td>
                                <td class="text-end">
                                    <c:if test="${l.status == 'SUBMITTED'}">
                                        <button type="button" class="btn btn-sm btn-outline-primary me-1" data-bs-toggle="modal" data-bs-target="#editModal${l.id}">
                                            <i class="bi bi-pencil me-1"></i>Edit
                                        </button>
                                        <form method="post" action="<c:url value='/loans/${l.id}/cancel'/>" class="d-inline"
                                              onsubmit="return confirm('Are you sure you want to cancel this loan application?');">
                                            <c:if test="${not empty _csrf}"><input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" /></c:if>
                                            <button type="submit" class="btn btn-sm btn-outline-danger">Cancel</button>
                                        </form>

                                        <!-- Edit Modal -->
                                        <div class="modal fade text-start" id="editModal${l.id}" tabindex="-1" aria-hidden="true">
                                            <div class="modal-dialog">
                                                <div class="modal-content">
                                                    <form method="post" action="<c:url value='/loans/${l.id}/update'/>">
                                                        <c:if test="${not empty _csrf}"><input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" /></c:if>
                                                        <div class="modal-header">
                                                            <h6 class="modal-title"><i class="bi bi-pencil me-2 text-primary"></i>Edit Application: <c:out value="${l.applicationNumber}"/></h6>
                                                            <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                                                        </div>
                                                        <div class="modal-body">
                                                            <div class="mb-3">
                                                                <label class="form-label small fw-semibold">Loan Type <span class="text-danger">*</span></label>
                                                                <select name="loanType" class="form-select" required>
                                                                    <option value="PERSONAL" ${l.loanType == 'PERSONAL' ? 'selected' : ''}>Personal Loan</option>
                                                                    <option value="HOME" ${l.loanType == 'HOME' ? 'selected' : ''}>Home Loan</option>
                                                                    <option value="VEHICLE" ${l.loanType == 'VEHICLE' ? 'selected' : ''}>Vehicle Loan</option>
                                                                    <option value="EDUCATION" ${l.loanType == 'EDUCATION' ? 'selected' : ''}>Education Loan</option>
                                                                </select>
                                                            </div>
                                                            <div class="mb-3">
                                                                <label class="form-label small fw-semibold">Requested Amount (LKR) <span class="text-danger">*</span></label>
                                                                <input type="number" step="0.01" min="10000" class="form-control" name="amount" value="${l.requestedAmount}" required>
                                                            </div>
                                                            <div class="mb-3">
                                                                <label class="form-label small fw-semibold">Monthly Income (LKR) <span class="text-danger">*</span></label>
                                                                <input type="number" step="0.01" min="10000" class="form-control" name="monthlyIncome" value="${l.monthlyIncome}" required>
                                                            </div>
                                                            <div class="mb-3">
                                                                <label class="form-label small fw-semibold">Employment Status <span class="text-danger">*</span></label>
                                                                <select name="employmentStatus" class="form-select" required>
                                                                    <option value="Salaried - Private Sector" ${l.employmentStatus == 'Salaried - Private Sector' ? 'selected' : ''}>Salaried - Private Sector</option>
                                                                    <option value="Salaried - Government" ${l.employmentStatus == 'Salaried - Government' ? 'selected' : ''}>Salaried - Government</option>
                                                                    <option value="Self-Employed / Business" ${l.employmentStatus == 'Self-Employed / Business' ? 'selected' : ''}>Self-Employed / Business</option>
                                                                    <option value="Professional" ${l.employmentStatus == 'Professional' ? 'selected' : ''}>Professional</option>
                                                                    <option value="Other" ${l.employmentStatus == 'Other' ? 'selected' : ''}>Other</option>
                                                                </select>
                                                            </div>
                                                            <div class="mb-3">
                                                                <label class="form-label small fw-semibold">Documents &amp; Purpose Notes</label>
                                                                <textarea name="documentsSummary" class="form-control" rows="2"><c:out value="${l.documentsSummary}"/></textarea>
                                                            </div>
                                                        </div>
                                                        <div class="modal-footer">
                                                            <button type="button" class="btn btn-sm btn-secondary" data-bs-dismiss="modal">Cancel</button>
                                                            <button type="submit" class="btn btn-sm btn-primary"><i class="bi bi-check-circle me-1"></i>Save Changes</button>
                                                        </div>
                                                    </form>
                                                </div>
                                            </div>
                                        </div>
                                    </c:if>
                                </td>
                            </tr>
                        </c:forEach>
                        <c:if test="${empty loans}"><tr><td colspan="6" class="text-muted text-center py-3">No applications yet.</td></tr></c:if>
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
