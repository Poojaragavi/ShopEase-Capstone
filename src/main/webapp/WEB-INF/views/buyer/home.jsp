<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="pageTitle" value="ShopEase — Fresh Groceries, Supermarket & Lifestyle" scope="request"/>
<jsp:include page="/WEB-INF/views/common/header.jsp"/>

<div class="category-nav">
    <div class="container">
        <ul class="category-list">
            <li><a href="${pageContext.request.contextPath}/products" class="active">🌟 All Categories</a></li>
            <li><a href="${pageContext.request.contextPath}/products?category=Grocery">🍚 Grocery</a></li>
            <li><a href="${pageContext.request.contextPath}/products?category=Fragrance">🌸 Fragrance</a></li>
            <li><a href="${pageContext.request.contextPath}/products?category=Juice">🧃 Juice</a></li>
            <li><a href="${pageContext.request.contextPath}/products?category=Ice%20Cream">🍨 Ice Cream</a></li>
            <li><a href="${pageContext.request.contextPath}/products?category=Snacks">🍿 Snacks</a></li>
            <li><a href="${pageContext.request.contextPath}/products?category=Stationery">✏️ Stationery</a></li>
            <li><a href="${pageContext.request.contextPath}/products?category=Makeup">💄 Makeup</a></li>
        </ul>
    </div>
</div>

