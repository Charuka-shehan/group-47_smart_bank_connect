<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Notification Templates" scope="request"/>
<jsp:include page="../common/app-frame-start.jsp"/>

<c:if test="${not empty success}"><div class="alert alert-success py-2 mb-3">${success}</div></c:if>
<c:if test="${not empty error}"><div class="alert alert-danger py-2 mb-3">${error}</div></c:if>

<div class="d-flex justify-content-between align-items-center mb-3">
    <p class="text-muted small mb-0">Manage system notification templates. Changes and deletions require Manager approval.</p>
    <button type="button" class="btn btn-sm btn-primary" data-bs-toggle="modal" data-bs-target="#createTemplateModal">
        <i class="bi bi-plus-circle me-1"></i>New Template
    </button>
</div>

<!-- Create Template Modal -->
<div class="modal fade" id="createTemplateModal" tabindex="-1" aria-hidden="true">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <h6 class="modal-title">Create Notification Template</h6>
                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
            </div>
            <form method="post" action="<c:url value='/officer/templates/create'/>">
                <c:if test="${not empty _csrf}"><input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" /></c:if>
                <div class="modal-body">
                    <div class="mb-3">
                        <label class="form-label">Template Key <span class="text-danger">*</span></label>
                        <input class="form-control" name="templateKey" placeholder="e.g. KYC_APPROVED" required>
                    </div>
                    <div class="mb-3">
                        <label class="form-label">Template Name <span class="text-danger">*</span></label>
                        <input class="form-control" name="name" placeholder="e.g. KYC Approval Notification" required>
                    </div>
                    <div class="mb-3">
                        <label class="form-label">Delivery Channel</label>
                        <select name="channel" class="form-select">
                            <option value="EMAIL">Email</option>
                            <option value="SMS">SMS</option>
                            <option value="IN_APP">In-App</option>
                        </select>
                    </div>
                    <div class="mb-3">
                        <label class="form-label">Subject</label>
                        <input class="form-control" name="subject" placeholder="e.g. Your KYC verification has been approved">
                    </div>
                    <div class="mb-3">
                        <label class="form-label">Message Body <span class="text-danger">*</span></label>
                        <textarea class="form-control" name="body" rows="3" required placeholder="Template text content..."></textarea>
                    </div>
                    <div class="alert alert-info py-2 small mb-0">
                        <i class="bi bi-info-circle me-1"></i> Submitted for Manager dual-authorization before appearing in live templates.
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-secondary btn-sm" data-bs-dismiss="modal">Cancel</button>
                    <button type="submit" class="btn btn-primary btn-sm">Submit Template</button>
                </div>
            </form>
        </div>
    </div>
</div>

<div class="row">
    <c:forEach var="t" items="${templates}">
        <div class="col-md-6 mb-3">
            <div class="sb-card h-100 d-flex flex-column justify-content-between">
                <div>
                    <div class="d-flex justify-content-between align-items-start mb-2">
                        <h6 class="mb-0"><c:out value="${t.name}"/> <small class="text-muted d-block font-monospace">(${t.templateKey})</small></h6>
                        <span class="badge bg-secondary">${t.channel}</span>
                    </div>
                    <form method="post" action="<c:url value='/officer/templates/${t.id}'/>">
                        <c:if test="${not empty _csrf}"><input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" /></c:if>
                        <div class="mb-2">
                            <label class="form-label small">Subject</label>
                            <input class="form-control form-control-sm" name="subject" value="<c:out value='${t.subject}'/>">
                        </div>
                        <div class="mb-2">
                            <label class="form-label small">Body</label>
                            <textarea class="form-control form-control-sm" name="body" rows="3"><c:out value="${t.body}"/></textarea>
                        </div>
                        <div class="d-flex justify-content-between align-items-center mt-3">
                            <button type="submit" class="btn btn-sm btn-primary">Save (Submit for Approval)</button>
                        </div>
                    </form>
                </div>
                <div class="border-top pt-2 mt-2 text-end">
                    <form method="post" action="<c:url value='/officer/templates/${t.id}/delete'/>" class="d-inline"
                          onsubmit="return confirm('Submit template deletion for manager approval?');">
                        <c:if test="${not empty _csrf}"><input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" /></c:if>
                        <button type="submit" class="btn btn-sm btn-outline-danger"><i class="bi bi-trash me-1"></i>Request Deletion</button>
                    </form>
                </div>
            </div>
        </div>
    </c:forEach>
</div>

<jsp:include page="../common/app-frame-end.jsp"/>
