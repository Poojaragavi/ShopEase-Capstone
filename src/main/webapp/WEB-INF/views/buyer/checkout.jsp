<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="pageTitle" value="Checkout — ShopEase" scope="request"/>
<jsp:include page="/WEB-INF/views/common/header.jsp"/>

<div class="container" style="max-width: 900px; margin: 40px auto;">
    <h1 style="font-size: 28px; font-weight: 800; margin-bottom: 24px;">Secure Checkout 🔒</h1>

    <c:if test="${not empty errorMessage}">
        <div class="alert alert-danger"><c:out value="${errorMessage}"/></div>
    </c:if>

    <div style="display: grid; grid-template-columns: 3fr 2fr; gap: 30px; align-items: start;">
        <!-- Shipping & Mock Payment Form -->
        <div class="card">
            <h3 style="font-size: 18px; font-weight: 700; margin-bottom: 20px;">1. Shipping Information (India)</h3>

            <form action="${pageContext.request.contextPath}/checkout" method="post" id="checkoutForm">
                <div class="form-group">
                    <label class="form-label" for="customerName">Full Name</label>
                    <input type="text" id="customerName" name="customerName" value="<c:out value='${customerName != null ? customerName : user.name}'/>" required class="form-control" placeholder="Recipient's Name">
                </div>

                <div class="form-group">
                    <label class="form-label" for="phone">Phone Number (10 Digits)</label>
                    <input type="tel" id="phone" name="phone" value="<c:out value='${phone != null ? phone : "9876543210"}'/>" required class="form-control" placeholder="e.g. 9876543210">
                </div>

                <div class="form-group">
                    <label class="form-label" for="address">Street Address / House No.</label>
                    <input type="text" id="address" name="address" value="<c:out value='${address != null ? address : "Flat 402, Green Valley Apartments"}'/>" required class="form-control" placeholder="House No, Street, Landmark">
                </div>

                <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 16px;">
                    <div class="form-group">
                        <label class="form-label" for="city">City / District</label>
                        <input type="text" id="city" name="city" value="<c:out value='${city != null ? city : "Chennai"}'/>" required class="form-control" placeholder="e.g. Chennai">
                    </div>

                    <div class="form-group">
                        <label class="form-label" for="pincode">Postal Pincode</label>
                        <input type="text" id="pincode" name="pincode" value="<c:out value='${pincode != null ? pincode : "600001"}'/>" required class="form-control" placeholder="e.g. 600001">
                    </div>
                </div>

                <h3 style="font-size: 18px; font-weight: 700; margin: 24px 0 16px;">2. Payment Method</h3>
                <div class="card" style="background: var(--gray-100); border: 2px dashed var(--primary); padding: 16px; margin-bottom: 24px;">
                    <div style="display: flex; align-items: center; gap: 10px;">
                        <span style="font-size: 24px;">💳</span>
                        <div>
                            <strong style="font-size: 15px; color: var(--primary-dark);">ShopEase Instant Mock Gateway (Simulated UPI / Cards)</strong>
                            <p style="font-size: 12px; color: var(--gray-500); margin-top: 2px;">
                                Simulated sandbox environment. No actual bank charge will occur for this capstone review.
                            </p>
                        </div>
                    </div>
                </div>

                <button type="submit" class="btn btn-primary btn-block" style="padding: 14px; font-size: 16px;">
                    Pay <c:out value="${cart.formattedTotalAmount}"/> & Place Order ➔
                </button>
            </form>
        </div>

        <!-- Checkout Summary -->
        <div class="card">
            <h3 style="font-size: 18px; font-weight: 700; margin-bottom: 16px;">Order Items (${cart.totalItemCount})</h3>

            <div style="display: flex; flex-direction: column; gap: 12px; max-height: 280px; overflow-y: auto; margin-bottom: 16px;">
                <c:forEach var="item" items="${cart.items}">
                    <div style="display: flex; justify-content: space-between; align-items: center; font-size: 14px; border-bottom: 1px solid var(--gray-200); padding-bottom: 8px;">
                        <div>
                            <div style="font-weight: 600;"><c:out value="${item.productName}"/></div>
                            <div style="font-size: 12px; color: var(--gray-500);">Qty: <c:out value="${item.quantity}"/> &times; <c:out value="${item.formattedUnitPrice}"/></div>
                        </div>
                        <div style="font-weight: 700;"><c:out value="${item.formattedSubtotal}"/></div>
                    </div>
                </c:forEach>
            </div>

            <div style="border-top: 2px solid var(--gray-200); padding-top: 14px;">
                <div style="display: flex; justify-content: space-between; margin-bottom: 8px; font-size: 14px;">
                    <span>Delivery Fee:</span>
                    <span style="color: var(--primary-dark); font-weight: 700;">FREE</span>
                </div>
                <div style="display: flex; justify-content: space-between; font-size: 18px; font-weight: 800;">
                    <span>Total:</span>
                    <span style="color: var(--primary-dark);"><c:out value="${cart.formattedTotalAmount}"/></span>
                </div>
            </div>
        </div>
    </div>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp"/>
