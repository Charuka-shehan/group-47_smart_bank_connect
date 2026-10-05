<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <title>My Documents | SmartBank Connect</title>
    <jsp:include page="../common/app-head.jsp"/>
</head>
<body>
<div class="sb-shell">
    <jsp:include page="../common/sidebar.jsp"/>
    <div class="sb-main">
        <div class="sb-topbar"><h5 class="mb-0">My Documents</h5></div>
        <div class="sb-content">
            <c:if test="${not empty success}">
                <div class="alert alert-success alert-dismissible fade show" role="alert">${success}<button type="button" class="btn-close" data-bs-dismiss="alert"></button></div>
            </c:if>
            <c:if test="${not empty error}">
                <div class="alert alert-danger alert-dismissible fade show" role="alert">${error}<button type="button" class="btn-close" data-bs-dismiss="alert"></button></div>
            </c:if>

            <div class="sb-card mb-4">
                <h6 class="mb-3">Upload New Document</h6>
                <form method="post" action="<c:url value='/customer/documents'/>" enctype="multipart/form-data" class="row g-3">
                    <c:if test="${not empty _csrf}"><input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" /></c:if>
                    <div class="col-md-4">
                        <select class="form-select" name="documentType" required>
                            <option value="">Select document type...</option>
                            <option value="NIC_FRONT">NIC Front</option>
                            <option value="NIC_BACK">NIC Back</option>
                            <option value="PROOF_OF_ADDRESS">Proof of Address</option>
                            <option value="INCOME_PROOF">Income Proof</option>
                            <option value="OTHER">Other</option>
                        </select>
                    </div>
                    <div class="col-md-5">
                        <input type="file" class="form-control" name="document" accept=".pdf,.jpg,.jpeg,.png" required>
                    </div>
                    <div class="col-md-3">
                        <button type="submit" class="btn btn-primary w-100"><i class="bi bi-upload"></i> Upload</button>
                    </div>
                </form>
            </div>

            <div class="sb-card">
                <h6 class="mb-3">Uploaded Documents</h6>
                <div class="table-responsive">
                    <table class="table align-middle">
                        <thead>
                            <tr><th>Type</th><th>File Name</th><th>Uploaded At</th><th>Status</th><th>Action</th></tr>
                        </thead>
                        <tbody>
                            <c:forEach var="doc" items="${documents}">
                                <tr>
                                    <td><c:out value="${doc.documentType}"/></td>
                                    <td><c:out value="${doc.fileName}"/></td>
                                    <td><c:out value="${doc.uploadedAt}"/></td>
                                    <td>
                                        <span class="badge ${doc.verified ? 'bg-success' : 'bg-warning text-dark'}">
                                            ${doc.verified ? 'Verified' : 'Pending Verification'}
                                        </span>
                                    </td>
                                    <td>
                                        <div class="d-inline-flex gap-1">
                                            <a href="<c:url value='${doc.filePath}'/>" target="_blank" class="btn btn-sm btn-outline-primary">View</a>
                                            <form method="post" action="<c:url value='/customer/documents/${doc.id}/delete'/>" class="d-inline"
                                                  onsubmit="return confirm('Are you sure you want to delete this document?');">
                                                <c:if test="${not empty _csrf}"><input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" /></c:if>
                                                <button type="submit" class="btn btn-sm btn-outline-danger" title="Delete Document"><i class="bi bi-trash"></i></button>
                                            </form>
                                        </div>
                                    </td>
                                </tr>
                            </c:forEach>
                            <c:if test="${empty documents}">
                                <tr><td colspan="5" class="text-muted text-center py-3">No documents uploaded yet.</td></tr>
                            </c:if>
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
