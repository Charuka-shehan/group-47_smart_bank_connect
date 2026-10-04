<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <title>Open Account | SmartBank Connect</title>
    <jsp:include page="../common/app-head.jsp"/>
</head>
<body>
<div class="sb-shell">
    <jsp:include page="../common/sidebar.jsp"/>
    <div class="sb-main">
        <div class="sb-topbar">
            <h5 class="mb-0"><i class="bi bi-folder-plus text-primary me-2"></i>Open New Account</h5>
        </div>
        <div class="sb-content">
            <c:if test="${not empty error}">
                <div class="alert alert-danger alert-dismissible fade show" role="alert">${error}<button type="button" class="btn-close" data-bs-dismiss="alert"></button></div>
            </c:if>
            <c:if test="${not empty success}">
                <div class="alert alert-success alert-dismissible fade show" role="alert">${success}<button type="button" class="btn-close" data-bs-dismiss="alert"></button></div>
            </c:if>

        <form id="openAccountForm" method="post" action="<c:url value='/accounts/open'/>" enctype="multipart/form-data" class="card p-4" novalidate>
            <c:if test="${not empty _csrf}"><input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" /></c:if>

            <h5 class="mb-3">Customer Information</h5>
            <div class="row g-3">
                <div class="col-md-6">
                    <label class="form-label" for="customerId">Customer ID <span class="text-danger">*</span></label>
                    <input type="text" class="form-control" name="customerId" id="customerId" placeholder="e.g. CUST001" required>
                    <div class="invalid-feedback text-danger" id="custIdError">Customer ID is required.</div>
                </div>
                <div class="col-md-6">
                    <label class="form-label" for="fullName">Full Name <span class="text-danger">*</span></label>
                    <input type="text" class="form-control" name="fullName" id="fullName" placeholder="Full legal name" minlength="2" required>
                    <div class="invalid-feedback text-danger" id="nameError">Full name must be at least 2 characters.</div>
                </div>
                <div class="col-md-6">
                    <label class="form-label" for="email">Email <span class="text-danger">*</span></label>
                    <input type="email" class="form-control" name="email" id="email" placeholder="customer@example.com" required>
                    <div class="invalid-feedback text-danger" id="emailError">Valid email address is required.</div>
                </div>
                <div class="col-md-6">
                    <label class="form-label" for="phone">Mobile Number <span class="text-danger">*</span></label>
                    <input type="text" class="form-control" name="phone" id="phone" placeholder="077 123 4567" required>
                    <div class="invalid-feedback text-danger" id="phoneError">Valid phone number is required.</div>
                </div>
                <div class="col-md-6">
                    <label class="form-label" for="nic">NIC <span class="text-danger">*</span></label>
                    <input type="text" class="form-control" name="nic" id="nic" placeholder="9 digits + V or 12 digits" required>
                    <div class="invalid-feedback text-danger" id="nicError">Valid NIC is required (e.g. 123456789V or 12 digits).</div>
                </div>
                <div class="col-md-6">
                    <label class="form-label" for="dob">Date of Birth (Must be 18+) <span class="text-danger">*</span></label>
                    <input type="date" class="form-control" name="dob" id="dob" required>
                    <div class="invalid-feedback text-danger" id="dobError">Customer must be at least 18 years old.</div>
                </div>
                <div class="col-12">
                    <label class="form-label" for="address">Address <span class="text-danger">*</span></label>
                    <textarea class="form-control" name="address" id="address" rows="2" placeholder="Permanent address" required></textarea>
                    <div class="invalid-feedback text-danger" id="addressError">Address is required.</div>
                </div>
            </div>

            <h5 class="mt-4 mb-3">Employment & Account Details</h5>
            <div class="row g-3">
                <div class="col-md-6">
                    <label class="form-label" for="occupation">Occupation <span class="text-danger">*</span></label>
                    <input type="text" class="form-control" name="occupation" id="occupation" required>
                    <div class="invalid-feedback text-danger" id="occError">Occupation is required.</div>
                </div>
                <div class="col-md-6">
                    <label class="form-label" for="monthlyIncome">Monthly Income (LKR) <span class="text-danger">*</span></label>
                    <input type="number" step="0.01" min="0.01" class="form-control" name="monthlyIncome" id="monthlyIncome" required>
                    <div class="invalid-feedback text-danger" id="incomeError">Monthly income must be greater than zero.</div>
                </div>
                <div class="col-md-6">
                    <label class="form-label" for="accountType">Account Type <span class="text-danger">*</span></label>
                    <select name="accountType" id="accountType" class="form-select" required>
                        <c:forEach var="type" items="${accountTypes}">
                            <option value="${type}">${type}</option>
                        </c:forEach>
                    </select>
                </div>
                <div class="col-md-6">
                    <label class="form-label" for="initialDeposit">Initial Deposit (LKR) <span class="text-danger">*</span></label>
                    <input type="number" step="0.01" class="form-control" name="initialDeposit" id="initialDeposit" required>
                    <div class="invalid-feedback text-danger" id="depositError">Initial deposit does not meet the minimum requirement.</div>
                    <div class="form-text" id="depositHint">Minimum: Savings LKR 1,000 | Current LKR 5,000 | Fixed Deposit LKR 50,000</div>
                </div>
                <div class="col-md-6">
                    <label class="form-label" for="branchId">Branch <span class="text-danger">*</span></label>
                    <select name="branchId" id="branchId" class="form-select" required>
                        <c:forEach var="branch" items="${branches}">
                            <option value="${branch.id}">${branch.name}</option>
                        </c:forEach>
                    </select>
                </div>
            </div>

            <h5 class="mt-4 mb-3">Nominee Details</h5>
            <div class="row g-3">
                <div class="col-md-6">
                    <label class="form-label">Nominee Name</label>
                    <input type="text" class="form-control" name="nomineeName">
                </div>
                <div class="col-md-6">
                    <label class="form-label">Relationship</label>
                    <input type="text" class="form-control" name="nomineeRelationship">
                </div>
            </div>

            <h5 class="mt-4 mb-3">Documents</h5>
            <div class="row g-3">
                <div class="col-md-6">
                    <label class="form-label">Signature</label>
                    <input type="file" class="form-control" name="signature" accept="image/*,.pdf">
                </div>
                <div class="col-md-6">
                    <label class="form-label">NIC Front</label>
                    <input type="file" class="form-control" name="nicFront" accept="image/*,.pdf">
                </div>
                <div class="col-md-6">
                    <label class="form-label">NIC Back</label>
                    <input type="file" class="form-control" name="nicBack" accept="image/*,.pdf">
                </div>
                <div class="col-md-6">
                    <label class="form-label">Proof of Address</label>
                    <input type="file" class="form-control" name="proofOfAddress" accept="image/*,.pdf">
                </div>
            </div>

            <div class="mt-4">
                <button type="submit" class="btn btn-gold py-2 px-4"><i class="bi bi-check-circle me-1"></i> Submit Account Opening Request</button>
                <a href="<c:url value='/accounts/manage'/>" class="btn btn-outline-secondary ms-2">Cancel</a>
            </div>
        </form>
    </div>
