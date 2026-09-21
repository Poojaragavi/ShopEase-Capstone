# Changelog

All notable changes to the **ShopEase** Multi-Seller E-Commerce project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/), and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

---

## [1.1.0] - 2026-09-21

### Added
- Integrated Google Gemini 1.5 Flash AI shopping assistant provider with fallback `MockChatProvider`.
- Added IP-based in-memory rate limiting (10 queries / minute) and in-memory LRU query cache for AI chatbot responses.
- Implemented transactional order fulfillment state machine (`PENDING` -> `CONFIRMED` -> `SHIPPED` -> `DELIVERED`).
- Added verified-buyer review constraint enforcing delivered order association before review publication.
- Integrated automated Checkstyle and SpotBugs static code quality gates in Maven build pipeline.

### Improved
- Formatted all monetary amounts in Indian Rupees (`₹ INR`) with 2-decimal precision.
- Optimized HikariCP connection pool settings with 10 maximum connections and 30-second connection timeout.
- Enhanced responsive CSS styling for mobile and desktop screens with sticky navigation and modal dialogs.

---

## [1.0.0] - 2026-09-21

### Added
- Core Java EE Servlet 4.0 / JSP MVC application architecture.
- Full database schema with Flyway-style SQL migrations (`V1__init_schema.sql`, `V2__create_indexes.sql`).
- Embedded H2 database integration with HikariCP connection pooling.
- 6 Gang of Four design patterns: DAO, Front Controller, Singleton, Factory, Strategy, and Builder.
- Role-Based Access Control (RBAC) supporting `BUYER`, `SELLER`, and `ADMIN` roles.
- Multi-seller catalog management with 35+ seeded Indian consumer products across 7 categories.
- Shopping cart, checkout, order management, and user profile servlets.
- Complete versioned REST API (`/api/v1/*`) with standard JSON envelope.
- 32 Unit, DAO, and Security tests using JUnit 5 and Mockito.
