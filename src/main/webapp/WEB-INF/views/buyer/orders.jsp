<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="pageTitle" value="My Orders — ShopEase" scope="request"/>
<jsp:include page="/WEB-INF/views/common/header.jsp"/>

<div class="container" style="max-width: 900px; margin: 30px auto;">
    <h1 style="font-size: 26px; font-weight: 800; margin-bottom: 24px;">My Orders History 📦</h1>

    <c:choose>
        <c:when test="${empty orders}">
            <div class="card" style="text-align: center; padding: 48px 20px;">
                <h3>No Orders Placed Yet</h3>
                <p style="color: var(--gray-500); margin: 8px 0 20px;">You haven't placed any orders yet. Discover our fresh groceries and essentials!</p>
                <a href="${pageContext.request.contextPath}/products" class="btn btn-primary">Start Shopping</a>
            </div>
        </c:when>
        <c:otherwise>
            <div style="display: flex; flex-direction: column; gap: 20px;">
                <c:forEach var="ord" items="${orders}">
                    <div class="card">
                        <div style="display: flex; justify-content: space-between; align-items: center; border-bottom: 1px solid var(--gray-200); padding-bottom: 12px; margin-bottom: 14px;">
                            <div>
                                <span style="font-size: 13px; color: var(--gray-500);">Order placed on:</span>
                                <strong style="font-size: 14px; margin-left: 4px;"><c:out value="${ord.createdAt}"/></strong>
                            </div>
                            <div>
                                <span class="badge ${ord.status == 'DELIVERED' ? 'badge-primary' : (ord.status == 'CANCELLED' ? 'badge-danger' : 'badge-accent')}">
                                    <c:out value="${ord.status}"/>
                                </span>
                            </div>
                        </div>

                        <div style="display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 14px;">
                            <div>
                                <div style="font-size: 15px; font-weight: 700; margin-bottom: 4px;">
                                    Order #<c:out value="${ord.id}"/> &bull; <c:out value="${ord.items.size()}"/> item(s)
                                </div>
                                <div style="font-size: 13px; color: var(--gray-500);">
                                    Ship to: <c:out value="${ord.customerName}"/>, <c:out value="${ord.city}"/>
                                </div>
                            </div>

                            <div style="text-align: right;">
                                <div style="font-size: 18px; font-weight: 800; color: var(--primary-dark);">
                                    <c:out value="${ord.formattedTotalAmount}"/>
                                </div>
                                <a href="${pageContext.request.contextPath}/order?id=${ord.id}" class="btn btn-secondary" style="padding: 6px 14px; font-size: 13px; margin-top: 6px;">
                                    View Details & Tracking ➔
                                </a>
                            </div>
                        </div>
                    </div>
                </c:forEach>
            </div>
        </c:otherwise>
    </c:choose>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp"/>
