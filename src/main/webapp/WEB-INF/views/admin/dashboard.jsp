<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="pageTitle" value="Admin Console — ShopEase" scope="request"/>
<jsp:include page="/WEB-INF/views/common/header.jsp"/>

<div class="container" style="margin-top: 30px;">
    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px;">
        <div>
            <h1 style="font-size: 26px; font-weight: 800;">Administrator Console 🛡️</h1>
            <p style="color: var(--gray-500); font-size: 14px;">System governance, user registry, and catalog moderation</p>
        </div>
        <div style="display: flex; gap: 10px;">
            <a href="${pageContext.request.contextPath}/admin/users" class="btn btn-secondary">👥 User Registry</a>
            <a href="${pageContext.request.contextPath}/admin/products" class="btn btn-secondary">📦 Catalog Moderation</a>
            <a href="${pageContext.request.contextPath}/admin/orders" class="btn btn-secondary">📜 All Orders</a>
        </div>
    </div>

    <!-- Metrics Cards -->
    <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(220px, 1fr)); gap: 20px; margin-bottom: 30px;">
        <div class="card" style="border-left: 4px solid var(--primary);">
            <span style="font-size: 13px; color: var(--gray-500); font-weight: 600;">TOTAL USERS</span>
            <h2 style="font-size: 26px; font-weight: 800; color: var(--primary-dark); margin-top: 6px;"><c:out value="${totalUsers}"/></h2>
        </div>

        <div class="card" style="border-left: 4px solid var(--secondary);">
            <span style="font-size: 13px; color: var(--gray-500); font-weight: 600;">CATALOG PRODUCTS</span>
            <h2 style="font-size: 26px; font-weight: 800; color: var(--secondary); margin-top: 6px;"><c:out value="${totalProducts}"/></h2>
        </div>

        <div class="card" style="border-left: 4px solid var(--accent);">
            <span style="font-size: 13px; color: var(--gray-500); font-weight: 600;">TOTAL ORDERS</span>
            <h2 style="font-size: 26px; font-weight: 800; color: #b45309; margin-top: 6px;"><c:out value="${totalOrders}"/></h2>
        </div>
    </div>

    <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 30px;">
        <!-- Registered Users -->
        <div class="card">
            <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px;">
                <h3 style="font-size: 18px; font-weight: 700;">Recent User Registrations</h3>
                <a href="${pageContext.request.contextPath}/admin/users" style="font-size: 13px; color: var(--primary-dark); font-weight: 600;">View All Users ➔</a>
            </div>

            <div style="display: flex; flex-direction: column; gap: 12px;">
                <c:forEach var="u" items="${recentUsers}">
                    <div style="display: flex; justify-content: space-between; align-items: center; border-bottom: 1px solid var(--gray-200); padding-bottom: 10px; font-size: 14px;">
                        <div>
                            <strong><c:out value="${u.name}"/></strong>
                            <div style="font-size: 12px; color: var(--gray-500);"><c:out value="${u.email}"/></div>
                        </div>
                        <div>
                            <span class="badge ${u.role == 'ADMIN' ? 'badge-accent' : (u.role == 'SELLER' ? 'badge-primary' : 'badge-secondary')}">
                                <c:out value="${u.role}"/>
                            </span>
                        </div>
                    </div>
                </c:forEach>
            </div>
        </div>

        <!-- Recent System Orders -->
        <div class="card">
            <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px;">
                <h3 style="font-size: 18px; font-weight: 700;">Recent System Orders</h3>
                <a href="${pageContext.request.contextPath}/admin/orders" style="font-size: 13px; color: var(--primary-dark); font-weight: 600;">View All Orders ➔</a>
            </div>

            <div style="display: flex; flex-direction: column; gap: 12px;">
                <c:forEach var="ord" items="${recentOrders}">
                    <div style="display: flex; justify-content: space-between; align-items: center; border-bottom: 1px solid var(--gray-200); padding-bottom: 10px; font-size: 14px;">
                        <div>
                            <strong>Order #<c:out value="${ord.id}"/></strong> &bull; <c:out value="${ord.customerName}"/>
                            <div style="font-size: 12px; color: var(--gray-500);"><c:out value="${ord.buyerEmail}"/></div>
                        </div>
                        <div style="text-align: right;">
                            <span class="badge ${ord.status == 'DELIVERED' ? 'badge-primary' : 'badge-accent'}"><c:out value="${ord.status}"/></span>
                            <div style="font-weight: 700; color: var(--primary-dark); margin-top: 2px;"><c:out value="${ord.formattedTotalAmount}"/></div>
                        </div>
                    </div>
                </c:forEach>
            </div>
        </div>
    </div>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp"/>
