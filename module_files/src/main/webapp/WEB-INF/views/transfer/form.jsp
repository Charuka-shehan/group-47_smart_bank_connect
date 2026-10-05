<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Fund Transfer | SmartBank Connect</title>
    <jsp:include page="../common/app-head.jsp"/>
</head>
<body>
<div class="sb-shell">
    <jsp:include page="../common/sidebar.jsp"/>
    <div class="sb-main">
        <div class="sb-topbar"><h5 class="mb-0">Fund Transfer</h5></div>
        <div class="sb-content">
            <div class="sb-card" style="max-width: 560px;">
                <div class="alert alert-warning py-2 mb-3">
                    <i class="bi bi-info-circle-fill me-2"></i>
                    <strong>DEMO MODE:</strong> Simulated fund transfer for demonstration purposes only.
                </div>
                <c:if test="${not empty error}">
                    <div class="alert alert-danger py-2">
                        <i class="bi bi-exclamation-circle me-2"></i>
                        <strong>Error:</strong> ${error}
                    </div>
                </c:if>

                <form id="transferForm" action="<c:url value='/transfer'/>" method="post">
                    <c:if test="${not empty _csrf}">
                        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />
                    </c:if>

                    <div class="mb-3">
                        <label class="form-label">Select Account <span class="text-danger">*</span></label>
                        <select name="sourceAccountId" id="sourceAccountId" class="form-select" required>
                            <option value="">-- Select Account --</option>
                            <c:forEach var="a" items="${accounts}">
                                <option value="${a.id}">
                                    ${a.accountNumber} — ${a.accountType}
                                    (Rs. <fmt:formatNumber value="${a.balance}" maxFractionDigits="2"/>)<c:if test="${not empty a.status && a.status != 'ACTIVE'}"> [${a.status}]</c:if>
                                </option>
                            </c:forEach>
                        </select>
                        <div class="invalid-feedback text-danger" id="sourceAccountError">Please select an active source account.</div>
                        <small class="form-text text-muted">Select an account with sufficient balance</small>
                    </div>

                    <div class="mb-3">
                        <label class="form-label">Destination Account Number / ID (optional)</label>
                        <input type="text" name="destinationAccountId" id="destinationAccountId" 
                               class="form-control" placeholder="e.g. LTB000123456 or leave blank for external">
                        <small class="form-text text-muted">
                            For internal transfers within LankaTrust Bank, enter the destination account number (e.g. LTB...) or ID. 
                            Leave blank to transfer to an external beneficiary.
                        </small>
                    </div>

                    <c:if test="${not empty beneficiaries}">
                        <div class="mb-3">
                            <label class="form-label">Saved Beneficiaries</label>
                            <select id="savedBeneficiarySelect" class="form-select">
                                <option value="">-- Choose from saved beneficiaries (optional) --</option>
                                <c:forEach var="b" items="${beneficiaries}">
                                    <option value="${b.id}" data-name="${b.beneficiaryName}" data-account="${b.beneficiaryAccountNumber}">
                                        ${b.beneficiaryName} (${b.beneficiaryAccountNumber}<c:if test="${not empty b.beneficiaryBank}"> - ${b.beneficiaryBank}</c:if>)<c:if test="${not empty b.nickname}"> [${b.nickname}]</c:if>
                                    </option>
                                </c:forEach>
                            </select>
                            <small class="form-text text-muted">Selecting a saved beneficiary auto-fills destination details below.</small>
                        </div>
                    </c:if>

                    <div class="mb-3">
                        <label class="form-label">Beneficiary Name <span class="text-danger">*</span></label>
                        <input type="text" name="beneficiaryName" id="beneficiaryName"
                               class="form-control" placeholder="Full name of beneficiary"
                               minlength="3" maxlength="100" required>
                        <div class="invalid-feedback text-danger" id="beneficiaryError">Beneficiary name is required (3-100 characters).</div>
                        <small class="form-text text-muted">Beneficiary name is required for all transfers</small>
                    </div>

                    <div class="mb-3">
                        <label class="form-label">Amount (Rs.) <span class="text-danger">*</span></label>
                        <div class="input-group">
                            <span class="input-group-text">Rs.</span>
                            <input type="number" name="amount" id="amount" class="form-control"
                                   step="0.01" min="10.00" max="999999999.99"
                                   placeholder="0.00" required>
                        </div>
                        <div class="invalid-feedback text-danger d-block" id="amountError" style="display:none !important;">Minimum transfer amount is Rs. 10.00.</div>
                        <div class="mt-2">
                            <small class="form-text text-muted">
                                Minimum: Rs. 10.00 | 
                                Transfers above Rs. <fmt:formatNumber value="${otpThreshold}"/> require OTP verification
                            </small>
                            <div id="amountWarning" class="alert alert-warning py-2 mt-2" style="display:none;">
                                <i class="bi bi-shield-check me-2"></i>
                                <strong>OTP Required:</strong> This transfer requires email OTP verification for security. Money will only be transferred after verification.
                            </div>
                        </div>
                    </div>

                    <div class="mb-3">
                        <label class="form-label">Remarks / Description (optional)</label>
                        <input type="text" name="remarks" class="form-control"
                               placeholder="Optional note for reference"
                               maxlength="200">
                        <small class="form-text text-muted">Max 200 characters</small>
                    </div>

                    <div class="mb-3" id="summaryCard" style="display:none;">
                        <div class="alert alert-light border py-3">
                            <h6 class="mb-3">Transfer Summary</h6>
                            <div class="row mb-2">
                                <div class="col-6"><small class="text-muted">From Account:</small></div>
                                <div class="col-6"><small id="summaryFrom">-</small></div>
                            </div>
                            <div class="row mb-2">
                                <div class="col-6"><small class="text-muted">To Beneficiary:</small></div>
                                <div class="col-6"><small id="summaryBeneficiary">-</small></div>
                            </div>
                            <div class="row mb-2">
                                <div class="col-6"><small class="text-muted">Amount:</small></div>
                                <div class="col-6"><small id="summaryAmount" style="font-weight:600;">-</small></div>
                            </div>
                            <div class="row">
                                <div class="col-6"><small class="text-muted">Security:</small></div>
                                <div class="col-6"><small id="summarySecurity">-</small></div>
                            </div>
                        </div>
                    </div>

                    <button type="submit" class="btn btn-gold w-100 py-2">
                        <i class="bi bi-arrow-left-right me-2"></i>Transfer Funds
                    </button>
                </form>
            </div>
        </div>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
