<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="pageTitle" value="Shopping Cart — ShopEase" scope="request"/>
<jsp:include page="/WEB-INF/views/common/header.jsp"/>

<div class="container" style="margin-top: 30px;">
    <h1 style="font-size: 28px; font-weight: 800; margin-bottom: 24px;">Your Shopping Cart 🛒</h1>

    <c:if test="${not empty sessionScope.cartErrorMessage}">
        <div class="alert alert-danger"><c:out value="${sessionScope.cartErrorMessage}"/></div>
        <c:remove var="cartErrorMessage" scope="session"/>
    </c:if>

    <c:choose>
        <c:when test="${empty cart.items && empty cart.savedForLaterItems}">
            <div class="card" style="text-align: center; padding: 60px 20px;">
                <div style="font-size: 48px; margin-bottom: 16px;">🛒</div>
                <h2>Your Cart is Empty</h2>
                <p style="color: var(--gray-500); margin: 8px 0 24px;">Explore our 7 departments and fill your basket with great supermarket deals!</p>
                <a href="${pageContext.request.contextPath}/products" class="btn btn-primary">Start Shopping Now</a>
            </div>
        </c:when>
        <c:otherwise>
            <div style="display: grid; grid-template-columns: 2fr 1fr; gap: 30px; align-items: start;">
                <!-- Cart Items Column -->
                <div>
                    <div class="card">
                        <h3 style="font-size: 18px; font-weight: 700; margin-bottom: 16px;">Active Items (${cart.totalItemCount})</h3>

                        <c:choose>
                            <c:when test="${empty cart.items}">
                                <p style="color: var(--gray-500);">No active items in cart.</p>
                            </c:when>
                            <c:otherwise>
                                <div style="display: flex; flex-direction: column; gap: 20px;">
                                    <c:forEach var="item" items="${cart.items}">
                                        <div style="display: flex; gap: 16px; border-bottom: 1px solid var(--gray-200); padding-bottom: 16px; align-items: center;">
                                            <img src="<c:out value='${item.productImageUrl}'/>" alt="<c:out value='${item.productName}'/>" style="width: 80px; height: 80px; object-fit: cover; border-radius: var(--radius);" onerror="this.src='https://images.unsplash.com/photo-1542838132-92c53300491e?w=500';">

                                            <div style="flex: 1;">
                                                <span class="product-cat" style="font-size: 11px;"><c:out value="${item.category}"/></span>
                                                <h4 style="font-size: 16px; margin-bottom: 4px;"><c:out value="${item.productName}"/></h4>
                                                <p style="font-weight: 700; color: var(--primary-dark); font-size: 15px;"><c:out value="${item.formattedUnitPrice}"/></p>
                                            </div>

                                            <!-- Quantity Update Form -->
                                            <form action="${pageContext.request.contextPath}/cart" method="post" style="display: flex; align-items: center; gap: 8px;">
                                                <input type="hidden" name="action" value="update">
                                                <input type="hidden" name="cartItemId" value="${item.id}">
                                                <input type="number" name="quantity" value="${item.quantity}" min="1" max="${item.availableStock}" class="form-control" style="width: 65px; text-align: center;" onchange="this.form.submit()">
                                            </form>

                                            <!-- Item Subtotal -->
                                            <div style="min-width: 85px; text-align: right; font-weight: 800; font-size: 16px;">
                                                <c:out value="${item.formattedSubtotal}"/>
                                            </div>

                                            <!-- Actions: Save For Later & Remove -->
                                            <div style="display: flex; flex-direction: column; gap: 6px;">
                                                <form action="${pageContext.request.contextPath}/cart" method="post">
                                                    <input type="hidden" name="action" value="saveForLater">
                                                    <input type="hidden" name="cartItemId" value="${item.id}">
                                                    <button type="submit" class="btn btn-secondary" style="padding: 4px 8px; font-size: 11px;">Save for Later</button>
                                                </form>

                                                <form action="${pageContext.request.contextPath}/cart" method="post">
                                                    <input type="hidden" name="action" value="remove">
                                                    <input type="hidden" name="cartItemId" value="${item.id}">
                                                    <button type="submit" class="btn btn-danger" style="padding: 4px 8px; font-size: 11px;">Remove</button>
                                                </form>
                                            </div>
                                        </div>
                                    </c:forEach>
                                </div>
                            </c:otherwise>
                        </c:choose>
                    </div>

                    <!-- Saved For Later Section -->
                    <c:if test="${not empty cart.savedForLaterItems}">
                        <div class="card" style="margin-top: 24px;">
                            <h3 style="font-size: 18px; font-weight: 700; margin-bottom: 16px;">Saved For Later (${cart.savedForLaterItems.size()})</h3>
                            <div style="display: flex; flex-direction: column; gap: 16px;">
                                <c:forEach var="saved" items="${cart.savedForLaterItems}">
                                    <div style="display: flex; gap: 16px; border-bottom: 1px solid var(--gray-200); padding-bottom: 14px; align-items: center;">
                                        <img src="<c:out value='${saved.productImageUrl}'/>" alt="<c:out value='${saved.productName}'/>" style="width: 60px; height: 60px; object-fit: cover; border-radius: var(--radius);">

                                        <div style="flex: 1;">
                                            <h4 style="font-size: 15px;"><c:out value="${saved.productName}"/></h4>
                                            <p style="font-weight: 700; color: var(--primary-dark);"><c:out value="${saved.formattedUnitPrice}"/></p>
                                        </div>

                                        <form action="${pageContext.request.contextPath}/cart" method="post">
                                            <input type="hidden" name="action" value="moveToCart">
                                            <input type="hidden" name="cartItemId" value="${saved.id}">
                                            <button type="submit" class="btn btn-primary" style="padding: 6px 12px; font-size: 12px;">Move to Cart</button>
                                        </form>

                                        <form action="${pageContext.request.contextPath}/cart" method="post">
                                            <input type="hidden" name="action" value="remove">
                                            <input type="hidden" name="cartItemId" value="${saved.id}">
                                            <button type="submit" class="btn btn-secondary" style="padding: 6px 12px; font-size: 12px;">Delete</button>
                                        </form>
                                    </div>
                                </c:forEach>
                            </div>
                        </div>
                    </c:if>
                </div>

                <!-- Order Summary Card -->
                <div>
                    <div class="card">
                        <h3 style="font-size: 18px; font-weight: 700; margin-bottom: 20px;">Order Summary</h3>

                        <div style="display: flex; justify-content: space-between; margin-bottom: 12px; font-size: 14px;">
                            <span style="color: var(--gray-500);">Subtotal (${cart.totalItemCount} items)</span>
                            <span style="font-weight: 600;"><c:out value="${cart.formattedTotalAmount}"/></span>
                        </div>

                        <div style="display: flex; justify-content: space-between; margin-bottom: 12px; font-size: 14px;">
                            <span style="color: var(--gray-500);">Standard Delivery</span>
                            <span style="color: var(--primary-dark); font-weight: 700;">FREE</span>
                        </div>

                        <div style="border-top: 1px solid var(--gray-200); padding-top: 16px; margin-top: 16px; display: flex; justify-content: space-between; align-items: baseline;">
                            <span style="font-size: 18px; font-weight: 800;">Grand Total:</span>
                            <span style="font-size: 24px; font-weight: 800; color: var(--primary-dark);"><c:out value="${cart.formattedTotalAmount}"/></span>
                        </div>

                        <c:choose>
                            <c:when test="${not empty cart.items}">
                                <a href="${pageContext.request.contextPath}/checkout" class="btn btn-primary btn-block" style="margin-top: 24px; padding: 14px; font-size: 16px;">
                                    Proceed to Checkout ➔
                                </a>
                            </c:when>
                            <c:otherwise>
                                <button class="btn btn-secondary btn-block btn-disabled" disabled style="margin-top: 24px;">Cart has no active items</button>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>
            </div>
        </c:otherwise>
    </c:choose>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp"/>
