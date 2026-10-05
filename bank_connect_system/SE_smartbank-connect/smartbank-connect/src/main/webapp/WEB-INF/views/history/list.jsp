<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="pageTitle" value="Manager Approvals" scope="request"/>
<jsp:include page="
































../common/app-frame-start.jsp"/>

<div class="d-flex justify-content-between align-items-center mb-3">
    <div>
        <h5 class="mb-1"><i class="bi bi-shield-check text-primary me-2"></i>Dual Authorization & Manager Approvals</h5>
        <p class="text-muted small mb-0">Four-Eyes Principle: Gated operations require dual authorization. Requesters cannot approve their own requests.</p>
    </div>
    <div class="d-flex gap-2 align-items-center">
        <span class="badge bg-primary px-3 py-2"><i class="bi bi-person-badge me-1"></i>Approver: <c:out value="${user.fullName}"/> (<c:out value="${user.role}"/>)</span>
    </div>
</div>

<c:if test="${not empty success}">
    <div class="alert alert-success alert-dismissible fade show py-2" role="alert">
        <i class="bi bi-check-circle-fill me-2"></i><c:out value="${success}"/>
        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    </div>
</c:if>
<c:if test="${not empty error}">
    <div class="alert alert-danger alert-dismissible fade show py-2" role="alert">
        <i class="bi bi-exclamation-triangle-fill me-2"></i><c:out value="${error}"/>
        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    </div>
</c:if>

<div class="sb-card mb-4">
    <div class="d-flex justify-content-between align-items-center mb-3">
        <h6 class="mb-0"><i class="bi bi-hourglass-split text-warning me-2"></i>Pending Dual Authorization Queue</h6>
        <span class="badge bg-warning text-dark"><c:out value="${pending.size()}"/> Pending</span>
    </div>
    <div class="table-responsive">
        <table class="table align-middle table-hover">
            <thead class="table-light">
                <tr>
                    <th>Type</th>
                    <th>Reference</th>
                    <th>Requester</th>
                    <th>Requested At</th>
                    <th>Proposed Change / Remarks</th>
                    <th>Dual Auth Status</th>
                    <th class="text-end">Actions</th>
                </tr>
            </thead>
            <tbody>
            <c:forEach var="r" items="${pending}">
                <c:set var="isSelf" value="${(not empty user && not empty r.requester && (user.id == r.requester.id || user.email.equalsIgnoreCase(r.requester.email))) || (not empty r.requester && r.requester.email.equalsIgnoreCase(pageContext.request.userPrincipal.name))}"/>
                <tr>
                    <td>
                        <span class="badge bg-primary text-wrap"><c:out value="${r.requestType}"/></span>
                    </td>
                    <td class="fw-semibold">
                        <c:out value="${r.targetReference}"/>
                    </td>
                    <td>
                        <div class="fw-medium"><c:out value="${r.requester.fullName}"/></div>
                        <div class="small text-muted"><c:out value="${r.requester.role}"/> &bull; <c:out value="${r.requester.email}"/></div>
                    </td>
                    <td class="small text-muted">
                        <c:out value="${r.requestedAt}"/>
                    </td>
                    <td class="small" style="max-width: 280px; word-break: break-word;">
                        <c:choose>
                            <c:when test="${not empty r.requestedData}">
                                <div class="p-1 bg-light rounded font-monospace small"><c:out value="${r.requestedData}"/></div>
                            </c:when>
                            <c:otherwise>
                                <c:out value="${r.remarks}"/>
                            </c:otherwise>
                        </c:choose>
                    </td>
                    <td>
                        <c:choose>
                            <c:when test="${isSelf}">
                                <span class="badge bg-warning text-dark" title="Dual authorization required: requesters cannot approve their own requests. A different authorized manager must approve this.">
                                    <i class="bi bi-shield-lock me-1"></i>Self-Requested
                                </span>
                            </c:when>
                            <c:otherwise>
                                <span class="badge bg-success" title="You are authorized to review and approve this request.">
                                    <i class="bi bi-check2-circle me-1"></i>Ready for Approval
                                </span>
                            </c:otherwise>
                        </c:choose>
                    </td>
                    <td class="text-end">
                        <div class="d-flex gap-1 justify-content-end">
                            <c:choose>
                                <c:when test="${isSelf}">
                                    <button class="btn btn-sm btn-secondary" disabled title="This request cannot be approved by its requester. A different authorized Manager is required.">
                                        <i class="bi bi-lock me-1"></i>Approve
                                    </button>
                                </c:when>
                                <c:otherwise>
                                    <form action="<c:url value='/approvals/${r.id}/approve'/>" method="post" class="d-inline">
                                        <c:if test="${not empty _csrf}"><input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" /></c:if>
                                        <button class="btn btn-sm btn-success"><i class="bi bi-check-circle me-1"></i>Approve</button>
                                    </form>
                                </c:otherwise>
                            </c:choose>

                            <button type="button" class="btn btn-sm btn-outline-danger" data-bs-toggle="modal" data-bs-target="#rejectModal${r.id}">
                                <i class="bi bi-x-circle me-1"></i>Reject
                            </button>
                        </div>

                        <!-- Reject Modal -->
                        <div class="modal fade" id="rejectModal${r.id}" tabindex="-1" aria-hidden="true">
                            <div class="modal-dialog modal-dialog-centered text-start">
                                <div class="modal-content">
                                    <form action="<c:url value='/approvals/${r.id}/reject'/>" method="post">
                                        <c:if test="${not empty _csrf}"><input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" /></c:if>
                                        <div class="modal-header">
                                            <h6 class="modal-title"><i class="bi bi-x-circle text-danger me-2"></i>Reject Request: <c:out value="${r.requestType}"/></h6>
                                            <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                                        </div>
                                        <div class="modal-body">
                                            <p class="small text-muted mb-2">Target Reference: <strong><c:out value="${r.targetReference}"/></strong></p>
                                            <label class="form-label small fw-semibold">Rejection Reason <span class="text-danger">*</span></label>
                                            <textarea name="remarks" class="form-control" rows="3" required placeholder="Specify why this request is being rejected..."></textarea>
                                        </div>
                                        <div class="modal-footer">
                                            <button type="button" class="btn btn-sm btn-secondary" data-bs-dismiss="modal">Cancel</button>
                                            <button type="submit" class="btn btn-sm btn-danger">Confirm Rejection</button>
                                        </div>
                                    </form>
                                </div>
                            </div>
                        </div>


                    </td>
                </tr>
            </c:forEach>
            <c:if test="${empty pending}">
                <tr><td colspan="7" class="text-muted text-center py-4"><i class="bi bi-check-circle text-success me-2"></i>No pending approval requests.</td></tr>
            </c:if>
            </tbody>
        </table>
    </div>
