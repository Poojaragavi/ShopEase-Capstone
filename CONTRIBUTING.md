# Contributing to ShopEase

Thank you for your interest in contributing to the ShopEase College Capstone Project! This document outlines our development workflow, coding conventions, and pull request guidelines.

---

## 1. Code of Conduct
All contributors and maintainers are expected to uphold a respectful, inclusive, and professional environment during discussions, code reviews, and issue tracking.

---

## 2. Technology & Architecture Guidelines
ShopEase strictly follows the classic Java Enterprise Edition (Java EE) Servlet / JSP MVC architecture:
- **Language**: Java 17 LTS.
- **Servlet Specification**: Servlet 4.0 (`javax.servlet.*` on Apache Tomcat 9). Do not introduce `jakarta.*` packages as Tomcat 9 evaluates `javax.*`.
- **Database Access**: All database operations must go through the DAO layer (`com.shopease.dao`) using JDBC `PreparedStatement`. Do not execute inline SQL inside servlets or JSP files.
- **Money Calculations**: Always use `java.math.BigDecimal` for monetary amounts, prices, and discounts. Never use `float` or `double`.
- **View Layer**: JSPs must use JSTL (`c:out`, `c:forEach`, `c:if`) and Expression Language (`${...}`). Scriptlets (`<% ... %>`) are strictly prohibited.

---

## 3. Git Branching & Conventional Commits
We follow a Git Feature-Branch workflow:
- `main`: Production-ready, deployable code.
- `feat/<feature-name>`: New features and capabilities.
- `fix/<bug-name>`: Bug fixes and performance patches.
- `docs/<doc-name>`: Documentation improvements.

### Commit Message Convention
Please adhere to [Conventional Commits](https://www.conventionalcommits.org/):
- `feat:` A new feature for users or API clients.
- `fix:` A bug fix.
- `docs:` Documentation only changes.
- `refactor:` Code changes that neither fix a bug nor add a feature.
- `test:` Adding or updating unit/integration tests.
- `chore:` Build scripts, dependencies, or tool configurations.

---

## 4. Coding Standards & Verification
Before submitting any pull request or committing code, ensure all local verification checks pass:

```bash
# 1. Validate Checkstyle rules (Sun coding standards)
mvn checkstyle:check

# 2. Run all unit and security test suites
mvn test

# 3. Run SpotBugs static analysis
mvn spotbugs:check

# 4. Perform complete clean build and package
mvn clean verify
```

---

## 5. Security Checklist for PRs
When contributing new endpoints or features:
1. Ensure all user inputs are sanitized and validated with `ValidationUtil`.
2. Check that all private endpoints are gated by `AuthFilter` and role checks.
3. Validate multi-tenant seller boundaries: Sellers must not modify products or orders belonging to other sellers.
4. Ensure all SQL parameters use `?` placeholders.
