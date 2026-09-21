<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="pageTitle" value="Product Catalog — ShopEase" scope="request"/>
<jsp:include page="/WEB-INF/views/common/header.jsp"/>

<div class="category-nav">
    <div class="container">
        <ul class="category-list">
            <li><a href="${pageContext.request.contextPath}/products" class="${selectedCategory == 'all' ? 'active' : ''}">🌟 All Categories</a></li>
            <c:forEach var="cat" items="${categories}">
                <li><a href="${pageContext.request.contextPath}/products?category=${cat}" class="${selectedCategory == cat ? 'active' : ''}"><c:out value="${cat}"/></a></li>
            </c:forEach>
        </ul>
    </div>
</div>

<div class="container" style="margin-top: 24px;">
    <!-- Filter Bar -->
    <div class="card" style="padding: 16px; margin-bottom: 24px;">
        <form action="${pageContext.request.contextPath}/products" method="get" style="display: flex; gap: 16px; align-items: center; flex-wrap: wrap;">
            <div style="flex: 2; min-width: 200px;">
                <input type="text" name="q" value="<c:out value='${keyword}'/>" class="form-control" placeholder="Search product name or keyword...">
            </div>

            <div style="flex: 1; min-width: 150px;">
                <select name="category" class="form-control">
                    <option value="all" ${selectedCategory == 'all' ? 'selected' : ''}>All Categories</option>
                    <c:forEach var="cat" items="${categories}">
                        <option value="<c:out value='${cat}'/>" ${selectedCategory == cat ? 'selected' : ''}><c:out value="${cat}"/></option>
                    </c:forEach>
                </select>
            </div>

            <div style="flex: 1; min-width: 150px;">
                <select name="sort" class="form-control">
                    <option value="newest" ${sortBy == 'newest' ? 'selected' : ''}>Newest Arrivals</option>
                    <option value="price_asc" ${sortBy == 'price_asc' ? 'selected' : ''}>Price: Low to High</option>
                    <option value="price_desc" ${sortBy == 'price_desc' ? 'selected' : ''}>Price: High to Low</option>
                    <option value="discount" ${sortBy == 'discount' ? 'selected' : ''}>Highest Discount</option>
                </select>
            </div>

            <button type="submit" class="btn btn-primary">Apply Filters</button>
            <a href="${pageContext.request.contextPath}/products" class="btn btn-secondary">Clear</a>
        </form>
    </div>

    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px;">
        <p style="color: var(--gray-500); font-size: 14px;">Showing <strong><c:out value="${totalCount}"/></strong> products</p>
    </div>

    <!-- Product Grid -->
    <c:choose>
        <c:when test="${empty products}">
            <div class="card" style="text-align: center; padding: 48px 20px;">
                <h3>No Products Found</h3>
                <p style="color: var(--gray-500); margin-top: 8px;">Try adjusting your keyword search or category filter.</p>
                <a href="${pageContext.request.contextPath}/products" class="btn btn-primary" style="margin-top: 16px;">View All Products</a>
            </div>
        </c:when>
        <c:otherwise>
            <div class="product-grid">
                <c:forEach var="p" items="${products}">
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
        </c:otherwise>
    </c:choose>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp"/>
