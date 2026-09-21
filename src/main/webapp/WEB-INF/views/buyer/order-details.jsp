<%@ page contentType="text/html;charset=UTF-8" language="java" isELIgnored="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="pageTitle" value="Order #${order.id} Details — ShopEase" scope="request"/>
<jsp:include page="/WEB-INF/views/common/header.jsp"/>

<div class="container" style="max-width: 900px; margin: 30px auto;">
    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px;">
        <h1 style="font-size: 26px; font-weight: 800;">Order Details #<c:out value="${order.id}"/></h1>
        <a href="${pageContext.request.contextPath}/orders" class="btn btn-secondary" style="padding: 6px 14px; font-size: 13px;">➔ Back to Orders</a>
    </div>

    <!-- Status Tracking Card -->
    <div class="card" style="margin-bottom: 24px;">
        <h3 style="font-size: 16px; font-weight: 700; margin-bottom: 16px;">Fulfillment Status:</h3>
        <div style="display: flex; justify-content: space-between; position: relative; padding: 10px 0;">
            <div style="text-align: center; flex: 1;">
                <div style="font-size: 22px;">📝</div>
                <strong style="font-size: 13px; color: ${order.status == 'PENDING' || order.status == 'CONFIRMED' || order.status == 'SHIPPED' || order.status == 'DELIVERED' ? 'var(--primary-dark)' : 'var(--gray-500)'};">Placed / Confirmed</strong>
            </div>
            <div style="text-align: center; flex: 1;">
                <div style="font-size: 22px;">🚚</div>
                <strong style="font-size: 13px; color: ${order.status == 'SHIPPED' || order.status == 'DELIVERED' ? 'var(--primary-dark)' : 'var(--gray-500)'};">Shipped</strong>
            </div>
            <div style="text-align: center; flex: 1;">
                <div style="font-size: 22px;">🏠</div>
                <strong style="font-size: 13px; color: ${order.status == 'DELIVERED' ? 'var(--primary-dark)' : 'var(--gray-500)'};">Delivered</strong>
            </div>
        </div>
        <div style="margin-top: 14px; text-align: center;">
            Current Status: <span class="badge ${order.status == 'DELIVERED' ? 'badge-primary' : 'badge-accent'}" style="font-size: 14px; padding: 4px 12px;"><c:out value="${order.status}"/></span>
        </div>
    </div>

    <!-- Order Items & Reviews -->
    <div class="card" style="margin-bottom: 24px;">
        <h3 style="font-size: 18px; font-weight: 700; margin-bottom: 16px;">Purchased Items</h3>

        <div style="display: flex; flex-direction: column; gap: 16px;">
            <c:forEach var="item" items="${order.items}">
                <div style="display: flex; gap: 16px; align-items: center; border-bottom: 1px solid var(--gray-200); padding-bottom: 16px; flex-wrap: wrap;">
                    <img src="${fn:escapeXml(item.productImageUrl)}" alt="${fn:escapeXml(item.productName)}" style="width: 70px; height: 70px; object-fit: cover; border-radius: var(--radius);" onerror="this.src='https://images.unsplash.com/photo-1542838132-92c53300491e?w=500';">

                    <div style="flex: 1; min-width: 200px;">
                        <h4 style="font-size: 15px; margin-bottom: 4px;"><c:out value="${item.productName}"/></h4>
                        <div style="font-size: 13px; color: var(--gray-500);">
                            Qty: <c:out value="${item.quantity}"/> &times; <c:out value="${item.formattedUnitPrice}"/>
                        </div>
                    </div>

                    <div style="font-weight: 800; font-size: 16px; color: var(--dark);">
                        <c:out value="${item.formattedSubtotal}"/>
                    </div>

                    <!-- Review Eligibility Section -->
                    <div style="margin-left: auto;">
                        <c:choose>
                            <c:when test="${isDelivered}">
                                <c:choose>
                                    <c:when test="${item.reviewed}">
                                        <span class="badge badge-primary">✔ Reviewed</span>
                                    </c:when>
                                    <c:otherwise>
                                        <button type="button" class="btn btn-primary" style="padding: 6px 12px; font-size: 12px;" onclick="openReviewModal(${item.productId}, '${fn:escapeXml(item.productName)}')">
                                            ⭐ Leave Review
                                        </button>
                                    </c:otherwise>
                                </c:choose>
                            </c:when>
                            <c:otherwise>
                                <span style="font-size: 12px; color: var(--gray-500); font-style: italic;">
                                    (Review available upon delivery)
                                </span>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>
            </c:forEach>
        </div>

        <div style="margin-top: 20px; text-align: right;">
            <span style="font-size: 16px; color: var(--gray-500);">Total Paid: </span>
            <span style="font-size: 22px; font-weight: 800; color: var(--primary-dark);"><c:out value="${order.formattedTotalAmount}"/></span>
        </div>
    </div>

    <!-- Shipping & Transaction Info -->
    <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 20px;">
        <div class="card">
            <h4 style="font-size: 16px; font-weight: 700; margin-bottom: 12px;">Delivery Address</h4>
            <p style="font-size: 14px; line-height: 1.6; color: var(--gray-700);">
                <strong><c:out value="${order.customerName}"/></strong><br>
                <c:out value="${order.address}"/><br>
                <c:out value="${order.city}"/> - <c:out value="${order.pincode}"/><br>
                Phone: <c:out value="${order.phone}"/>
            </p>
        </div>

        <div class="card">
            <h4 style="font-size: 16px; font-weight: 700; margin-bottom: 12px;">Payment Details</h4>
            <p style="font-size: 14px; line-height: 1.6; color: var(--gray-700);">
                <strong>Method:</strong> <c:out value="${order.paymentMethod != null ? order.paymentMethod : 'ShopEase Mock Gateway'}"/><br>
                <strong>Transaction ID:</strong> <span style="font-family: monospace;"><c:out value="${order.transactionId != null ? order.transactionId : 'N/A'}"/></span><br>
                <strong>Date:</strong> <c:out value="${order.createdAt}"/>
            </p>
        </div>
    </div>
