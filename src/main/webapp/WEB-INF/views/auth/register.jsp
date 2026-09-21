<c:set var="pageTitle" value="Create Account — ShopEase" scope="request"/>
<jsp:include page="/WEB-INF/views/common/header.jsp"/>

<div class="container" style="max-width: 500px; margin: 40px auto;">
    <div class="card">
        <h2 style="font-size: 24px; font-weight: 800; margin-bottom: 8px; text-align: center;">Create Account</h2>
        <p style="color: var(--gray-500); text-align: center; margin-bottom: 24px;">Join ShopEase as a Buyer or Seller</p>

        <c:if test="${not empty errorMessage}">
            <div class="alert alert-danger"><c:out value="${errorMessage}"/></div>
        </c:if>

        <form action="${pageContext.request.contextPath}/register" method="post">
            <div class="form-group">
                <label class="form-label" for="name">Full Name</label>
                <input type="text" id="name" name="name" value="<c:out value='${name}'/>" required class="form-control" placeholder="e.g. Priya Sharma">
            </div>

            <div class="form-group">
                <label class="form-label" for="email">Email Address</label>
                <input type="email" id="email" name="email" value="<c:out value='${email}'/>" required class="form-control" placeholder="priya@example.com">
            </div>

            <div class="form-group">
                <label class="form-label" for="password">Password (min 6 characters)</label>
                <input type="password" id="password" name="password" required minlength="6" class="form-control" placeholder="••••••••">
            </div>

            <div class="form-group">
                <label class="form-label">I want to register as a:</label>
                <div style="display: flex; gap: 20px; margin-top: 6px;">
                    <label style="display: flex; align-items: center; gap: 6px; font-weight: 600; cursor: pointer;">
                        <input type="radio" name="role" value="BUYER" ${role != 'SELLER' ? 'checked' : ''}> Buyer (Shop & Review)
                    </label>
                    <label style="display: flex; align-items: center; gap: 6px; font-weight: 600; cursor: pointer;">
                        <input type="radio" name="role" value="SELLER" ${role == 'SELLER' ? 'checked' : ''}> Seller (List Products)
                    </label>
                </div>
            </div>

            <button type="submit" class="btn btn-primary btn-block" style="margin-top: 14px;">Register Account</button>
        </form>

        <p style="margin-top: 20px; text-align: center; font-size: 14px;">
            Already have an account? <a href="${pageContext.request.contextPath}/login" style="color: var(--primary-dark); font-weight: 700;">Sign in</a>
        </p>
    </div>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp"/>