<div class="container">
    <!-- Hero Banner -->
    <div class="hero-banner">
        <div class="hero-content">
            <h1>Fresh Supermarket Picks & Everyday Essentials</h1>
            <p>Shop top brands across 7 curated departments with instant delivery and exclusive ₹ INR savings.</p>
            <a href="${pageContext.request.contextPath}/products" class="hero-btn">Explore All Deals ➔</a>
        </div>
    </div>

    <!-- Category Highlights -->
    <div class="section-header">
        <h2 class="section-title">Shop by Category</h2>
        <a href="${pageContext.request.contextPath}/products" class="view-all-link">Browse Catalog ➔</a>
    </div>

    <div class="category-grid">
        <a href="${pageContext.request.contextPath}/products?category=Grocery" class="category-card">
            <div class="category-icon">🌾</div>
            <div class="category-name">Grocery</div>
        </a>
        <a href="${pageContext.request.contextPath}/products?category=Fragrance" class="category-card">
            <div class="category-icon">✨</div>
            <div class="category-name">Fragrance</div>
        </a>
        <a href="${pageContext.request.contextPath}/products?category=Juice" class="category-card">
            <div class="category-icon">🥤</div>
            <div class="category-name">Juice</div>
        </a>
        <a href="${pageContext.request.contextPath}/products?category=Ice%20Cream" class="category-card">
            <div class="category-icon">🍦</div>
            <div class="category-name">Ice Cream</div>
        </a>
        <a href="${pageContext.request.contextPath}/products?category=Snacks" class="category-card">
            <div class="category-icon">🥨</div>
            <div class="category-name">Snacks</div>
        </a>
        <a href="${pageContext.request.contextPath}/products?category=Stationery" class="category-card">
            <div class="category-icon">📚</div>
            <div class="category-name">Stationery</div>
        </a>
        <a href="${pageContext.request.contextPath}/products?category=Makeup" class="category-card">
            <div class="category-icon">💄</div>
            <div class="category-name">Makeup</div>
        </a>
    </div>

    <!-- Hot Deals / Discounted Products -->
    <c:if test="${not empty discountedProducts}">
        <div class="section-header">
            <h2 class="section-title">🔥 Best Deals & Discounts</h2>
            <a href="${pageContext.request.contextPath}/products?sort=discount" class="view-all-link">View All Offers ➔</a>
        </div>

        <div class="product-grid">
            <c:forEach var="p" items="${discountedProducts}">
                <div class="product-card">
                    <div class="product-image-container">
                        <img src="<c:out value='${p.imageUrl}'/>" alt="<c:out value='${p.name}'/>" class="product-image" onerror="this.src='https://images.unsplash.com/photo-1542838132-92c53300491e?w=500';">
                        <c:if test="${p.discountPercentage > 0}">
                            <span class="discount-tag"><c:out value="${p.discountPercentage}"/>% OFF</span>
                        </c:if>
                        <c:choose>
                            <c:when test="${!p.inStock}">
                                <span class="badge badge-danger stock-tag">Out of Stock</span>
                            </c:when>
                            <c:when test="${p.lowStock}">
                                <span class="badge badge-accent stock-tag">Only <c:out value="${p.stockQty}"/> left!</span>
                            </c:when>
                        </c:choose>
                    </div>
                    <div class="product-body">
                        <span class="product-cat"><c:out value="${p.category}"/></span>
                        <a href="${pageContext.request.contextPath}/product?id=${p.id}" class="product-title"><c:out value="${p.name}"/></a>
                        <div class="product-rating">
                            <span class="stars">★ <c:out value="${p.averageRating != null && p.averageRating > 0 ? p.averageRating : '4.5'}"/></span>
                            <span>(<c:out value="${p.reviewCount != null ? p.reviewCount : 0}"/> reviews)</span>
                        </div>
                        <div class="product-pricing">
                            <span class="current-price"><c:out value="${p.formattedPrice}"/></span>
                            <c:if test="${not empty p.formattedOriginalPrice}">
                                <span class="original-price"><c:out value="${p.formattedOriginalPrice}"/></span>
                            </c:if>
                        </div>
                        <c:choose>
                            <c:when test="${p.inStock}">
                                <button type="button" class="btn btn-primary btn-block" onclick="addProductToCart(${p.id}, 1)">Add to Cart</button>
                            </c:when>
                            <c:otherwise>
                                <button type="button" class="btn btn-secondary btn-block btn-disabled" disabled>Out of Stock</button>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>
            </c:forEach>
        </div>
    </c:if>

    <!-- Featured Products Grid -->
    <div class="section-header">
        <h2 class="section-title">Featured Products</h2>
        <a href="${pageContext.request.contextPath}/products" class="view-all-link">View All Catalog ➔</a>
    </div>

    <div class="product-grid">
        <c:forEach var="p" items="${featuredProducts}">
            <div class="product-card">
                <div class="product-image-container">
                    <img src="<c:out value='${p.imageUrl}'/>" alt="<c:out value='${p.name}'/>" class="product-image" onerror="this.src='https://images.unsplash.com/photo-1542838132-92c53300491e?w=500';">
                    <c:if test="${p.discountPercentage > 0}">
                        <span class="discount-tag"><c:out value="${p.discountPercentage}"/>% OFF</span>
                    </c:if>
                    <c:choose>
                        <c:when test="${!p.inStock}">
                            <span class="badge badge-danger stock-tag">Out of Stock</span>
                        </c:when>
                        <c:when test="${p.lowStock}">
                            <span class="badge badge-accent stock-tag">Only <c:out value="${p.stockQty}"/> left!</span>
                        </c:when>
                    </c:choose>
                </div>
                <div class="product-body">
                    <span class="product-cat"><c:out value="${p.category}"/></span>
                    <a href="${pageContext.request.contextPath}/product?id=${p.id}" class="product-title"><c:out value="${p.name}"/></a>
                    <div class="product-rating">
                        <span class="stars">★ <c:out value="${p.averageRating != null && p.averageRating > 0 ? p.averageRating : '4.5'}"/></span>
                        <span>(<c:out value="${p.reviewCount != null ? p.reviewCount : 0}"/>)</span>
                    </div>
                    <div class="product-pricing">
                        <span class="current-price"><c:out value="${p.formattedPrice}"/></span>
                        <c:if test="${not empty p.formattedOriginalPrice}">
                            <span class="original-price"><c:out value="${p.formattedOriginalPrice}"/></span>
                        </c:if>
                    </div>
                    <c:choose>
                        <c:when test="${p.inStock}">
                            <button type="button" class="btn btn-primary btn-block" onclick="addProductToCart(${p.id}, 1)">Add to Cart</button>
                        </c:when>
                        <c:otherwise>
                            <button type="button" class="btn btn-secondary btn-block btn-disabled" disabled>Out of Stock</button>
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>
        </c:forEach>
    </div>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp"/>