</div>

<c:if test="${not empty pendingAccounts}">
<div class="sb-card mb-4">
    <div class="d-flex justify-content-between align-items-center mb-3">
        <h6 class="mb-0"><i class="bi bi-person-vcard text-primary me-2"></i>Pending Account Applications</h6>
        <span class="badge bg-secondary"><c:out value="${pendingAccounts.size()}"/> Pending</span>
    </div>
    <div class="table-responsive">
        <table class="table align-middle table-hover">
            <thead class="table-light">
                <tr>
                    <th>Account No</th>
                    <th>Customer Name</th>
                    <th>Type</th>
                    <th>Branch</th>
                    <th>Deposit (Rs.)</th>
                    <th>Submitted By</th>
                    <th>Dual Auth Status</th>
                    <th class="text-end">Actions</th>
                </tr>
            </thead>
            <tbody>
                <c:forEach var="acc" items="${pendingAccounts}">
                    <c:set var="isSelfAcc" value="${(not empty user && not empty acc.submittedBy && (user.id == acc.submittedBy.id || user.email.equalsIgnoreCase(acc.submittedBy.email))) || (not empty acc.submittedBy && acc.submittedBy.email.equalsIgnoreCase(pageContext.request.userPrincipal.name))}"/>
                    <tr>
                        <td class="fw-semibold"><c:out value="${acc.accountNumber}"/></td>
                        <td><c:out value="${acc.customer.fullName}"/></td>
                        <td><span class="badge bg-secondary"><c:out value="${acc.accountType}"/></span></td>
                        <td><c:out value="${acc.branch != null ? acc.branch.name : '-'}"/></td>
                        <td><fmt:formatNumber value="${acc.initialDeposit}" maxFractionDigits="2"/></td>
                        <td class="small">
                            <c:out value="${acc.submittedBy != null ? acc.submittedBy.fullName : '-'}"/>
                        </td>
                        <td>
                            <c:choose>
                                <c:when test="${isSelfAcc}">
                                    <span class="badge bg-warning text-dark"><i class="bi bi-shield-lock me-1"></i>Self-Submitted</span>
                                </c:when>
                                <c:otherwise>
                                    <span class="badge bg-success"><i class="bi bi-check2-circle me-1"></i>Ready</span>
                                </c:otherwise>
                            </c:choose>
                        </td>
                        <td class="text-end">
                            <c:choose>
                                <c:when test="${isSelfAcc}">
                                    <button class="btn btn-sm btn-secondary" disabled title="This account application was submitted by you. Another authorized Manager is required.">
                                        <i class="bi bi-lock me-1"></i>Approve
                                    </button>
                                </c:when>
                                <c:otherwise>
                                    <form action="<c:url value='/accounts/manage/${acc.id}/approve'/>" method="post" class="d-inline">
                                        <c:if test="${not empty _csrf}"><input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" /></c:if>
                                        <button type="submit" class="btn btn-sm btn-success"><i class="bi bi-check-circle me-1"></i>Approve</button>
                                    </form>
                                </c:otherwise>
                            </c:choose>
                            <form action="<c:url value='/accounts/manage/${acc.id}/reject'/>" method="post" class="d-inline"
                                  onsubmit="var rsn=prompt('Rejection reason:'); if(!rsn) return false; this.querySelector('input[name=reason]').value=rsn;">
                                <c:if test="${not empty _csrf}"><input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" /></c:if>
                                <input type="hidden" name="reason" value="">
                                <button type="submit" class="btn btn-sm btn-outline-danger"><i class="bi bi-x-circle me-1"></i>Reject</button>
                            </form>
                        </td>
                    </tr>
                </c:forEach>
            </tbody>
        </table>
    </div>
