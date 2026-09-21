<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="pageTitle" value="Seller Orders — ShopEase" scope="request"/>
<jsp:include page="/WEB-INF/views/common/header.jsp"/>

<div class="container" style="margin-top: 30px;">
    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px;">
        <div>
            <h1 style="font-size: 26px; font-weight: 800;">Incoming Customer Orders</h1>
            <p style="color: var(--gray-500); font-size: 14px;">Manage shipments and update delivery milestones</p>
        </div>
        <a href="${pageContext.request.contextPath}/seller/dashboard" class="btn btn-secondary">Dashboard</a>
    </div>

    <c:if test="${not empty sessionScope.successMessage}">
        <div class="alert alert-success"><c:out value="${sessionScope.successMessage}"/></div>
        <c:remove var="successMessage" scope="session"/>
    </c:if>
    <c:if test="${not empty sessionScope.errorMessage}">
        <div class="alert alert-danger"><c:out value="${sessionScope.errorMessage}"/></div>
        <c:remove var="errorMessage" scope="session"/>
    </c:if>

    <c:choose>
        <c:when test="${empty orders}">
            <div class="card" style="text-align: center; padding: 48px 20px;">
                <h3>No Incoming Orders</h3>
                <p style="color: var(--gray-500); margin-top: 8px;">Orders placed by buyers containing your products will appear here.</p>
            </div>
        </c:when>
        <c:otherwise>
            <div style="display: flex; flex-direction: column; gap: 20px;">
                <c:forEach var="ord" items="${orders}">
                    <div class="card">
                        <div style="display: flex; justify-content: space-between; align-items: center; border-bottom: 1px solid var(--gray-200); padding-bottom: 12px; margin-bottom: 14px;">
                            <div>
                                <strong style="font-size: 16px;">Order #<c:out value="${ord.id}"/></strong> &bull;
                                <span style="color: var(--gray-500); font-size: 13px;"><c:out value="${ord.createdAt}"/></span>
                            </div>
                            <div>
                                <span class="badge ${ord.status == 'DELIVERED' ? 'badge-primary' : (ord.status == 'CANCELLED' ? 'badge-danger' : 'badge-accent')}">
                                    <c:out value="${ord.status}"/>
                                </span>
                            </div>
                        </div>

                        <div style="display: grid; grid-template-columns: 2fr 1fr; gap: 20px; align-items: start;">
                            <div>
                                <h4 style="font-size: 14px; font-weight: 700; margin-bottom: 8px;">Items Ordered:</h4>
                                <ul style="list-style: none; display: flex; flex-direction: column; gap: 6px; font-size: 14px;">
                                    <c:forEach var="item" items="${ord.items}">
                                        <li>&bull; <c:out value="${item.productName}"/> &times; <strong><c:out value="${item.quantity}"/></strong> (<c:out value="${item.formattedSubtotal}"/>)</li>
                                    </c:forEach>
                                </ul>

                                <div style="margin-top: 12px; font-size: 13px; color: var(--gray-700);">
                                    <strong>Ship to:</strong> <c:out value="${ord.customerName}"/>, <c:out value="${ord.address}"/>, <c:out value="${ord.city}"/> - <c:out value="${ord.pincode}"/> (Ph: <c:out value="${ord.phone}"/>)
                                </div>
                            </div>

                            <div style="background: var(--gray-100); padding: 14px; border-radius: var(--radius);">
                                <h4 style="font-size: 13px; font-weight: 700; margin-bottom: 8px;">Update Order Status</h4>

                                <form action="${pageContext.request.contextPath}/seller/order/update" method="post" style="display: flex; flex-direction: column; gap: 8px;">
                                    <input type="hidden" name="orderId" value="${ord.id}">
                                    <select name="status" class="form-control" style="font-size: 13px; padding: 6px 10px;">
                                        <option value="CONFIRMED" ${ord.status == 'CONFIRMED' ? 'selected' : ''}>CONFIRMED</option>
                                        <option value="SHIPPED" ${ord.status == 'SHIPPED' ? 'selected' : ''}>SHIPPED</option>
                                        <option value="DELIVERED" ${ord.status == 'DELIVERED' ? 'selected' : ''}>DELIVERED</option>
                                        <option value="CANCELLED" ${ord.status == 'CANCELLED' ? 'selected' : ''}>CANCELLED</option>
                                    </select>
                                    <button type="submit" class="btn btn-primary" style="padding: 6px 12px; font-size: 12px;">Update Status</button>
                                </form>
                            </div>
                        </div>
                    </div>
                </c:forEach>
            </div>
        </c:otherwise>
    </c:choose>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp"/>
