<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="pageTitle" value="User Registry — Admin ShopEase" scope="request"/>
<jsp:include page="/WEB-INF/views/common/header.jsp"/>

<div class="container" style="margin-top: 30px;">
    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px;">
        <div>
            <h1 style="font-size: 26px; font-weight: 800;">User Registry</h1>
            <p style="color: var(--gray-500); font-size: 14px;">All registered accounts across Buyer, Seller, and Admin roles</p>
        </div>
        <a href="${pageContext.request.contextPath}/admin/dashboard" class="btn btn-secondary">Admin Dashboard</a>
    </div>

    <div class="card">
        <div class="table-responsive">
            <table class="table">
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>Full Name</th>
                        <th>Email Address</th>
                        <th>Role</th>
                        <th>Created At</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach var="u" items="${users}">
                        <tr>
                            <td>#<c:out value="${u.id}"/></td>
                            <td><strong><c:out value="${u.name}"/></strong></td>
                            <td><c:out value="${u.email}"/></td>
                            <td>
                                <span class="badge ${u.role == 'ADMIN' ? 'badge-accent' : (u.role == 'SELLER' ? 'badge-primary' : 'badge-secondary')}">
                                    <c:out value="${u.role}"/>
                                </span>
                            </td>
                            <td><c:out value="${u.createdAt}"/></td>
                        </tr>
                    </c:forEach>
                </tbody>
            </table>
        </div>
    </div>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp"/>
