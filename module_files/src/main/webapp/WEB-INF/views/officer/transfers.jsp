<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<c:set var="pageTitle" value="Transfer Verification" scope="request"/>
<jsp:include page="../common/app-frame-start.jsp"/>

<c:if test="${not empty success}"><div class="alert alert-success py-2 mb-3">${success}</div></c:if>
<c:if test="${not empty error}"><div class="alert alert-danger py-2 mb-3">${error}</div></c:if>

<div class="sb-card mb-4">
    <h6 class="mb-3">Pending transfers</h6>
    <div class="table-responsive">
        <table class="table align-middle">
            <thead>
                <tr>
                    <th>Reference</th>
                    <th>Source Account</th>
                    <th>Destination / Beneficiary</th>
                    <th>Amount</th>
                    <th>Status</th>
                    <th class="text-end">Actions</th>
                </tr>
            </thead>
            <tbody>
            <c:forEach var="t" items="${pending}">
                <tr>
                    <td><span class="fw-semibold"><c:out value="${t.referenceNumber}"/></span></td>
                    <td><c:out value="${t.sourceAccount != null ? t.sourceAccount.accountNumber : '-'}"/></td>
                    <td>
                        <c:choose>
                            <c:when test="${t.destinationAccount != null}"><c:out value="${t.destinationAccount.accountNumber}"/></c:when>
                            <c:when test="${not empty t.beneficiaryName}"><c:out value="${t.beneficiaryName}"/></c:when>
                            <c:otherwise>-</c:otherwise>
                        </c:choose>
                    </td>
                    <td>LKR <fmt:formatNumber value="${t.amount}" maxFractionDigits="2"/></td>
                    <td><span class="badge bg-warning text-dark"><c:out value="${t.status}"/></span></td>
                    <td class="text-end">
                        <div class="d-inline-flex gap-1">
                            <sec:authorize access="hasAnyRole('BANK_MANAGER','SYSTEM_ADMINISTRATOR')">
                                <form action="<c:url value='/officer/transfers/${t.id}/approve'/>" method="post" class="d-inline">
                                    <c:if test="${not empty _csrf}"><input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" /></c:if>
                                    <button type="submit" class="btn btn-sm btn-success"><i class="bi bi-check-circle me-1"></i>Approve</button>
                                </form>
                            </sec:authorize>
                            <button type="button" class="btn btn-sm btn-outline-primary" data-bs-toggle="modal" data-bs-target="#editTxnModal${t.id}">
                                <i class="bi bi-pencil me-1"></i>Update
                            </button>
                            <form action="<c:url value='/officer/transfers/${t.id}/cancel'/>" method="post" class="d-inline"
                                  onsubmit="return confirm('Cancel this pending transfer?');">
                                <c:if test="${not empty _csrf}"><input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" /></c:if>
                                <button type="submit" class="btn btn-sm btn-outline-danger">Cancel</button>
                            </form>
                        </div>

                        <!-- Update Pending Transfer Modal -->
                        <div class="modal fade" id="editTxnModal${t.id}" tabindex="-1" aria-hidden="true">
                            <div class="modal-dialog">
                                <div class="modal-content text-start">
                                    <div class="modal-header">
                                        <h6 class="modal-title">Update Transfer: ${t.referenceNumber}</h6>
                                        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                                    </div>
                                    <form action="<c:url value='/officer/transfers/${t.id}/update'/>" method="post">
                                        <c:if test="${not empty _csrf}"><input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" /></c:if>
                                        <div class="modal-body">
                                            <div class="mb-3">
                                                <label class="form-label">Transfer Amount (LKR)</label>
                                                <input type="number" step="0.01" name="amount" class="form-control" value="${t.amount}" required>
                                            </div>
                                            <div class="mb-3">
                                                <label class="form-label">Remarks / Correction Note</label>
                                                <textarea name="remarks" class="form-control" rows="2">${t.remarks}</textarea>
                                            </div>
                                        </div>
                                        <div class="modal-footer">
                                            <button type="button" class="btn btn-secondary btn-sm" data-bs-dismiss="modal">Close</button>
                                            <button type="submit" class="btn btn-primary btn-sm">Save Changes</button>
                                        </div>
                                    </form>
                                </div>
                            </div>
                        </div>
                    </td>
                </tr>
            </c:forEach>
            <c:if test="${empty pending}"><tr><td colspan="6" class="text-muted text-center py-3">No pending transfers.</td></tr></c:if>
            </tbody>
        </table>
    </div>
</div>

<div class="sb-card">
    <h6 class="mb-3">Recent transactions</h6>
    <table class="table">
        <thead><tr><th>Reference</th><th>Type</th><th>Amount</th><th>Status</th></tr></thead>
        <tbody>
        <c:forEach var="t" items="${recent}">
            <tr>
                <td><c:out value="${t.referenceNumber}"/></td>
                <td><c:out value="${t.type}"/></td>
                <td>LKR <fmt:formatNumber value="${t.amount}" maxFractionDigits="2"/></td>
                <td><c:out value="${t.status}"/></td>
            </tr>
        </c:forEach>
        </tbody>
    </table>
</div>

<jsp:include page="../common/app-frame-end.jsp"/>
