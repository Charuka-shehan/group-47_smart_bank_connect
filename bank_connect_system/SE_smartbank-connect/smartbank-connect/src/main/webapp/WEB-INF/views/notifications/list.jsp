<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <title>Notifications | SmartBank Connect</title>
    <jsp:include page="../common/app-head.jsp"/>
</head>
<body>
<div class="sb-shell">
    <jsp:include page="../common/sidebar.jsp"/>
    <div class="sb-main">
        <div class="sb-topbar"><h5 class="mb-0">Notifications</h5></div>
        <div class="sb-content">
            <c:if test="${not empty success}"><div class="alert alert-success py-2 mb-3">${success}</div></c:if>
            <c:if test="${not empty error}"><div class="alert alert-danger py-2 mb-3">${error}</div></c:if>

            <div class="row g-4">
                <div class="col-lg-8">
                    <div class="sb-card p-0">
                        <div class="px-4 py-3 border-bottom d-flex justify-content-between align-items-center">
                            <h6 class="mb-0"><i class="bi bi-bell text-primary me-2"></i>Inbox</h6>
                            <span class="badge bg-secondary">${notifications.size()} Total</span>
                        </div>
                        <c:forEach var="n" items="${notifications}" varStatus="st">
                            <div class="d-flex justify-content-between align-items-start px-4 py-3 ${st.index != notifications.size()-1 ? 'border-bottom' : ''}"
                                 style="${n.read ? '' : 'background:#fdf8ec;'}">
                                <div>
                                    <div class="d-flex align-items-center gap-2">
                                        <i class="bi
                                            ${n.channel == 'SMS' ? 'bi-chat-dots' : n.channel == 'EMAIL' ? 'bi-envelope' : 'bi-bell'}"
                                           style="color: var(--sb-gold);"></i>
                                        <strong>${n.title}</strong>
                                        <c:if test="${!n.read}"><span class="badge bg-danger">New</span></c:if>
                                    </div>
                                    <p class="mb-1 mt-1 text-muted small">${n.message}</p>
                                    <span class="text-muted" style="font-size: 0.75rem;">${n.createdAt}</span>
                                </div>
                                <div class="d-flex align-items-center gap-1">
                                    <c:if test="${!n.read}">
                                        <form action="<c:url value='/notifications/${n.id}/read'/>" method="post" class="d-inline">
                                            <c:if test="${not empty _csrf}">
                                                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />
                                            </c:if>
                                            <button type="submit" class="btn btn-sm btn-outline-secondary">Mark Read</button>
                                        </form>
                                    </c:if>
                                    <form action="<c:url value='/notifications/${n.id}/delete'/>" method="post" class="d-inline"
                                          onsubmit="return confirm('Delete this notification?');">
                                        <c:if test="${not empty _csrf}">
                                            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />
                                        </c:if>
                                        <button type="submit" class="btn btn-sm btn-outline-danger" title="Delete"><i class="bi bi-trash"></i></button>
                                    </form>
                                </div>
                            </div>
                        </c:forEach>
                        <c:if test="${empty notifications}">
                            <div class="text-muted text-center py-5">No notifications yet.</div>
                        </c:if>
                    </div>
                </div>

                <div class="col-lg-4">
                    <div class="sb-card">
                        <h6 class="mb-3"><i class="bi bi-sliders text-primary me-2"></i>Notification Preferences</h6>
                        <p class="text-muted small">Manage which communication channels you prefer for account alerts.</p>
                        <form action="<c:url value='/notifications/preferences'/>" method="post">
                            <c:if test="${not empty _csrf}">
                                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />
                            </c:if>
                            <div class="form-check form-switch mb-3">
                                <input class="form-check-input" type="checkbox" id="emailPref" name="emailNotifications" value="true"
                                       ${preferences != null && preferences.emailNotifications ? 'checked' : ''}>
                                <label class="form-check-label" for="emailPref">Email Notifications</label>
                            </div>
                            <div class="form-check form-switch mb-3">
                                <input class="form-check-input" type="checkbox" id="smsPref" name="smsNotifications" value="true"
                                       ${preferences != null && preferences.smsNotifications ? 'checked' : ''}>
                                <label class="form-check-label" for="smsPref">SMS Notifications</label>
                            </div>
                            <div class="form-check form-switch mb-4">
                                <input class="form-check-input" type="checkbox" id="inAppPref" name="inAppNotifications" value="true"
                                       ${preferences != null && preferences.inAppNotifications ? 'checked' : ''}>
                                <label class="form-check-label" for="inAppPref">In-App Alerts</label>
                            </div>
                            <button type="submit" class="btn btn-primary btn-sm w-100"><i class="bi bi-check2 me-1"></i>Save Preferences</button>
                        </form>
                    </div>
                </div>
            </div>
    </div>
</div>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
