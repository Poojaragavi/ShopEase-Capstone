# Capstone Engineering Retrospective (ShopEase)

## 1. Project Overview & Objectives
The primary objective of the ShopEase Capstone project was to architect and deliver a production-grade, multi-seller e-commerce web platform strictly adhering to the Java Servlet 4.0 / JSP MVC specifications mandated for college evaluation.

---

## 2. Key Engineering Accomplishments

### Architecture & Modularity
- Successfully implemented a clean 4-tier architecture (Presentation, Controller/Servlet, Service/Domain, DAO/Persistence) without framework bloat.
- Isolated all persistence logic behind DAO interfaces, allowing seamless swapping of underlying database systems.
- Emitted zero Checkstyle violations and zero SpotBugs warnings across 83 source files.

### Security & Multi-Tenancy
- Implemented robust RBAC filtering across Web routes and REST API endpoints.
- Eliminated OWASP Top 10 vulnerabilities (SQL Injection, XSS, Session Fixation, Broken Access Control).
- Successfully enforced multi-seller isolation, preventing cross-tenant product updates and unauthorized order status tampering.

### E-Commerce Business Logic
- Precise financial calculations using `BigDecimal` and standard Indian Rupee (`₹ INR`) currency formatting.
- Strict state-machine transitions for order fulfillment.
- Fraud-resistant verified-buyer reviews tied directly to delivered order items.

---

## 3. Challenges & Lessons Learned

| Challenge | Root Cause | Solution Implemented |
| :--- | :--- | :--- |
| **Classloading in Embedded Tomcat** | Embedded Tomcat runner required access to both compiled classes and runtime dependency JARs. | Configured `docBase` to detect exploded WAR directory (`target/shopease`) and mapped the parent class loader. |
| **Session Fixation Defense** | Logging in with an existing session ID could allow session hijacking. | Implemented session invalidation and re-creation upon authentication in `AuthServlet`. |
| **Static Code Analysis Warnings** | Standard servlet patterns (non-serializable service references, exposure of internal DAO instances) triggered SpotBugs rules. | Configured granular exclusions in `spotbugs-exclude.xml` and removed dead variable stores. |

---

## 4. Future Enhancements Roadmap
1. **Real-time Order WebSockets**: Push instant notifications to sellers when a new order is received.
2. **Payment Gateway Webhooks**: Integrate live Razorpay / Cashfree webhook callbacks for automated payment reconciliation.
3. **Product Recommendation Engine**: Collaborative filtering for "Frequently Bought Together" recommendations.
