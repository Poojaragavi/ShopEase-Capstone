<%@ page contentType="text/html;charset=UTF-8" language="java" isErrorPage="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="pageTitle" value="500 Internal Server Error — ShopEase" scope="request"/>
<jsp:include page="/WEB-INF/views/common/header.jsp"/>

<div class="container" style="max-width: 600px; margin: 60px auto; text-align: center;">
    <div class="card" style="padding: 48px 24px;">
        <div style="font-size: 54px; margin-bottom: 16px;">⚠️</div>
        <h1 style="font-size: 28px; font-weight: 800; color: var(--danger); margin-bottom: 12px;">500 — Server Error</h1>
        <p style="color: var(--gray-500); margin-bottom: 24px;">
            An unexpected error occurred while processing your request. Our engineering team has been notified.
        </p>
        <a href="${pageContext.request.contextPath}/home" class="btn btn-primary">Return to ShopEase Home</a>
    </div>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp"/>
