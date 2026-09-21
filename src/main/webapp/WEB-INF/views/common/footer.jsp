<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<footer>
    <div class="container">
        <div class="footer-grid">
            <div class="footer-col">
                <h4>ShopEase Superstore</h4>
                <p>Your premier multi-seller online grocery and supermarket marketplace. Fresh quality, authentic products, and swift door delivery across India in ₹ INR.</p>
            </div>
            <div class="footer-col">
                <h4>Categories</h4>
                <ul>
                    <li><a href="${pageContext.request.contextPath}/products?category=Grocery">Grocery & Staples</a></li>
                    <li><a href="${pageContext.request.contextPath}/products?category=Fragrance">Fragrances & Scents</a></li>
                    <li><a href="${pageContext.request.contextPath}/products?category=Juice">Fresh Juices</a></li>
                    <li><a href="${pageContext.request.contextPath}/products?category=Ice%20Cream">Gourmet Ice Creams</a></li>
                    <li><a href="${pageContext.request.contextPath}/products?category=Snacks">Snacks & Confectionery</a></li>
                    <li><a href="${pageContext.request.contextPath}/products?category=Stationery">Office & Stationery</a></li>
                    <li><a href="${pageContext.request.contextPath}/products?category=Makeup">Makeup & Cosmetics</a></li>
                </ul>
            </div>
            <div class="footer-col">
                <h4>Quick Links</h4>
                <ul>
                    <li><a href="${pageContext.request.contextPath}/home">Home</a></li>
                    <li><a href="${pageContext.request.contextPath}/products">All Catalog</a></li>
                    <li><a href="${pageContext.request.contextPath}/cart">My Cart</a></li>
                    <li><a href="${pageContext.request.contextPath}/orders">Order Tracking</a></li>
                    <li><a href="${pageContext.request.contextPath}/login">Seller & Admin Login</a></li>
                </ul>
            </div>
            <div class="footer-col">
                <h4>Capstone Tech Stack</h4>
                <ul>
                    <li>Java 17 LTS</li>
                    <li>Tomcat 9 (javax.servlet.*)</li>
                    <li>JSP + JSTL & HikariCP</li>
                    <li>H2 Database & jBCrypt</li>
                </ul>
            </div>
        </div>
        <div class="footer-bottom">
            &copy; 2026 ShopEase E-Commerce Platform. All rights reserved. Built for College Capstone Evaluation.
        </div>
    </div>
</footer>

<!-- Floating AI Chatbot Widget -->
<button class="chatbot-toggler" id="chatbotToggler" title="Ask ShopEase Assistant">
    💬
</button>

<div class="chatbot-container hidden" id="chatbotContainer">
    <div class="chatbot-header">
        <h3>🤖 ShopEase AI Assistant</h3>
        <button class="chatbot-close" id="chatbotClose">&times;</button>
    </div>
    <div class="chatbot-messages" id="chatbotMessages">
        <div class="chat-msg bot">
            Hello! 👋 I am your ShopEase AI Shopping Assistant. Ask me about products in our 7 categories, delivery, mock payment, or tracking your orders!
        </div>
    </div>
    <div class="chatbot-input-area">
        <input type="text" id="chatbotInput" class="chatbot-input" placeholder="Ask a question..." maxlength="500">
        <button id="chatbotSend" class="chatbot-send">Send</button>
    </div>
</div>

<script src="${pageContext.request.contextPath}/static/js/app.js"></script>
<script src="${pageContext.request.contextPath}/static/js/cart.js"></script>
<script src="${pageContext.request.contextPath}/static/js/chatbot.js"></script>
</body>
</html>
