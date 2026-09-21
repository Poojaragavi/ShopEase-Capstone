<%@ page contentType="text/html;charset=UTF-8" language="java" isELIgnored="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="pageTitle" value="Seller Products — ShopEase" scope="request"/>
<jsp:include page="/WEB-INF/views/common/header.jsp"/>

<div class="container" style="margin-top: 30px;">
    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px;">
        <div>
            <h1 style="font-size: 26px; font-weight: 800;">My Product Listings</h1>
            <p style="color: var(--gray-500); font-size: 14px;">Manage stock, update pricing, or create new listings</p>
        </div>
        <div>
            <button type="button" class="btn btn-primary" onclick="openNewProductModal()">+ Add New Product</button>
            <a href="${pageContext.request.contextPath}/seller/dashboard" class="btn btn-secondary" style="margin-left: 8px;">Dashboard</a>
        </div>
    </div>

    <c:if test="${not empty sessionScope.successMessage}">
        <div class="alert alert-success"><c:out value="${sessionScope.successMessage}"/></div>
        <c:remove var="successMessage" scope="session"/>
    </c:if>
    <c:if test="${not empty sessionScope.errorMessage}">
        <div class="alert alert-danger"><c:out value="${sessionScope.errorMessage}"/></div>
        <c:remove var="errorMessage" scope="session"/>
    </c:if>

    <div class="card">
        <div class="table-responsive">
            <table class="table">
                <thead>
                    <tr>
                        <th>Product</th>
                        <th>Category</th>
                        <th>Price (₹)</th>
                        <th>Original (₹)</th>
                        <th>Discount</th>
                        <th>Stock</th>
                        <th>Actions</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach var="p" items="${products}">
                        <tr>
                            <td>
                                <div style="display: flex; align-items: center; gap: 10px;">
                                    <img src="${fn:escapeXml(p.imageUrl)}" style="width: 45px; height: 45px; object-fit: cover; border-radius: 6px;" onerror="this.src='https://images.unsplash.com/photo-1542838132-92c53300491e?w=500';">
                                    <div>
                                        <strong><c:out value="${p.name}"/></strong>
                                        <div style="font-size: 11px; color: var(--gray-500);">ID: #${p.id}</div>
                                    </div>
                                </div>
                            </td>
                            <td><span class="badge badge-secondary"><c:out value="${p.category}"/></span></td>
                            <td><strong style="color: var(--primary-dark);"><c:out value="${p.formattedPrice}"/></strong></td>
                            <td><c:out value="${p.formattedOriginalPrice != null ? p.formattedOriginalPrice : '-'}"/></td>
                            <td>
                                <c:if test="${p.discountPercentage > 0}">
                                    <span class="badge badge-accent"><c:out value="${p.discountPercentage}"/>%</span>
                                </c:if>
                            </td>
                            <td>
                                <c:choose>
                                    <c:when test="${!p.inStock}">
                                        <span class="badge badge-danger">0 (Out)</span>
                                    </c:when>
                                    <c:when test="${p.lowStock}">
                                        <span class="badge badge-accent"><c:out value="${p.stockQty}"/> (Low)</span>
                                    </c:when>
                                    <c:otherwise>
                                        <span class="badge badge-primary"><c:out value="${p.stockQty}"/></span>
                                    </c:otherwise>
                                </c:choose>
                            </td>
                            <td>
                                <div style="display: flex; gap: 6px;">
                                    <button type="button" class="btn btn-secondary" style="padding: 4px 8px; font-size: 12px;" onclick='openEditModal(${p.id}, "${fn:escapeXml(p.name)}", "${fn:escapeXml(p.description)}", "${fn:escapeXml(p.category)}", ${p.price}, "${p.originalPrice}", ${p.stockQty}, "${fn:escapeXml(p.imageUrl)}")'>Edit</button>
                                    <a href="${pageContext.request.contextPath}/seller/product/delete?id=${p.id}" class="btn btn-danger" style="padding: 4px 8px; font-size: 12px;" onclick="return confirm('Are you sure you want to delete this product listing?')">Delete</a>
                                </div>
                            </td>
                        </tr>
                    </c:forEach>
                </tbody>
            </table>
        </div>
    </div>
</div>

