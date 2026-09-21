<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="pageTitle" value="Order Confirmation — ShopEase" scope="request"/>
<jsp:include page="/WEB-INF/views/common/header.jsp"/>

<div class="container" style="max-width: 650px; margin: 40px auto;">
    <div class="card" style="text-align: center; padding: 40px 30px;">
        <div style="font-size: 54px; margin-bottom: 16px;">🎉</div>
        <h1 style="font-size: 26px; font-weight: 800; color: var(--primary-dark); margin-bottom: 8px;">Order Successfully Placed!</h1>
        <p style="color: var(--gray-500); margin-bottom: 24px;">Thank you for shopping with ShopEase. Your order has been confirmed.</p>

        <div class="card" style="background: var(--light-bg); text-align: left; padding: 20px; margin-bottom: 24px;">
            <div style="display: flex; justify-content: space-between; margin-bottom: 10px; font-size: 14px;">
                <span style="color: var(--gray-500);">Order ID:</span>
                <strong>#<c:out value="${order.id}"/></strong>
            </div>
            <div style="display: flex; justify-content: space-between; margin-bottom: 10px; font-size: 14px;">
                <span style="color: var(--gray-500);">Status:</span>
                <span class="badge badge-primary"><c:out value="${order.status}"/></span>
            </div>
            <div style="display: flex; justify-content: space-between; margin-bottom: 10px; font-size: 14px;">
                <span style="color: var(--gray-500);">Transaction ID:</span>
                <span style="font-family: monospace; font-weight: 700;"><c:out value="${order.transactionId}"/></span>
            </div>
            <div style="display: flex; justify-content: space-between; margin-bottom: 10px; font-size: 14px;">
                <span style="color: var(--gray-500);">Amount Paid:</span>
                <strong style="color: var(--primary-dark); font-size: 16px;"><c:out value="${order.formattedTotalAmount}"/></strong>
            </div>
            <div style="display: flex; justify-content: space-between; font-size: 14px;">
                <span style="color: var(--gray-500);">Deliver To:</span>
                <span><c:out value="${order.customerName}"/>, <c:out value="${order.city}"/> - <c:out value="${order.pincode}"/></span>
            </div>
        </div>

        <div style="display: flex; gap: 14px; justify-content: center;">
            <a href="${pageContext.request.contextPath}/order?id=${order.id}" class="btn btn-primary">Track Order ➔</a>
            <a href="${pageContext.request.contextPath}/products" class="btn btn-secondary">Continue Shopping</a>
        </div>
    </div>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp"/>
