<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="pageTitle" value="Transaction Corrections" scope="request"/>
<jsp:include page="../common/app-frame-start.jsp"/>

<c:if test="${not empty success}"><div class="alert alert-success py-2 mb-3">${success}</div></c:if>
<c:if test="${not empty error}"><div class="alert alert-danger py-2 mb-3">${error}</div></c:if>

<div class="row">
    <div class="col-lg-7">
        <div class="sb-card mb-4">
            <h6 class="mb-3"><i class="bi bi-pencil-square text-primary me-2"></i>Submit Correction Request</h6>
            <form method="post" action="<c:url value='/officer/corrections'/>" class="row g-3">
                <c:if test="${not empty _csrf}"><input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" /></c:if>
                <div class="col-md-6">
                    <label class="form-label">Transaction</label>
                    <select name="transactionId" class="form-select" required>
                        <c:forEach var="t" items="${transactions}">
                            <option value="${t.id}">${t.referenceNumber} — LKR ${t.amount} (${t.status})</option>
                        </c:forEach>
                    </select>
                </div>
                <div class="col-md-6">
                    <label class="form-label">Proposed amount (optional)</label>
                    <input class="form-control" name="proposedAmount" type="number" step="0.01">
                </div>
                <div class="col-12">
                    <label class="form-label">Proposed remarks</label>
                    <input class="form-control" name="proposedRemarks" placeholder="Corrected description or remarks">
                </div>
                <div class="col-12">
                    <label class="form-label">Reason for correction</label>
                    <textarea class="form-control" name="reason" rows="2" placeholder="Explain the clerical error or reason" required></textarea>
                </div>
                <div class="col-12"><button class="btn btn-primary btn-sm"><i class="bi bi-send me-1"></i>Submit for Manager Approval</button></div>
            </form>
        </div>
    </div>
    <div class="col-lg-5">
        <div class="sb-card mb-4">
            <h6 class="mb-3 text-danger"><i class="bi bi-trash text-danger me-2"></i>Request Transaction Deletion</h6>
            <p class="text-muted small">Request voiding or deletion of an erroneous entry. Requires Manager dual-authorization approval.</p>
            <form method="post" action="<c:url value='/officer/corrections/request-delete'/>" class="row g-3">
                <c:if test="${not empty _csrf}"><input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" /></c:if>
                <div class="col-12">
                    <label class="form-label">Select Erroneous Transaction</label>
                    <select name="transactionId" class="form-select" required>
                        <c:forEach var="t" items="${transactions}">
                            <option value="${t.id}">${t.referenceNumber} — LKR ${t.amount}</option>
                        </c:forEach>
                    </select>
                </div>
                <div class="col-12">
                    <label class="form-label">Reason for Deletion / Reversal</label>
                    <textarea class="form-control" name="reason" rows="2" placeholder="e.g. Duplicate debit or erroneous entry" required></textarea>
                </div>
                <div class="col-12">
                    <button class="btn btn-outline-danger btn-sm" onclick="return confirm('Submit deletion request for manager approval?');">
                        <i class="bi bi-shield-x me-1"></i>Request Deletion
                    </button>
                </div>
            </form>
        </div>
    </div>
</div>

<div class="sb-card">
    <h6 class="mb-3">Correction requests</h6>
    <table class="table">
        <thead><tr><th>Txn</th><th>Reason</th><th>Status</th></tr></thead>
        <tbody>
        <c:forEach var="corr" items="${corrections}">
            <tr>
                <td><c:out value="${corr.transaction.referenceNumber}"/></td>
                <td><c:out value="${corr.reason}"/></td>
                <td><c:out value="${corr.status}"/></td>
            </tr>
        </c:forEach>
        <c:if test="${empty corrections}"><tr><td colspan="3" class="text-muted">No correction requests.</td></tr></c:if>
        </tbody>
    </table>
</div>

<jsp:include page="../common/app-frame-end.jsp"/>
