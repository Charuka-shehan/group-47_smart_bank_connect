<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <title>Apply for a Loan | SmartBank Connect</title>
    <jsp:include page="../common/app-head.jsp"/>
</head>
<body>
<div class="sb-shell">
    <jsp:include page="../common/sidebar.jsp"/>
    <div class="sb-main">
        <div class="sb-topbar"><h5 class="mb-0"><i class="bi bi-bank me-2"></i>Apply for a Loan</h5></div>
        <div class="sb-content">
            <c:if test="${not empty error}">
                <div class="alert alert-danger py-2 mb-3 text-danger" style="max-width: 560px;">
                    <i class="bi bi-exclamation-triangle-fill me-2"></i><strong>Error:</strong> <c:out value="${error}"/>
                </div>
            </c:if>
            <c:if test="${not empty success}">
                <div class="alert alert-success py-2 mb-3" style="max-width: 560px;">
                    <i class="bi bi-check-circle-fill me-2"></i><c:out value="${success}"/>
                </div>
            </c:if>

            <div class="sb-card" style="max-width: 560px;">
                <form id="loanForm" action="<c:url value='/loans/apply'/>" method="post" novalidate>
                    <c:if test="${not empty _csrf}"><input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" /></c:if>
                    
                    <div class="mb-3">
                        <label class="form-label" for="loanType">Loan Type <span class="text-danger">*</span></label>
                        <select name="loanType" id="loanType" class="form-select" required>
                            <option value="PERSONAL">Personal Loan</option>
                            <option value="HOME">Home Loan</option>
                            <option value="VEHICLE">Vehicle Loan</option>
                            <option value="EDUCATION">Education Loan</option>
                        </select>
                        <div class="invalid-feedback text-danger" id="loanTypeError">Please select a loan type.</div>
                    </div>

                    <div class="mb-3">
                        <label class="form-label" for="amount">Requested Amount (Rs.) <span class="text-danger">*</span></label>
                        <input type="number" step="0.01" min="10000" max="50000000" name="amount" id="amount" class="form-control" placeholder="e.g. 500000" required>
                        <div class="invalid-feedback text-danger" id="amountError">Loan amount must be between Rs. 10,000 and Rs. 50,000,000.</div>
                        <small class="form-text text-muted">Min: Rs. 10,000 | Max: Rs. 50,000,000</small>
                    </div>

                    <div class="mb-3">
                        <label class="form-label" for="monthlyIncome">Monthly Income (Rs.) <span class="text-danger">*</span></label>
                        <input type="number" step="0.01" min="1000" name="monthlyIncome" id="monthlyIncome" class="form-control" placeholder="e.g. 75000" required>
                        <div class="invalid-feedback text-danger" id="incomeError">Monthly income must be a positive amount (min Rs. 1,000).</div>
                    </div>

                    <div class="mb-3">
                        <label class="form-label" for="employmentStatus">Employment Status <span class="text-danger">*</span></label>
                        <input type="text" name="employmentStatus" id="employmentStatus" class="form-control" placeholder="e.g. Permanent, Self-Employed" minlength="2" maxlength="100" required>
                        <div class="invalid-feedback text-danger" id="empError">Employment status is required.</div>
                    </div>

                    <div class="mb-3">
                        <label class="form-label" for="documentsSummary">Supporting Documents Summary <span class="text-danger">*</span></label>
                        <textarea name="documentsSummary" id="documentsSummary" class="form-control" rows="3"
                                  placeholder="List documents you can provide (e.g. 3 months payslips, billing proof, NIC copy)" minlength="5" maxlength="500" required></textarea>
                        <div class="invalid-feedback text-danger" id="docError">Please provide a brief summary of supporting documents (min 5 characters).</div>
                    </div>

                    <button type="submit" class="btn btn-gold w-100 py-2">
                        <i class="bi bi-send me-2"></i>Submit Application
                    </button>
                </form>
            </div>
        </div>
    </div>
</div>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
<script>
document.getElementById('loanForm').addEventListener('submit', function(e) {
    let valid = true;
    const amount = document.getElementById('amount');
    const income = document.getElementById('monthlyIncome');
    const emp = document.getElementById('employmentStatus');
    const doc = document.getElementById('documentsSummary');

    const amt = parseFloat(amount.value);
    if (isNaN(amt) || amt < 10000 || amt > 50000000) {
        amount.classList.add('is-invalid');
        valid = false;
    } else {
        amount.classList.remove('is-invalid');
    }

    const inc = parseFloat(income.value);
    if (isNaN(inc) || inc <= 0) {
        income.classList.add('is-invalid');
        valid = false;
    } else {
        income.classList.remove('is-invalid');
    }

    if (emp.value.trim().length < 2) {
        emp.classList.add('is-invalid');
        valid = false;
    } else {
        emp.classList.remove('is-invalid');
    }

    if (doc.value.trim().length < 5) {
        doc.classList.add('is-invalid');
        valid = false;
    } else {
        doc.classList.remove('is-invalid');
    }

    if (!valid) {
        e.preventDefault();
        return false;
    }

    if (!confirm('Submit loan application for Rs. ' + amt.toLocaleString() + '?')) {
        e.preventDefault();
        return false;
    }
});
</script>
</body>
</html>
