<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="pageTitle" value="Seller Dashboard — ShopEase" scope="request"/>
<jsp:include page="/WEB-INF/views/common/header.jsp"/>

<div class="container" style="margin-top: 30px;">
    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px;">
        <div>
            <h1 style="font-size: 26px; font-weight: 800;">Seller Dashboard</h1>
            <p style="color: var(--gray-500); font-size: 14px;">Welcome back, <c:out value="${sessionScope.currentUser.name}"/>! Here is your performance overview.</p>
        </div>
        <div style="display: flex; gap: 10px;">
            <a href="${pageContext.request.contextPath}/seller/products" class="btn btn-primary">+ Manage Products</a>
            <a href="${pageContext.request.contextPath}/seller/orders" class="btn btn-secondary">📦 Incoming Orders</a>
        </div>
    </div>

    <!-- Metric Cards Grid -->
    <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(220px, 1fr)); gap: 20px; margin-bottom: 30px;">
        <div class="card" style="border-left: 4px solid var(--primary);">
            <span style="font-size: 13px; color: var(--gray-500); font-weight: 600;">TOTAL REVENUE</span>
            <h2 style="font-size: 26px; font-weight: 800; color: var(--primary-dark); margin-top: 6px;"><c:out value="${stats.formattedTotalRevenue}"/></h2>
        </div>

        <div class="card" style="border-left: 4px solid var(--secondary);">
            <span style="font-size: 13px; color: var(--gray-500); font-weight: 600;">ACTIVE PRODUCTS</span>
            <h2 style="font-size: 26px; font-weight: 800; color: var(--secondary); margin-top: 6px;"><c:out value="${stats.totalProducts}"/></h2>
        </div>

        <div class="card" style="border-left: 4px solid var(--accent);">
            <span style="font-size: 13px; color: var(--gray-500); font-weight: 600;">LOW STOCK ALERTS</span>
            <h2 style="font-size: 26px; font-weight: 800; color: #b45309; margin-top: 6px;"><c:out value="${stats.lowStockProducts}"/></h2>
        </div>

        <div class="card" style="border-left: 4px solid var(--danger);">
            <span style="font-size: 13px; color: var(--gray-500); font-weight: 600;">OUT OF STOCK</span>
            <h2 style="font-size: 26px; font-weight: 800; color: var(--danger); margin-top: 6px;"><c:out value="${stats.outOfStockProducts}"/></h2>
        </div>
    </div>

    <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 30px;">
        <!-- Recent Orders -->
        <div class="card">
            <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px;">
                <h3 style="font-size: 18px; font-weight: 700;">Recent Customer Orders</h3>
                <a href="${pageContext.request.contextPath}/seller/orders" style="font-size: 13px; color: var(--primary-dark); font-weight: 600;">View All ➔</a>
            </div>

            <c:choose>
                <c:when test="${empty orders}">
                    <p style="color: var(--gray-500); font-size: 14px;">No incoming orders yet.</p>
                </c:when>
                <c:otherwise>
                    <div style="display: flex; flex-direction: column; gap: 12px;">
                        <c:forEach var="ord" items="${orders}">
                            <div style="display: flex; justify-content: space-between; align-items: center; border-bottom: 1px solid var(--gray-200); padding-bottom: 10px; font-size: 14px;">
                                <div>
                                    <strong>Order #<c:out value="${ord.id}"/></strong> &bull; <c:out value="${ord.customerName}"/>
                                    <div style="font-size: 12px; color: var(--gray-500);"><c:out value="${ord.createdAt}"/></div>
                                </div>
                                <div style="text-align: right;">
                                    <span class="badge ${ord.status == 'DELIVERED' ? 'badge-primary' : 'badge-accent'}"><c:out value="${ord.status}"/></span>
                                    <div style="font-weight: 700; margin-top: 2px;"><c:out value="${ord.formattedTotalAmount}"/></div>
                                </div>
                            </div>
                        </c:forEach>
                    </div>
                </c:otherwise>
            </c:choose>
        </div>

        <!-- Catalog Highlights -->
        <div class="card">
            <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px;">
                <h3 style="font-size: 18px; font-weight: 700;">Your Listed Products</h3>
                <a href="${pageContext.request.contextPath}/seller/products" style="font-size: 13px; color: var(--primary-dark); font-weight: 600;">Manage All ➔</a>
            </div>

            <c:choose>
                <c:when test="${empty products}">
                    <p style="color: var(--gray-500); font-size: 14px;">No products listed yet.</p>
                </c:when>
                <c:otherwise>
                    <div style="display: flex; flex-direction: column; gap: 12px;">
                        <c:forEach var="prod" items="${products}">
                            <div style="display: flex; justify-content: space-between; align-items: center; border-bottom: 1px solid var(--gray-200); padding-bottom: 10px; font-size: 14px;">
                                <div>
                                    <strong style="color: var(--dark);"><c:out value="${prod.name}"/></strong>
                                    <div style="font-size: 12px; color: var(--gray-500);"><c:out value="${prod.category}"/> &bull; Stock: <c:out value="${prod.stockQty}"/> units</div>
                                </div>
                                <div>
                                    <strong style="color: var(--primary-dark);"><c:out value="${prod.formattedPrice}"/></strong>
                                </div>
                            </div>
                        </c:forEach>
                    </div>
                </c:otherwise>
            </c:choose>
        </div>
    </div>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp"/>
