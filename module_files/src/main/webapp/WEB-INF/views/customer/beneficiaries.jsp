<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <title>My Beneficiaries | SmartBank Connect</title>
    <jsp:include page="../common/app-head.jsp"/>
</head>
<body>
<div class="sb-shell">
    <jsp:include page="../common/sidebar.jsp"/>
    <div class="sb-main">
        <div class="sb-topbar"><h5 class="mb-0">Beneficiary Management</h5></div>
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

            <div class="sb-card mb-4">
                <h6 class="mb-3">Add Beneficiary</h6>
                <form method="post" action="<c:url value='/customer/beneficiaries'/>" class="row g-2 needs-validation" novalidate onsubmit="return validateBeneficiaryForm(this);">
                    <c:if test="${not empty _csrf}"><input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" /></c:if>
                    <div class="col-md-3">
                        <label class="form-label small text-muted">Beneficiary Name *</label>
                        <input type="text" class="form-control" name="beneficiaryName" id="beneficiaryName" placeholder="Full Name" minlength="2" maxlength="100" required>
                        <div class="invalid-feedback text-danger" id="nameError" style="font-size: 0.8rem;">Name must be 2-100 characters.</div>
                    </div>
                    <div class="col-md-3">
                        <label class="form-label small text-muted">Account Number *</label>
                        <input type="text" class="form-control" name="accountNumber" id="accountNumber" placeholder="Account Number" minlength="6" maxlength="20" required>
                        <div class="invalid-feedback text-danger" id="accError" style="font-size: 0.8rem;">Account must be 6-20 characters.</div>
                    </div>
                    <div class="col-md-2">
                        <label class="form-label small text-muted">Bank Name</label>
                        <input type="text" class="form-control" name="bank" placeholder="e.g. LankaTrust Bank" maxlength="100">
                    </div>
                    <div class="col-md-2">
                        <label class="form-label small text-muted">Nickname</label>
                        <input type="text" class="form-control" name="nickname" placeholder="e.g. Mom, Landlord" maxlength="50">
                    </div>
                    <div class="col-md-2 d-flex align-items-end">
                        <button type="submit" class="btn btn-gold w-100">Add</button>
                    </div>
                </form>
            </div>

            <div class="sb-card">
                <h6 class="mb-3">Saved Beneficiaries</h6>
                <div class="table-responsive">
                    <table class="table align-middle">
                        <thead><tr><th>Name</th><th>Account</th><th>Bank</th><th>Nickname</th><th>Actions</th></tr></thead>
                        <tbody>
                        <c:forEach var="b" items="${beneficiaries}">
                            <tr>
                                <td>${b.beneficiaryName}</td>
                                <td>${b.beneficiaryAccountNumber}</td>
                                <td>${b.beneficiaryBank}</td>
                                <td>${b.nickname}</td>
                                <td>
                                    <div class="d-inline-flex gap-1">
                                        <button type="button" class="btn btn-sm btn-outline-primary" data-bs-toggle="modal" data-bs-target="#editBenModal${b.id}">
                                            <i class="bi bi-pencil me-1"></i>Edit
                                        </button>
                                        <form method="post" action="<c:url value='/customer/beneficiaries/${b.id}/delete'/>" class="d-inline"
                                              onsubmit="return confirm('Remove this beneficiary?');">
                                            <c:if test="${not empty _csrf}"><input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" /></c:if>
                                            <button class="btn btn-sm btn-outline-danger">Remove</button>
                                        </form>
                                    </div>

                                    <!-- Edit Beneficiary Modal -->
                                    <div class="modal fade" id="editBenModal${b.id}" tabindex="-1" aria-hidden="true">
                                        <div class="modal-dialog">
                                            <div class="modal-content text-start">
                                                <div class="modal-header">
                                                    <h6 class="modal-title">Edit Beneficiary: <c:out value="${b.beneficiaryName}"/></h6>
                                                    <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                                                </div>
                                                <form method="post" action="<c:url value='/customer/beneficiaries/${b.id}/update'/>">
                                                    <c:if test="${not empty _csrf}"><input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" /></c:if>
                                                    <div class="modal-body">
                                                        <div class="mb-3">
                                                            <label class="form-label small text-muted">Account Number</label>
                                                            <input type="text" class="form-control" value="<c:out value='${b.beneficiaryAccountNumber}'/>" readonly disabled>
                                                        </div>
                                                        <div class="mb-3">
                                                            <label class="form-label small text-muted">Bank Name</label>
                                                            <input type="text" class="form-control" name="bank" value="<c:out value='${b.beneficiaryBank}'/>" maxlength="100">
                                                        </div>
                                                        <div class="mb-3">
                                                            <label class="form-label small text-muted">Nickname</label>
                                                            <input type="text" class="form-control" name="nickname" value="<c:out value='${b.nickname}'/>" maxlength="50">
                                                        </div>
                                                    </div>
                                                    <div class="modal-footer">
                                                        <button type="button" class="btn btn-secondary btn-sm" data-bs-dismiss="modal">Cancel</button>
                                                        <button type="submit" class="btn btn-gold btn-sm">Save Changes</button>
                                                    </div>
                                                </form>
                                            </div>
                                        </div>
                                    </div>
                                </td>
                            </tr>
                        </c:forEach>
                        <c:if test="${empty beneficiaries}"><tr><td colspan="5" class="text-muted text-center py-3">No beneficiaries saved.</td></tr></c:if>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    </div>
</div>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
<script>
function validateBeneficiaryForm(form) {
    let valid = true;
    const name = form.beneficiaryName.value.trim();
    const acc = form.accountNumber.value.trim();

    if (name.length < 2 || name.length > 100) {
        form.beneficiaryName.classList.add('is-invalid');
        valid = false;
    } else {
        form.beneficiaryName.classList.remove('is-invalid');
    }

    if (acc.length < 6 || acc.length > 20 || !/^[A-Za-z0-9]+$/.test(acc)) {
        form.accountNumber.classList.add('is-invalid');
        valid = false;
    } else {
        form.accountNumber.classList.remove('is-invalid');
    }

    return valid;
}
</script>
</body>
</html>
