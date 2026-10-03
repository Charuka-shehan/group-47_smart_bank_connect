<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Customer Search" scope="request"/>
<jsp:include page="../common/app-frame-start.jsp"/>

<c:if test="${not empty success}"><div class="alert alert-success py-2 mb-3">${success}</div></c:if>
<c:if test="${not empty error}"><div class="alert alert-danger py-2 mb-3">${error}</div></c:if>

<div class="d-flex justify-content-between align-items-center mb-3">
    <h6 class="mb-0">Customer Record Directory</h6>
    <button type="button" class="btn btn-sm btn-primary" data-bs-toggle="modal" data-bs-target="#newCustomerModal">
        <i class="bi bi-person-plus me-1"></i>New Customer Record
    </button>
</div>

<!-- New Customer Modal -->
<div class="modal fade" id="newCustomerModal" tabindex="-1" aria-hidden="true">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <h6 class="modal-title">Create New Customer Record</h6>
                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
            </div>
            <form method="post" action="<c:url value='/officer/customers/create'/>">
                <c:if test="${not empty _csrf}"><input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" /></c:if>
                <div class="modal-body">
                    <div class="mb-3">
                        <label class="form-label">Full Name <span class="text-danger">*</span></label>
                        <input class="form-control" name="fullName" required placeholder="e.g. John Doe">
                    </div>
                    <div class="mb-3">
                        <label class="form-label">Email Address <span class="text-danger">*</span></label>
                        <input type="email" class="form-control" name="email" required placeholder="e.g. john@example.com">
                    </div>
                    <div class="mb-3">
                        <label class="form-label">Mobile Number <span class="text-danger">*</span></label>
                        <input type="tel" class="form-control" name="phone" required placeholder="e.g. 0771234567">
                    </div>
                    <div class="mb-3">
                        <label class="form-label">NIC / National ID <span class="text-danger">*</span></label>
                        <input class="form-control" name="nic" required placeholder="e.g. 199012345678">
                    </div>
                    <div class="mb-3">
                        <label class="form-label">Date of Birth <span class="text-danger">*</span></label>
                        <input type="date" class="form-control" name="dob" required>
                    </div>
                    <div class="mb-3">
                        <label class="form-label">Residential Address <span class="text-danger">*</span></label>
                        <textarea class="form-control" name="address" rows="2" required placeholder="Street address, city"></textarea>
                    </div>
                    <div class="mb-3">
                        <label class="form-label">Branch</label>
                        <select name="branchId" class="form-select">
                            <option value="">-- Select Branch --</option>
                            <c:forEach var="b" items="${branches}">
                                <option value="${b.id}">${b.name}</option>
                            </c:forEach>
                        </select>
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-secondary btn-sm" data-bs-dismiss="modal">Cancel</button>
                    <button type="submit" class="btn btn-primary btn-sm">Create Customer</button>
                </div>
            </form>
        </div>
    </div>
</div>

<form class="sb-card mb-4 row g-2" method="get">
    <div class="col-md-9">
        <input class="form-control" name="q" value="${query}" placeholder="Search by name, email, NIC or mobile">
    </div>
    <div class="col-md-3"><button class="btn btn-primary w-100">Search</button></div>
</form>

<div class="sb-card">
    <div class="table-responsive">
        <table class="table align-middle">
            <thead><tr><th>Name</th><th>Email</th><th>NIC</th><th>Mobile</th><th></th></tr></thead>
            <tbody>
            <c:forEach var="cust" items="${customers}">
                <tr>
                    <td><c:out value="${cust.fullName}"/></td>
                    <td><c:out value="${cust.email}"/></td>
                    <td><c:out value="${cust.nic}"/></td>
                    <td><c:out value="${cust.phoneNumber}"/></td>
                    <td><a class="btn btn-sm btn-outline-primary" href="<c:url value='/officer/customers/${cust.id}'/>">View</a></td>
                </tr>
            </c:forEach>
            <c:if test="${empty customers}"><tr><td colspan="5" class="text-muted text-center py-3">No customers found.</td></tr></c:if>
            </tbody>
        </table>
    </div>
</div>

<jsp:include page="../common/app-frame-end.jsp"/>
