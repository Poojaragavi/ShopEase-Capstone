<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="pageTitle" value="My Profile — ShopEase" scope="request"/>
<jsp:include page="/WEB-INF/views/common/header.jsp"/>

<div class="container" style="max-width: 600px; margin: 40px auto;">
    <div class="card">
        <h2 style="font-size: 24px; font-weight: 800; margin-bottom: 20px;">Account Profile</h2>

        <div style="display: flex; flex-direction: column; gap: 16px;">
            <div style="display: flex; justify-content: space-between; border-bottom: 1px solid var(--gray-200); padding-bottom: 10px;">
                <span style="color: var(--gray-500);">Full Name:</span>
                <strong><c:out value="${user.name}"/></strong>
            </div>

            <div style="display: flex; justify-content: space-between; border-bottom: 1px solid var(--gray-200); padding-bottom: 10px;">
                <span style="color: var(--gray-500);">Email Address:</span>
                <strong><c:out value="${user.email}"/></strong>
            </div>

            <div style="display: flex; justify-content: space-between; border-bottom: 1px solid var(--gray-200); padding-bottom: 10px;">
                <span style="color: var(--gray-500);">Account Role:</span>
                <span class="badge ${user.role == 'SELLER' ? 'badge-primary' : (user.role == 'ADMIN' ? 'badge-accent' : 'badge-secondary')}">
                    <c:out value="${user.role}"/>
                </span>
            </div>

            <div style="display: flex; justify-content: space-between; border-bottom: 1px solid var(--gray-200); padding-bottom: 10px;">
                <span style="color: var(--gray-500);">Member Since:</span>
                <span><c:out value="${user.createdAt}"/></span>
            </div>
        </div>

        <div style="margin-top: 24px; display: flex; gap: 12px;">
            <c:if test="${user.role == 'BUYER'}">
                <a href="${pageContext.request.contextPath}/orders" class="btn btn-primary">My Orders</a>
                <a href="${pageContext.request.contextPath}/cart" class="btn btn-secondary">My Cart</a>
            </c:if>
            <c:if test="${user.role == 'SELLER'}">
                <a href="${pageContext.request.contextPath}/seller/dashboard" class="btn btn-primary">Seller Dashboard</a>
            </c:if>
            <c:if test="${user.role == 'ADMIN'}">
                <a href="${pageContext.request.contextPath}/admin/dashboard" class="btn btn-primary">Admin Console</a>
            </c:if>
        </div>
    </div>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp"/>
