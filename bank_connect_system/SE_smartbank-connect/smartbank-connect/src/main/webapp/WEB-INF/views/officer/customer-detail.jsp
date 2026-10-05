<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="pageTitle" value="Customer Record" scope="request"/>
<jsp:include page="../common/app-frame-start.jsp"/>

<div class="sb-card mb-4">
    <h6><c:out value="${customer.fullName}"/></h6>
    <p class="mb-1 small text-muted"><c:out value="${customer.email}"/> · <c:out value="${customer.phoneNumber}"/> · NIC <c:out value="${customer.nic}"/></p>
</div>

<div class="sb-card mb-4">
    <h6 class="mb-3">Request record update (Manager approval required)</h6>
    <c:if test="${not empty success}"><div class="alert alert-success py-2"><c:out value="${success}"/></div></c:if>
    <c:if test="${not empty error}"><div class="alert alert-danger py-2"><c:out value="${error}"/></div></c:if>
    <form method="post" action="<c:url value='/officer/customers/${customer.id}/request-update'/>" class="row g-3">
        <c:if test="${not empty _csrf}"><input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" /></c:if>
        <div class="col-md-4">
            <label class="form-label">Full name</label>
            <input class="form-control" name="fullName" value="<c:out value='${customer.fullName}'/>" required>
        </div>
        <div class="col-md-4">
            <label class="form-label">Phone</label>
            <input class="form-control" name="phone" value="<c:out value='${customer.phoneNumber}'/>" required>
        </div>
        <div class="col-md-4">
            <label class="form-label">Address</label>
            <input class="form-control" name="address" value="<c:out value='${address}'/>">
        </div>
        <div class="col-12"><button class="btn btn-primary">Submit update for manager approval</button></div>
    </form>
    <form method="post" action="<c:url value='/officer/customers/${customer.id}/request-deletion'/>" class="mt-3"
          onsubmit="var rsn=prompt('Reason for deletion request:'); if(!rsn) return false; this.querySelector('[name=reason]').value=rsn;">
        <c:if test="${not empty _csrf}"><input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" /></c:if>
        <input type="hidden" name="reason" value="">
        <button class="btn btn-outline-danger btn-sm">Request record deletion (outdated record)</button>
    </form>
</div>

<div class="sb-card mb-4">
    <h6 class="mb-3">Accounts</h6>
    <table class="table">
        <thead><tr><th>Number</th><th>Type</th><th>Balance</th><th>Status</th></tr></thead>
        <tbody>
        <c:forEach var="a" items="${accounts}">
            <tr>
                <td><c:out value="${a.accountNumber}"/></td>
                <td><c:out value="${a.accountType}"/></td>
                <td>LKR <fmt:formatNumber value="${a.balance}" maxFractionDigits="2"/></td>
                <td><c:out value="${a.status}"/></td>
            </tr>
        </c:forEach>
        </tbody>
    </table>
</div>

<div class="sb-card mb-4">
    <div class="d-flex justify-content-between align-items-center mb-3">
        <h6 class="mb-0">KYC documents</h6>
        <button type="button" class="btn btn-sm btn-primary" data-bs-toggle="modal" data-bs-target="#uploadDocModal">
            <i class="bi bi-upload me-1"></i>Upload Document
        </button>
    </div>
    <table class="table align-middle">
        <thead><tr><th>Type</th><th>File</th><th>Uploaded</th><th>Verified</th></tr></thead>
        <tbody>
        <c:forEach var="d" items="${documents}">
            <tr>
                <td><span class="badge bg-secondary"><c:out value="${d.documentType}"/></span></td>
                <td><a href="<c:url value='${d.filePath}'/>" target="_blank" class="fw-semibold"><c:out value="${d.fileName}"/></a></td>
                <td class="small text-muted"><c:out value="${d.uploadedAt}"/></td>
                <td>
                    <c:choose>
                        <c:when test="${d.verified}"><span class="badge bg-success">Verified</span></c:when>
                        <c:otherwise><span class="badge bg-warning text-dark">Pending</span></c:otherwise>
                    </c:choose>
                </td>
            </tr>
        </c:forEach>
        <c:if test="${empty documents}"><tr><td colspan="4" class="text-muted text-center py-3">No documents uploaded yet.</td></tr></c:if>
        </tbody>
    </table>
</div>

<!-- Upload Document Modal -->
<div class="modal fade" id="uploadDocModal" tabindex="-1" aria-hidden="true">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <h6 class="modal-title">Upload KYC Document</h6>
                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
            </div>
            <form method="post" action="<c:url value='/officer/customers/${customer.id}/upload-document'/>" enctype="multipart/form-data">
                <c:if test="${not empty _csrf}"><input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" /></c:if>
                <div class="modal-body">
                    <div class="mb-3">
                        <label class="form-label">Document Type <span class="text-danger">*</span></label>
                        <select name="documentType" class="form-select" required>
                            <option value="NIC_FRONT">NIC Front Copy</option>
                            <option value="NIC_BACK">NIC Back Copy</option>
                            <option value="PASSPORT">Passport</option>
                            <option value="DRIVING_LICENSE">Driving License</option>
                            <option value="UTILITY_BILL">Proof of Address / Utility Bill</option>
                            <option value="SALARY_SLIP">Salary Slip / Proof of Income</option>
                            <option value="OTHER">Other KYC Document</option>
                        </select>
                    </div>
                    <div class="mb-3">
                        <label class="form-label">Choose File (PDF, JPG, PNG - Max 10MB) <span class="text-danger">*</span></label>
                        <input type="file" name="file" class="form-control" accept=".pdf,.jpg,.jpeg,.png" required>
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-secondary btn-sm" data-bs-dismiss="modal">Cancel</button>
                    <button type="submit" class="btn btn-primary btn-sm">Upload Document</button>
                </div>
            </form>
        </div>
    </div>
</div>

<jsp:include page="../common/app-frame-end.jsp"/>
