<c:set var="pageTitle" value="Login — ShopEase" scope="request"/>
<jsp:include page="/WEB-INF/views/common/header.jsp"/>

<div class="container" style="max-width: 480px; margin: 40px auto;">
    <div class="card">
        <h2 style="font-size: 24px; font-weight: 800; margin-bottom: 8px; text-align: center;">Welcome Back</h2>
        <p style="color: var(--gray-500); text-align: center; margin-bottom: 24px;">Sign in to your ShopEase account</p>

        <c:if test="${not empty errorMessage}">
            <div class="alert alert-danger"><c:out value="${errorMessage}"/></div>
        </c:if>
        <c:if test="${param.loggedOut == 'true'}">
            <div class="alert alert-success">You have been logged out safely.</div>
        </c:if>

        <form action="${pageContext.request.contextPath}/login" method="post">
            <input type="hidden" name="redirect" value="<c:out value='${param.redirect}'/>">

            <div class="form-group">
                <label class="form-label" for="email">Email Address</label>
                <input type="email" id="email" name="email" value="<c:out value='${email != null ? email : param.email}'/>" required class="form-control" placeholder="name@example.com">
            </div>

            <div class="form-group">
                <label class="form-label" for="password">Password</label>
                <input type="password" id="password" name="password" required class="form-control" placeholder="••••••••">
            </div>

            <button type="submit" class="btn btn-primary btn-block" style="margin-top: 10px;">Sign In</button>
        </form>

        <div style="margin-top: 24px; padding-top: 20px; border-top: 1px solid var(--gray-200);">
            <p style="font-size: 13px; font-weight: 700; color: var(--gray-700); margin-bottom: 10px;">⚡ Quick Demo Credentials:</p>
            <div style="display: flex; gap: 8px; flex-wrap: wrap;">
                <button type="button" class="btn btn-secondary" style="padding: 5px 10px; font-size: 12px;" onclick="fillLogin('buyer@shopease.com', 'buyer123')">Demo Buyer</button>
                <button type="button" class="btn btn-secondary" style="padding: 5px 10px; font-size: 12px;" onclick="fillLogin('seller@shopease.com', 'seller123')">Demo Seller</button>
                <button type="button" class="btn btn-secondary" style="padding: 5px 10px; font-size: 12px;" onclick="fillLogin('admin@shopease.com', 'admin123')">Demo Admin</button>
            </div>
        </div>

        <p style="margin-top: 20px; text-align: center; font-size: 14px;">
            Don't have an account? <a href="${pageContext.request.contextPath}/register" style="color: var(--primary-dark); font-weight: 700;">Sign up</a>
        </p>
    </div>
</div>

<script>
function fillLogin(email, pwd) {
    document.getElementById('email').value = email;
    document.getElementById('password').value = pwd;
}
</script>

<jsp:include page="/WEB-INF/views/common/footer.jsp"/>
