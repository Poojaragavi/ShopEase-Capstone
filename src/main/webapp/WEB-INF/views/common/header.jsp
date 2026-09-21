<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><c:out value="${pageTitle != null ? pageTitle : 'ShopEase — Multi-Seller E-Commerce'}"/></title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/style.css">
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap" rel="stylesheet">
</head>
<body>
<header>
    <div class="top-bar">
        <div class="container">
            🚚 Free express delivery across India on all orders over ₹499 | 100% Quality Guaranteed
        </div>
    </div>
    <div class="container">
        <nav class="navbar">
            <a href="${pageContext.request.contextPath}/home" class="brand-logo">
                🛒 Shop<span>Ease</span>
            </a>

            <div class="search-bar">
                <form action="${pageContext.request.contextPath}/products" method="get" class="search-form">
                    <input type="text" name="q" value="${fn:escapeXml(param.q)}" placeholder="Search groceries, perfumes, snacks, beauty..." class="search-input">
                    <button type="submit" class="search-btn">Search</button>
                </form>
            </div>

            <ul class="nav-links">
                <li><a href="${pageContext.request.contextPath}/products">All Products</a></li>

                <c:choose>
                    <c:when test="${not empty sessionScope.currentUser}">
                        <c:if test="${sessionScope.currentUser.role == 'BUYER'}">
                            <li><a href="${pageContext.request.contextPath}/orders">My Orders</a></li>
                            <li><a href="${pageContext.request.contextPath}/cart">Cart 🛒</a></li>
                        </c:if>
                        <c:if test="${sessionScope.currentUser.role == 'SELLER'}">
                            <li><a href="${pageContext.request.contextPath}/seller/dashboard" class="badge badge-primary">Seller Portal</a></li>
                        </c:if>
                        <c:if test="${sessionScope.currentUser.role == 'ADMIN'}">
                            <li><a href="${pageContext.request.contextPath}/admin/dashboard" class="badge badge-accent">Admin Console</a></li>
                        </c:if>
                        <li><a href="${pageContext.request.contextPath}/profile"><c:out value="${sessionScope.currentUser.name}"/></a></li>
                        <li><a href="${pageContext.request.contextPath}/logout" style="color: var(--danger);">Logout</a></li>
                    </c:when>
                    <c:otherwise>
                        <li><a href="${pageContext.request.contextPath}/login">Login</a></li>
                        <li><a href="${pageContext.request.contextPath}/register" class="btn btn-primary" style="padding: 6px 14px; font-size: 13px;">Sign Up</a></li>
                    </c:otherwise>
                </c:choose>
            </ul>
        </nav>
    </div>
</header>