</div>

<!-- Review Modal -->
<div id="reviewModal" style="display: none; position: fixed; inset: 0; background: rgba(0,0,0,0.5); z-index: 9999; justify-content: center; align-items: center;">
    <div class="card" style="width: 450px; max-width: 90%; margin: auto; padding: 24px;">
        <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px;">
            <h3 style="font-size: 18px; font-weight: 700;">Rate & Review Product</h3>
            <button type="button" onclick="closeReviewModal()" style="background: none; border: none; font-size: 20px; cursor: pointer;">&times;</button>
        </div>

        <p id="reviewProductName" style="font-weight: 600; font-size: 14px; color: var(--primary-dark); margin-bottom: 16px;"></p>

        <form id="reviewForm" onsubmit="submitReview(event)">
            <input type="hidden" id="reviewProductId" name="productId">
            <input type="hidden" id="reviewOrderId" name="orderId" value="${order.id}">

            <div class="form-group">
                <label class="form-label">Your Rating (1 to 5 Stars)</label>
                <select id="reviewRating" class="form-control" required>
                    <option value="5">★★★★★ (5 Stars - Excellent)</option>
                    <option value="4">★★★★☆ (4 Stars - Very Good)</option>
                    <option value="3">★★★☆☆ (3 Stars - Average)</option>
                    <option value="2">★★☆☆☆ (2 Stars - Poor)</option>
                    <option value="1">★☆☆☆☆ (1 Star - Terrible)</option>
                </select>
            </div>

            <div class="form-group">
                <label class="form-label">Your Feedback / Review</label>
                <textarea id="reviewComment" class="form-control" rows="4" maxlength="1000" placeholder="Share your honest feedback on product freshness, quality, and packaging..." required></textarea>
            </div>

            <div style="display: flex; justify-content: flex-end; gap: 10px;">
                <button type="button" class="btn btn-secondary" onclick="closeReviewModal()">Cancel</button>
                <button type="submit" class="btn btn-primary">Submit Review</button>
            </div>
        </form>
    </div>
</div>

<script>
function openReviewModal(productId, productName) {
    document.getElementById('reviewProductId').value = productId;
    document.getElementById('reviewProductName').innerText = productName;
    document.getElementById('reviewModal').style.display = 'flex';
}

function closeReviewModal() {
    document.getElementById('reviewModal').style.display = 'none';
}

async function submitReview(e) {
    e.preventDefault();
    const productId = parseInt(document.getElementById('reviewProductId').value);
    const orderId = parseInt(document.getElementById('reviewOrderId').value);
    const rating = parseInt(document.getElementById('reviewRating').value);
    const comment = document.getElementById('reviewComment').value.trim();

    try {
        const resp = await fetch('${pageContext.request.contextPath}/api/v1/reviews', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ productId, orderId, rating, comment })
        });
        const data = await resp.json();
        if (data.success) {
            alert('Thank you! Your verified review has been published.');
            window.location.reload();
        } else {
            alert(data.error ? data.error.message : 'Failed to submit review.');
        }
    } catch (err) {
        alert('Error submitting review.');
    }
}
</script>

<jsp:include page="/WEB-INF/views/common/footer.jsp"/>
