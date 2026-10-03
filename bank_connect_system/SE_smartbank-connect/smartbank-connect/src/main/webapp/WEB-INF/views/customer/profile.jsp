<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <title>My Profile | SmartBank Connect</title>
    <jsp:include page="../common/app-head.jsp"/>
</head>
<body>
<div class="sb-shell">
    <jsp:include page="../common/sidebar.jsp"/>
    <div class="sb-main">
        <div class="sb-topbar">
            <h5 class="mb-0"><i class="bi bi-person-circle text-primary me-2"></i>My Profile</h5>
            <div class="d-flex align-items-center gap-2">
                <span class="badge bg-primary-subtle text-primary border border-primary-subtle px-3 py-2 rounded-pill">
                    <i class="bi bi-shield-check me-1"></i><c:out value="${user.role}"/>
                </span>
                <a href="<c:url value='/customer/change-password'/>" class="btn btn-sm btn-outline-secondary">
                    <i class="bi bi-key me-1"></i>Change Password
                </a>
            </div>
        </div>
        <div class="sb-content">
            <c:if test="${not empty success}">
                <div class="alert alert-success alert-dismissible fade show" role="alert">
                    <i class="bi bi-check-circle-fill me-2"></i><c:out value="${success}"/>
                    <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
                </div>
            </c:if>
            <c:if test="${not empty error}">
                <div class="alert alert-danger alert-dismissible fade show" role="alert">
                    <i class="bi bi-exclamation-triangle-fill me-2"></i><c:out value="${error}"/>
                    <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
                </div>
            </c:if>

            <!-- Profile Summary Card -->
            <div class="sb-card mb-4" style="background: linear-gradient(135deg, rgba(26,54,93,0.03) 0%, rgba(201,146,42,0.06) 100%); border: 1px solid rgba(26,54,93,0.1);">
                <div class="d-flex flex-wrap align-items-center justify-content-between gap-3">
                    <div class="d-flex align-items-center gap-3">
                        <c:choose>
                            <c:when test="${not empty profilePhotoPath}">
                                <img src="<c:url value='${profilePhotoPath}'/>" alt="Profile" class="rounded-circle border border-2 border-primary shadow-sm" style="width: 80px; height: 80px; object-fit: cover;">
                            </c:when>
                            <c:otherwise>
                                <div class="rounded-circle text-white d-flex align-items-center justify-content-center shadow-sm" style="width: 80px; height: 80px; font-size: 2rem; background: linear-gradient(135deg, #1A365D 0%, #2A4365 100%);">
                                    <i class="bi bi-person-fill"></i>
                                </div>
                            </c:otherwise>
                        </c:choose>
                        <div>
                            <h4 class="mb-1 fw-bold text-dark"><c:out value="${user.fullName}"/></h4>
                            <div class="d-flex flex-wrap align-items-center gap-2 text-muted small">
                                <span><i class="bi bi-envelope me-1"></i><c:out value="${user.email}"/></span>
                                <span>•</span>
                                <span><i class="bi bi-telephone me-1"></i><c:out value="${user.phoneNumber}"/></span>
                                <c:if test="${isCustomer}">
                                    <span>•</span>
                                    <span><i class="bi bi-card-text me-1"></i>ID: <c:out value="${customerId}"/></span>
                                </c:if>
                            </div>
                        </div>
                    </div>
                    <div class="text-end">
                        <span class="badge bg-success-subtle text-success border border-success-subtle px-3 py-2 rounded-pill">
                            <i class="bi bi-check-circle me-1"></i>Active Account
                        </span>
                    </div>
                </div>
            </div>

            <div class="row g-4">
                <!-- Left Column: Edit Form -->
                <div class="col-lg-8">
                    <div class="sb-card">
                        <h6 class="fw-bold mb-3 pb-2 border-bottom text-primary">
                            <i class="bi bi-pencil-square me-2"></i>Personal & Contact Details
                        </h6>
                        <form method="post" action="<c:url value='/customer/profile'/>" enctype="multipart/form-data" class="row g-3" id="profileForm">
                            <c:if test="${not empty _csrf}"><input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" /></c:if>

                            <div class="col-md-6">
                                <label class="form-label fw-semibold small">Full Name <span class="text-danger">*</span></label>
                                <input type="text" class="form-control" name="fullName" value="<c:out value='${user.fullName}'/>" minlength="2" required>
                                <div class="form-text">As it appears on official identification.</div>
                            </div>

                            <div class="col-md-6">
                                <label class="form-label fw-semibold small">Phone Number <span class="text-danger">*</span></label>
                                <input type="text" class="form-control" name="phone" value="<c:out value='${user.phoneNumber}'/>" required>
                                <div class="form-text">Used for SMS alerts and transaction OTPs.</div>
                            </div>

                            <div class="col-md-6">
                                <label class="form-label fw-semibold small">National Identity Card (NIC)</label>
                                <input type="text" class="form-control bg-light" value="<c:out value='${user.nic}'/>" readonly disabled>
                                <div class="form-text">NIC is verified and cannot be edited online.</div>
                            </div>

                            <c:choose>
                                <c:when test="${isCustomer}">
                                    <div class="col-md-6">
                                        <label class="form-label fw-semibold small">Date of Birth <span class="text-danger">*</span></label>
                                        <input type="date" class="form-control" name="dob" id="dobInput" value="${dob}" required>
                                        <div class="form-text">Must be 18 years or older.</div>
                                    </div>

                                    <div class="col-12">
                                        <label class="form-label fw-semibold small">Residential Address <span class="text-danger">*</span></label>
                                        <textarea class="form-control" name="address" rows="2" required><c:out value="${address}"/></textarea>
                                        <div class="form-text">Permanent registered address for bank correspondence.</div>
                                    </div>

                                    <div class="col-md-6">
                                        <label class="form-label fw-semibold small">Update Profile Photo</label>
                                        <input type="file" class="form-control" name="profilePhoto" accept="image/*">
                                        <div class="form-text">JPG, PNG or WEBP (Max 5MB).</div>
                                    </div>
                                </c:when>
                                <c:otherwise>
                                    <div class="col-md-6">
                                        <label class="form-label fw-semibold small">Account Role</label>
                                        <input type="text" class="form-control bg-light" value="<c:out value='${user.role}'/>" readonly disabled>
                                        <div class="form-text">Internal staff permissions role.</div>
                                    </div>

                                    <div class="col-12">
                                        <div class="p-3 rounded bg-light border small text-muted">
                                            <i class="bi bi-info-circle text-primary me-2"></i>
                                            Staff accounts are configured with institutional bank access. Customer-specific fields (e.g. KYC address and customer birth records) apply to retail customer accounts.
                                        </div>
                                    </div>
                                </c:otherwise>
                            </c:choose>

                            <div class="col-12 pt-3 border-top">
                                <button type="submit" class="btn btn-primary px-4">
                                    <i class="bi bi-check-lg me-1"></i> Save Changes
                                </button>
                                <a href="<c:url value='/customer/change-password'/>" class="btn btn-outline-secondary ms-2">
                                    <i class="bi bi-shield-lock me-1"></i> Security & Password
                                </a>
                            </div>
                        </form>
                    </div>
                </div>

                <!-- Right Column: Security & Status Summary -->
                <div class="col-lg-4">
                    <div class="sb-card mb-4">
                        <h6 class="fw-bold mb-3 pb-2 border-bottom text-secondary">
                            <i class="bi bi-shield-check me-2"></i>Security & Verification
                        </h6>
                        <ul class="list-unstyled mb-0 d-flex flex-column gap-3">
                            <li class="d-flex align-items-center justify-content-between">
                                <span class="text-muted small"><i class="bi bi-person-badge me-2"></i>Identity Verification</span>
                                <span class="badge bg-success-subtle text-success border border-success-subtle"><i class="bi bi-check-circle me-1"></i>Verified</span>
                            </li>
                            <li class="d-flex align-items-center justify-content-between">
                                <span class="text-muted small"><i class="bi bi-phone me-2"></i>SMS OTP Security</span>
                                <span class="badge bg-success-subtle text-success border border-success-subtle"><i class="bi bi-check-circle me-1"></i>Active</span>
                            </li>
                            <li class="d-flex align-items-center justify-content-between">
                                <span class="text-muted small"><i class="bi bi-key me-2"></i>Password Status</span>
                                <span class="badge bg-info-subtle text-info border border-info-subtle">Configured</span>
                            </li>
                            <li class="d-flex align-items-center justify-content-between">
                                <span class="text-muted small"><i class="bi bi-shield-shaded me-2"></i>Role Authorization</span>
                                <span class="fw-semibold small text-dark"><c:out value="${user.role}"/></span>
                            </li>
                        </ul>
                    </div>

                    <div class="sb-card">
                        <h6 class="fw-bold mb-3 pb-2 border-bottom text-secondary">
                            <i class="bi bi-link-45deg me-2"></i>Quick Navigation
                        </h6>
                        <div class="d-flex flex-column gap-2">
                            <c:if test="${isCustomer}">
                                <a href="<c:url value='/customer/documents'/>" class="btn btn-sm btn-light text-start border d-flex align-items-center justify-content-between">
                                    <span><i class="bi bi-file-earmark-text text-primary me-2"></i>My KYC Documents</span>
                                    <i class="bi bi-chevron-right text-muted small"></i>
                                </a>
                                <a href="<c:url value='/customer/beneficiaries'/>" class="btn btn-sm btn-light text-start border d-flex align-items-center justify-content-between">
                                    <span><i class="bi bi-people text-primary me-2"></i>Manage Beneficiaries</span>
                                    <i class="bi bi-chevron-right text-muted small"></i>
                                </a>
                            </c:if>
                            <a href="<c:url value='/history'/>" class="btn btn-sm btn-light text-start border d-flex align-items-center justify-content-between">
                                <span><i class="bi bi-clock-history text-primary me-2"></i>Transaction History</span>
                                <i class="bi bi-chevron-right text-muted small"></i>
                            </a>
                            <a href="<c:url value='/notifications'/>" class="btn btn-sm btn-light text-start border d-flex align-items-center justify-content-between">
                                <span><i class="bi bi-bell text-primary me-2"></i>Notification Center</span>
                                <i class="bi bi-chevron-right text-muted small"></i>
                            </a>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
<script>
    const dobInput = document.getElementById('dobInput');
    if (dobInput) {
        const today = new Date();
        const maxDob = new Date(today.getFullYear() - 18, today.getMonth(), today.getDate());
        dobInput.setAttribute('max', maxDob.toISOString().split('T')[0]);
    }
</script>
</body>
</html>
