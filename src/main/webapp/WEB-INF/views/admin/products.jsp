<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="pageTitle" value="Catalog Moderation — Admin ShopEase" scope="request"/>
<jsp:include page="/WEB-INF/views/common/header.jsp"/>

<div class="container" style="margin-top: 30px;">
    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px;">
        <div>
            <h1 style="font-size: 26px; font-weight: 800;">Catalog Moderation</h1>
            <p style="color: var(--gray-500); font-size: 14px;">Review active seller listings and moderate content</p>
        </div>
        <a href="${pageContext.request.contextPath}/admin/dashboard" class="btn btn-secondary">Admin Dashboard</a>
    </div>

    <c:if test="${not empty sessionScope.successMessage}">
        <div class="alert alert-success"><c:out value="${sessionScope.successMessage}"/></div>
        <c:remove var="successMessage" scope="session"/>
    </c:if>

    <div class="card">
        <div class="table-responsive">
            <table class="table">
                <thead>
                    <tr>
                        <th>Product ID</th>
                        <th>Product</th>
                        <th>Seller</th>
                        <th>Category</th>
                        <th>Price (₹)</th>
                        <th>Stock</th>
                        <th>Action</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach var="p" items="${products}">
                        <tr>
                            <td>#${p.id}</td>
                            <td>
                                <strong><c:out value="${p.name}"/></strong>
                            </td>
                            <td><c:out value="${p.sellerName != null ? p.sellerName : 'Seller #'.concat(p.sellerId)}"/></td>
                            <td><span class="badge badge-secondary"><c:out value="${p.category}"/></span></td>
                            <td><strong style="color: var(--primary-dark);"><c:out value="${p.formattedPrice}"/></strong></td>
                            <td><c:out value="${p.stockQty}"/> units</td>
                            <td>
                                <a href="${pageContext.request.contextPath}/admin/product/delete?id=${p.id}" class="btn btn-danger" style="padding: 4px 8px; font-size: 11px;" onclick="return confirm('Confirm removing this product listing from marketplace?')">Remove Listing</a>
                            </td>
                        </tr>
                    </c:forEach>
                </tbody>
            </table>
        </div>
    </div>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp"/>