<!-- Modal: New Product -->
<div id="newProductModal" style="display: none; position: fixed; inset: 0; background: rgba(0,0,0,0.5); z-index: 9999; justify-content: center; align-items: center;">
    <div class="card" style="width: 550px; max-width: 90%; margin: auto; padding: 24px; max-height: 90vh; overflow-y: auto;">
        <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px;">
            <h3 style="font-size: 18px; font-weight: 700;">Add New Product Listing</h3>
            <button type="button" onclick="closeModals()" style="background: none; border: none; font-size: 20px; cursor: pointer;">&times;</button>
        </div>

        <form action="${pageContext.request.contextPath}/seller/product/new" method="post">
            <div class="form-group">
                <label class="form-label">Product Name *</label>
                <input type="text" name="name" required class="form-control" placeholder="e.g. Organic Almond Butter (250g)">
            </div>

            <div class="form-group">
                <label class="form-label">Category *</label>
                <select name="category" required class="form-control">
                    <option value="Grocery">Grocery</option>
                    <option value="Fragrance">Fragrance</option>
                    <option value="Juice">Juice</option>
                    <option value="Ice Cream">Ice Cream</option>
                    <option value="Snacks">Snacks</option>
                    <option value="Stationery">Stationery</option>
                    <option value="Makeup">Makeup</option>
                </select>
            </div>

            <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 14px;">
                <div class="form-group">
                    <label class="form-label">Selling Price (₹ INR) *</label>
                    <input type="number" step="0.01" name="price" required min="0" class="form-control" placeholder="e.g. 249.00">
                </div>

                <div class="form-group">
                    <label class="form-label">Original MRP Price (₹ INR)</label>
                    <input type="number" step="0.01" name="originalPrice" min="0" class="form-control" placeholder="e.g. 299.00">
                </div>
            </div>

            <div class="form-group">
                <label class="form-label">Stock Quantity *</label>
                <input type="number" name="stockQty" required min="0" value="20" class="form-control">
            </div>

            <div class="form-group">
                <label class="form-label">Image URL</label>
                <input type="url" name="imageUrl" class="form-control" placeholder="https://images.unsplash.com/photo-...">
            </div>

            <div class="form-group">
                <label class="form-label">Description</label>
                <textarea name="description" class="form-control" rows="3" placeholder="Product details, ingredients, and shelf-life..."></textarea>
            </div>

            <div style="display: flex; justify-content: flex-end; gap: 10px; margin-top: 20px;">
                <button type="button" class="btn btn-secondary" onclick="closeModals()">Cancel</button>
                <button type="submit" class="btn btn-primary">Publish Listing</button>
            </div>
        </form>
    </div>
</div>

<!-- Modal: Edit Product -->
<div id="editProductModal" style="display: none; position: fixed; inset: 0; background: rgba(0,0,0,0.5); z-index: 9999; justify-content: center; align-items: center;">
    <div class="card" style="width: 550px; max-width: 90%; margin: auto; padding: 24px; max-height: 90vh; overflow-y: auto;">
        <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px;">
            <h3 style="font-size: 18px; font-weight: 700;">Edit Product Listing</h3>
            <button type="button" onclick="closeModals()" style="background: none; border: none; font-size: 20px; cursor: pointer;">&times;</button>
        </div>

        <form action="${pageContext.request.contextPath}/seller/product/edit" method="post">
            <input type="hidden" id="editId" name="id">

            <div class="form-group">
                <label class="form-label">Product Name *</label>
                <input type="text" id="editName" name="name" required class="form-control">
            </div>

            <div class="form-group">
                <label class="form-label">Category *</label>
                <select id="editCategory" name="category" required class="form-control">
                    <option value="Grocery">Grocery</option>
                    <option value="Fragrance">Fragrance</option>
                    <option value="Juice">Juice</option>
                    <option value="Ice Cream">Ice Cream</option>
                    <option value="Snacks">Snacks</option>
                    <option value="Stationery">Stationery</option>
                    <option value="Makeup">Makeup</option>
                </select>
            </div>

            <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 14px;">
                <div class="form-group">
                    <label class="form-label">Selling Price (₹ INR) *</label>
                    <input type="number" step="0.01" id="editPrice" name="price" required min="0" class="form-control">
                </div>

                <div class="form-group">
                    <label class="form-label">Original MRP Price (₹ INR)</label>
                    <input type="number" step="0.01" id="editOriginalPrice" name="originalPrice" min="0" class="form-control">
                </div>
            </div>

            <div class="form-group">
                <label class="form-label">Stock Quantity *</label>
                <input type="number" id="editStockQty" name="stockQty" required min="0" class="form-control">
            </div>

            <div class="form-group">
                <label class="form-label">Image URL</label>
                <input type="url" id="editImageUrl" name="imageUrl" class="form-control">
            </div>

            <div class="form-group">
                <label class="form-label">Description</label>
                <textarea id="editDescription" name="description" class="form-control" rows="3"></textarea>
            </div>

            <div style="display: flex; justify-content: flex-end; gap: 10px; margin-top: 20px;">
                <button type="button" class="btn btn-secondary" onclick="closeModals()">Cancel</button>
                <button type="submit" class="btn btn-primary">Save Changes</button>
            </div>
        </form>
    </div>
</div>

<script>
function openNewProductModal() {
    document.getElementById('newProductModal').style.display = 'flex';
}

function openEditModal(id, name, desc, cat, price, origPrice, stock, img) {
    document.getElementById('editId').value = id;
    document.getElementById('editName').value = name;
    document.getElementById('editDescription').value = desc;
    document.getElementById('editCategory').value = cat;
    document.getElementById('editPrice').value = price;
    document.getElementById('editOriginalPrice').value = (origPrice && origPrice !== 'null') ? origPrice : '';
    document.getElementById('editStockQty').value = stock;
    document.getElementById('editImageUrl').value = img;
    document.getElementById('editProductModal').style.display = 'flex';
}

function closeModals() {
    document.getElementById('newProductModal').style.display = 'none';
    document.getElementById('editProductModal').style.display = 'none';
}
</script>

<jsp:include page="/WEB-INF/views/common/footer.jsp"/>
