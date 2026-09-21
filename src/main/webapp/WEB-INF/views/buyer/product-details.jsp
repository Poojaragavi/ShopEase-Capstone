<%@ page contentType="text/html;charset=UTF-8" language="java" isELIgnored="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="pageTitle" value="${product.name} — ShopEase" scope="request"/>
<jsp:include page="/WEB-INF/views/common/header.jsp"/>

<div class="container" style="margin-top: 30px;">
    <!-- Breadcrumb -->
    <div style="font-size: 14px; color: var(--gray-500); margin-bottom: 20px;">
        <a href="${pageContext.request.contextPath}/home" style="color: var(--gray-500); text-decoration: none;">Home</a> /
        <a href="${pageContext.request.contextPath}/products?category=${product.category}" style="color: var(--gray-500); text-decoration: none;"><c:out value="${product.category}"/></a> /
        <span style="color: var(--dark); font-weight: 600;"><c:out value="${product.name}"/></span>
    </div>

    <div class="card" style="padding: 30px;">
        <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(320px, 1fr)); gap: 40px;">
            <!-- Product Image -->
            <div style="background: #f3f4f6; border-radius: var(--radius); overflow: hidden; height: 380px; display: flex; align-items: center; justify-content: center; position: relative;">
                <img src="${fn:escapeXml(product.imageUrl)}" alt="${fn:escapeXml(product.name)}" style="max-height: 100%; max-width: 100%; object-fit: contain;" onerror="this.src='https://images.unsplash.com/photo-1542838132-92c53300491e?w=500';">
                <c:if test="${product.discountPercentage > 0}">
                    <span class="discount-tag" style="font-size: 14px; padding: 4px 12px;"><c:out value="${product.discountPercentage}"/>% OFF</span>
                </c:if>
            </div>

            <!-- Product Details -->
            <div>
                <span class="product-cat" style="font-size: 14px;"><c:out value="${product.category}"/></span>
                <h1 style="font-size: 28px; font-weight: 800; margin: 8px 0 12px; line-height: 1.2;"><c:out value="${product.name}"/></h1>

                <div class="product-rating" style="font-size: 15px; margin-bottom: 16px;">
                    <span class="stars" style="font-size: 18px;">★ <c:out value="${product.averageRating != null && product.averageRating > 0 ? product.averageRating : '4.5'}"/></span>
                    <span>(<c:out value="${product.reviewCount != null ? product.reviewCount : 0}"/> verified buyer reviews)</span>
                </div>

                <div style="display: flex; align-items: baseline; gap: 14px; margin-bottom: 20px;">
                    <span style="font-size: 32px; font-weight: 800; color: var(--primary-dark);"><c:out value="${product.formattedPrice}"/></span>
                    <c:if test="${not empty product.formattedOriginalPrice}">
                        <span class="original-price" style="font-size: 18px;"><c:out value="${product.formattedOriginalPrice}"/></span>
                    </c:if>
                    <span style="font-size: 12px; color: var(--gray-500);">(Inclusive of all Indian taxes)</span>
                </div>

                <div style="margin-bottom: 24px;">
                    <h4 style="font-size: 15px; font-weight: 700; margin-bottom: 8px;">Description:</h4>
                    <p style="color: var(--gray-700); line-height: 1.6;"><c:out value="${product.description}"/></p>
                </div>

                <div style="margin-bottom: 24px;">
                    <p style="font-size: 14px; margin-bottom: 6px;">
                        <strong>Sold by:</strong> <c:out value="${product.sellerName != null ? product.sellerName : 'Verified Supermarket Seller'}"/>
                    </p>
                    <p style="font-size: 14px;">
                        <strong>Availability:</strong>
                        <c:choose>
                            <c:when test="${!product.inStock}">
                                <span class="badge badge-danger">Out of Stock</span>
                            </c:when>
                            <c:when test="${product.lowStock}">
                                <span class="badge badge-accent">Low Stock — Only <c:out value="${product.stockQty}"/> units left</span>
                            </c:when>
                            <c:otherwise>
                                <span class="badge badge-primary">In Stock (<c:out value="${product.stockQty}"/> units)</span>
                            </c:otherwise>
                        </c:choose>
                    </p>
                </div>

                <!-- Add to Cart Form -->
                <c:choose>
                    <c:when test="${product.inStock}">
                        <div style="display: flex; gap: 14px; align-items: center; max-width: 320px;">
                            <input type="number" id="detailQuantity" value="1" min="1" max="${product.stockQty}" class="form-control" style="width: 80px; text-align: center;">
                            <button type="button" class="btn btn-primary btn-block" onclick="addProductToCart(${product.id}, parseInt(document.getElementById('detailQuantity').value))">
                                🛒 Add to Cart
                            </button>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <button type="button" class="btn btn-secondary btn-disabled" disabled style="width: 200px;">Out of Stock</button>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>
    </div>

    <!-- Verified Customer Reviews Section -->
    <div class="card" style="margin-top: 30px; padding: 30px;">
        <h3 style="font-size: 20px; font-weight: 800; margin-bottom: 20px;">Verified Customer Reviews (${reviews.size()})</h3>

        <c:choose>
            <c:when test="${empty reviews}">
                <p style="color: var(--gray-500);">No customer reviews yet for this product. Be the first verified buyer to leave a review after placing and receiving your order!</p>
            </c:when>
            <c:otherwise>
                <div style="display: flex; flex-direction: column; gap: 16px;">
                    <c:forEach var="rev" items="${reviews}">
                        <div style="border-bottom: 1px solid var(--gray-200); padding-bottom: 14px;">
                            <div style="display: flex; justify-content: space-between; margin-bottom: 6px;">
                                <div>
                                    <strong style="font-size: 15px;"><c:out value="${rev.userName}"/></strong>
                                    <span class="badge badge-primary" style="margin-left: 8px; font-size: 11px;">Verified Buyer</span>
                                </div>
                                <span class="stars">
                                    <c:forEach begin="1" end="${rev.rating}">★</c:forEach>
                                    <c:forEach begin="${rev.rating + 1}" end="5">☆</c:forEach>
                                </span>
                            </div>
                            <p style="color: var(--gray-700); font-size: 14px;"><c:out value="${rev.comment}"/></p>
                        </div>
                    </c:forEach>
                </div>
            </c:otherwise>
        </c:choose>
    </div>

    <!-- Related Products -->
    <c:if test="${not empty relatedProducts}">
        <div class="section-header" style="margin-top: 30px;">
            <h3 class="section-title">Similar Products in <c:out value="${product.category}"/></h3>
        </div>
        <div class="product-grid">
            <c:forEach var="rp" items="${relatedProducts}">
                <div class="product-card">
                    <div class="product-image-container">
                        <img src="${fn:escapeXml(rp.imageUrl)}" alt="${fn:escapeXml(rp.name)}" class="product-image" onerror="this.src='https://images.unsplash.com/photo-1542838132-92c53300491e?w=500';">
                    </div>
                    <div class="product-body">
                        <a href="${pageContext.request.contextPath}/product?id=${rp.id}" class="product-title"><c:out value="${rp.name}"/></a>
                        <div class="product-pricing">
                            <span class="current-price"><c:out value="${rp.formattedPrice}"/></span>
                        </div>
                    </div>
                </div>
            </c:forEach>
        </div>
    </c:if>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp"/>
