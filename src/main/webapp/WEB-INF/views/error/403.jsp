<%@ page contentType="text/html;charset=UTF-8" language="java" isErrorPage="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="pageTitle" value="403 Forbidden — ShopEase" scope="request"/>
<jsp:include page="/WEB-INF/views/common/header.jsp"/>

<div class="container" style="max-width: 600px; margin: 60px auto; text-align: center;">
    <div class="card" style="padding: 48px 24px;">
        <div style="font-size: 54px; margin-bottom: 16px;">🚫</div>
        <h1 style="font-size: 28px; font-weight: 800; color: var(--danger); margin-bottom: 12px;">403 — Access Forbidden</h1>
        <p style="color: var(--gray-500); margin-bottom: 24px;">
            <c:out value="${errorMessage != null ? errorMessage : 'You do not have required permissions to access this seller or admin resource.'}"/>
        </p>
        <div style="display: flex; gap: 12px; justify-content: center;">
            <a href="${pageContext.request.contextPath}/home" class="btn btn-primary">Return to Home</a>
            <a href="${pageContext.request.contextPath}/login" class="btn btn-secondary">Switch Account</a>
        </div>
    </div>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp"/>
