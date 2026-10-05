<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Operational Reports" scope="request"/>
<jsp:include page="../common/app-frame-start.jsp"/>

<p class="text-muted">Preview in the browser or download. CSV is offered where available.
    <a class="btn btn-sm btn-gold float-end" href="<c:url value='/reports/draft'/>">Manage report drafts &amp; publishing &rarr;</a>
</p>
<div class="row g-3">
    <c:set var="items" value="${[
    ]}"/>
    <div class="col-md-4"><div class="sb-card"><h6>Daily transactions</h6>
        <a class="btn btn-sm btn-outline-danger" target="_blank" href="<c:url value='/reports/transactions/daily'/>">PDF</a>
        <a class="btn btn-sm btn-outline-success" href="<c:url value='/reports/transactions/daily/csv'/>">CSV</a></div></div>
    <div class="col-md-4"><div class="sb-card"><h6>Weekly transactions</h6>
        <a class="btn btn-sm btn-outline-danger" target="_blank" href="<c:url value='/reports/transactions/weekly'/>">PDF</a></div></div>
    <div class="col-md-4"><div class="sb-card"><h6>Monthly transactions</h6>
        <a class="btn btn-sm btn-outline-danger" target="_blank" href="<c:url value='/reports/transactions/monthly'/>">PDF</a></div></div>
    <div class="col-md-4"><div class="sb-card"><h6>Active accounts</h6>
        <a class="btn btn-sm btn-outline-danger" target="_blank" href="<c:url value='/reports/accounts/active'/>">PDF</a>
        <a class="btn btn-sm btn-outline-success" href="<c:url value='/reports/accounts/active/csv'/>">CSV</a></div></div>
    <div class="col-md-4"><div class="sb-card"><h6>Frozen accounts</h6>
        <a class="btn btn-sm btn-outline-danger" target="_blank" href="<c:url value='/reports/accounts/frozen'/>">PDF</a>
        <a class="btn btn-sm btn-outline-success" href="<c:url value='/reports/accounts/frozen/csv'/>">CSV</a></div></div>
    <div class="col-md-4"><div class="sb-card"><h6>Loan approvals</h6>
        <a class="btn btn-sm btn-outline-danger" target="_blank" href="<c:url value='/reports/loans/status'/>">PDF</a></div></div>
    <div class="col-md-4"><div class="sb-card"><h6>Branch performance</h6>
        <a class="btn btn-sm btn-outline-danger" target="_blank" href="<c:url value='/reports/branch/performance'/>">PDF</a></div></div>
    <div class="col-md-4"><div class="sb-card"><h6>Customer registrations</h6>
        <a class="btn btn-sm btn-outline-danger" target="_blank" href="<c:url value='/reports/customers/registrations'/>">PDF</a></div></div>
    <div class="col-md-4"><div class="sb-card"><h6>Transfer summary</h6>
        <a class="btn btn-sm btn-outline-danger" target="_blank" href="<c:url value='/reports/transfers/summary'/>">PDF</a></div></div>
    <div class="col-md-4"><div class="sb-card"><h6>Officer performance</h6>
        <a class="btn btn-sm btn-outline-danger" target="_blank" href="<c:url value='/reports/officer/performance'/>">PDF</a></div></div>
    <div class="col-md-4"><div class="sb-card"><h6>Audit activity</h6>
        <a class="btn btn-sm btn-outline-danger" target="_blank" href="<c:url value='/reports/audit/logs'/>">PDF</a>
        <a class="btn btn-sm btn-outline-success" href="<c:url value='/reports/audit/logs/csv'/>">CSV</a></div></div>
</div>

<jsp:include page="../common/app-frame-end.jsp"/>