</div>
</c:if>

<div class="sb-card">
    <div class="d-flex justify-content-between align-items-center mb-3">
        <h6 class="mb-0"><i class="bi bi-journal-check text-info me-2"></i>Recent Approval Decisions & Execution Audit</h6>
        <span class="badge bg-light text-dark"><c:out value="${all.size()}"/> Total</span>
    </div>
    <div class="table-responsive">
        <table class="table align-middle table-sm">
            <thead class="table-light">
                <tr>
                    <th>ID</th>
                    <th>Type</th>
                    <th>Reference</th>
                    <th>Requester</th>
                    <th>Decision</th>
                    <th>Decided By</th>
                    <th>Date / Time</th>
                    <th>Remarks / Reason</th>
                </tr>
            </thead>
            <tbody>
                <c:forEach var="item" items="${all}">
                    <c:if test="${item.status != 'PENDING'}">
                        <tr>
                            <td class="text-muted">#<c:out value="${item.id}"/></td>
                            <td><span class="badge bg-secondary"><c:out value="${item.requestType}"/></span></td>
                            <td class="fw-semibold small"><c:out value="${item.targetReference}"/></td>
                            <td class="small"><c:out value="${item.requester.fullName}"/></td>
                            <td>
                                <c:choose>
                                    <c:when test="${item.status == 'APPROVED'}">
                                        <span class="badge bg-success"><i class="bi bi-check-circle me-1"></i>APPROVED</span>
                                    </c:when>
                                    <c:otherwise>
                                        <span class="badge bg-danger"><i class="bi bi-x-circle me-1"></i>REJECTED</span>
                                    </c:otherwise>
                                </c:choose>
                            </td>
                            <td class="small"><c:out value="${item.approvedBy != null ? item.approvedBy.fullName : '-'}"/></td>
                            <td class="small text-muted"><c:out value="${item.approvedAt != null ? item.approvedAt : item.rejectedAt}"/></td>
                            <td class="small text-muted">
                                <c:out value="${item.rejectionReason != null ? item.rejectionReason : item.remarks}"/>
                            </td>
                        </tr>
                    </c:if>
                </c:forEach>
            </tbody>
        </table>
    </div>
</div>

<jsp:include page="../common/app-frame-end.jsp"/>
