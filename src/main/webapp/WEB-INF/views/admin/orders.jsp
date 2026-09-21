<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="pageTitle" value="System Orders — Admin ShopEase" scope="request"/>
<jsp:include page="/WEB-INF/views/common/header.jsp"/>

<div class="container" style="margin-top: 30px;">
    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px;">
        <div>
            <h1 style="font-size: 26px; font-weight: 800;">All System Orders</h1>
            <p style="color: var(--gray-500); font-size: 14px;">Complete order registry across all buyers and sellers</p>
        </div>
        <a href="${pageContext.request.contextPath}/admin/dashboard" class="btn btn-secondary">Admin Dashboard</a>
    </div>

    <div class="card">
        <div class="table-responsive">
            <table class="table">
                <thead>
                    <tr>
                        <th>Order ID</th>
                        <th>Buyer Email</th>
                        <th>Customer</th>
                        <th>Status</th>
                        <th>Total Amount (₹)</th>
                        <th>Created At</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach var="ord" items="${orders}">
                        <tr>
                            <td>#<c:out value="${ord.id}"/></td>
                            <td><c:out value="${ord.buyerEmail}"/></td>
                            <td><c:out value="${ord.customerName}"/> (<c:out value="${ord.city}"/>)</td>
                            <td>
                                <span class="badge ${ord.status == 'DELIVERED' ? 'badge-primary' : (ord.status == 'CANCELLED' ? 'badge-danger' : 'badge-accent')}">
                                    <c:out value="${ord.status}"/>
                                </span>
                            </td>
                            <td><strong style="color: var(--primary-dark);"><c:out value="${ord.formattedTotalAmount}"/></strong></td>
                            <td><c:out value="${ord.createdAt}"/></td>
                        </tr>
                    </c:forEach>
                </tbody>
            </table>
        </div>
    </div>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp"/>