</div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
<script>
// Set max date for DOB to 18 years ago today
const today = new Date();
const maxDob = new Date(today.getFullYear() - 18, today.getMonth(), today.getDate());
const maxDobStr = maxDob.toISOString().split('T')[0];
document.getElementById('dob').setAttribute('max', maxDobStr);

document.getElementById('openAccountForm').addEventListener('submit', function(e) {
    let valid = true;
    const dob = document.getElementById('dob');
    const deposit = document.getElementById('initialDeposit');
    const accType = document.getElementById('accountType').value;
    const income = document.getElementById('monthlyIncome');

    if (dob.value) {
        const enteredDate = new Date(dob.value);
        if (enteredDate > maxDob) {
            dob.classList.add('is-invalid');
            valid = false;
        } else {
            dob.classList.remove('is-invalid');
        }
    } else {
        dob.classList.add('is-invalid');
        valid = false;
    }

    const depVal = parseFloat(deposit.value);
    let minDep = 1000;
    if (accType === 'CURRENT') minDep = 5000;
    if (accType === 'FIXED_DEPOSIT') minDep = 50000;

    if (isNaN(depVal) || depVal < minDep) {
        deposit.classList.add('is-invalid');
        document.getElementById('depositError').textContent = 'Minimum deposit for ' + accType + ' is LKR ' + minDep.toLocaleString();
        valid = false;
    } else {
        deposit.classList.remove('is-invalid');
    }

    const incVal = parseFloat(income.value);
    if (isNaN(incVal) || incVal <= 0) {
        income.classList.add('is-invalid');
        valid = false;
    } else {
        income.classList.remove('is-invalid');
    }

    if (!valid) {
        e.preventDefault();
        return false;
    }

    if (!confirm('Submit new account opening application for manager approval?')) {
        e.preventDefault();
        return false;
    }
});
</script>
</body>
</html>