<script>
    const otpThreshold = ${otpThreshold};
    const form = document.getElementById('transferForm');
    const amountInput = document.getElementById('amount');
    const amountWarning = document.getElementById('amountWarning');
    const sourceAccountSelect = document.getElementById('sourceAccountId');
    const beneficiaryInput = document.getElementById('beneficiaryName');
    const destinationInput = document.getElementById('destinationAccountId');
    const savedBeneficiarySelect = document.getElementById('savedBeneficiarySelect');
    const summaryCard = document.getElementById('summaryCard');

    if (savedBeneficiarySelect) {
        savedBeneficiarySelect.addEventListener('change', function() {
            const opt = this.options[this.selectedIndex];
            if (opt && opt.value) {
                const benName = opt.getAttribute('data-name');
                const benAcc = opt.getAttribute('data-account');
                if (benName) beneficiaryInput.value = benName;
                if (benAcc && destinationInput) destinationInput.value = benAcc;
                beneficiaryInput.classList.remove('is-invalid');
                updateSummary();
            }
        });
    }

    // Validate and show/hide OTP warning
    amountInput.addEventListener('change', function() {
        if (this.value && parseFloat(this.value) > otpThreshold) {
            amountWarning.style.display = 'block';
        } else {
            amountWarning.style.display = 'none';
        }
        updateSummary();
    });

    // Update summary
    function updateSummary() {
        const amount = amountInput.value;
        const sourceId = sourceAccountSelect.value;
        const beneficiary = beneficiaryInput.value;

        if (amount && sourceId && beneficiary) {
            document.getElementById('summaryFrom').textContent = sourceAccountSelect.options[sourceAccountSelect.selectedIndex].text;
            document.getElementById('summaryBeneficiary').textContent = beneficiary;
            document.getElementById('summaryAmount').textContent = 'Rs. ' + parseFloat(amount).toFixed(2);
            document.getElementById('summarySecurity').innerHTML = parseFloat(amount) > otpThreshold 
                ? '<span class="text-success"><i class="bi bi-shield-check"></i> Email OTP Required</span>'
                : '<span class="text-muted">No additional verification</span>';
            summaryCard.style.display = 'block';
        } else {
            summaryCard.style.display = 'none';
        }
    }

    sourceAccountSelect.addEventListener('change', updateSummary);
    beneficiaryInput.addEventListener('input', updateSummary);

    // Form validation before submit
    form.addEventListener('submit', function(e) {
        let valid = true;

        if (!sourceAccountSelect.value) {
            sourceAccountSelect.classList.add('is-invalid');
            valid = false;
        } else {
            sourceAccountSelect.classList.remove('is-invalid');
        }

        const ben = beneficiaryInput.value.trim();
        if (!ben || ben.length < 3) {
            beneficiaryInput.classList.add('is-invalid');
            valid = false;
        } else {
            beneficiaryInput.classList.remove('is-invalid');
        }

        const amt = parseFloat(amountInput.value);
        const amtErr = document.getElementById('amountError');
        if (isNaN(amt) || amt < 10) {
            amountInput.classList.add('is-invalid');
            amtErr.style.setProperty('display', 'block', 'important');
            valid = false;
        } else {
            amountInput.classList.remove('is-invalid');
            amtErr.style.setProperty('display', 'none', 'important');
        }

        if (!valid) {
            e.preventDefault();
            return false;
        }

        const confirmMsg = "Are you sure you want to transfer Rs. " + amt.toFixed(2) + " to " + ben + "?";
        if (!confirm(confirmMsg)) {
            e.preventDefault();
            return false;
        }
    });

    // Initialize
    updateSummary();
</script>
</body>
</html>
