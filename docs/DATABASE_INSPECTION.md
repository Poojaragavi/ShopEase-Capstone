# ShopEase H2 Database Inspection & Faculty Demonstration Guide

This guide explains how you can inspect, query, and demonstrate the persistent **H2 Database** of the **ShopEase** application to your college evaluators and faculty.

---

## 1. Faculty Demonstration Overview

ShopEase uses an **Embedded H2 Database in Persistent File Mode** (`jdbc:h2:file:./data/shopease;DB_CLOSE_DELAY=-1;AUTO_SERVER=TRUE`).
- **Physical Data Files**: Stored on disk in the `./data/` directory (`shopease.mv.db` and `shopease.trace.db`).
- **Auto-Server Mode**: `AUTO_SERVER=TRUE` enables multiple processes (e.g. running Tomcat + H2 Web Console + External SQL client) to query the database concurrently without file locking conflicts.
- **Idempotent Persistence**: Data persists permanently across Tomcat restarts and redeployments.

---

## 2. Method 1: Web-Based H2 Console (Recommended for Demo)

The H2 Database Web Console is embedded directly into the application. No external software or database clients are required.

### Step-by-Step Connection Instructions:
1. Start the ShopEase server or ensure it is running at `http://localhost:8080`.
2. Open your web browser and navigate to:
   👉 **[http://localhost:8080/h2-console](http://localhost:8080/h2-console)**
3. On the login screen, enter the following connection parameters:

| Field | Value | Notes |
| :--- | :--- | :--- |
| **Saved Settings** | `Generic H2 (Embedded)` | Default preset |
| **Driver Class** | `org.h2.Driver` | Pre-filled |
| **JDBC URL** | `jdbc:h2:file:./data/shopease` | If in Docker/Cloud: `jdbc:h2:file:/app/data/shopease` |
| **User Name** | `sa` | Default system administrator user |
| **Password** | *(leave empty)* | No password configured for local dev |

4. Click **"Test Connection"** (verifies connection status).
5. Click **"Connect"**.

---

## 3. Recommended Demonstration SQL Queries for Faculty

Once connected in the H2 Console, you can execute the following queries to showcase the architecture and business data:

### 1. View Applied Database Migrations
```sql
SELECT installed_rank, version, description, success, installed_on 
FROM schema_version 
ORDER BY installed_rank ASC;
```

### 2. View Registered User Accounts (Role-Based Access)
```sql
SELECT id, name, email, role, created_at 
FROM users 
ORDER BY id ASC;
```

### 3. View Pre-Seeded Product Catalog Across 7 Categories
```sql
SELECT id, name, category, price, original_price, discount_pct, stock_qty, avg_rating, review_count 
FROM products 
ORDER BY category, id;
```

### 4. View Customer Orders & Payment Transactions
```sql
SELECT o.id AS order_id, o.customer_name, o.total_amount, o.status, 
       p.payment_method, p.transaction_id, o.created_at
FROM orders o
LEFT JOIN payments p ON o.id = p.order_id
ORDER BY o.id DESC;
```

### 5. View Order Line Items
```sql
SELECT oi.order_id, p.name AS product_name, p.category, oi.quantity, oi.unit_price, oi.subtotal
FROM order_items oi
JOIN products p ON oi.product_id = p.id
ORDER BY oi.order_id DESC;
```

### 6. View Verified Buyer Reviews
```sql
SELECT r.id, u.name AS reviewer, p.name AS product_name, r.rating, r.comment, r.created_at
FROM reviews r
JOIN users u ON r.user_id = u.id
JOIN products p ON r.product_id = p.id
ORDER BY r.created_at DESC;
```

---

## 4. Method 2: GUI Database Tools (DBeaver, IntelliJ IDEA, VS Code)

If your college evaluation requires using a standard database IDE:

1. Open **DBeaver** or **IntelliJ Database Tool Window**.
2. Create a new connection $\to$ Select **H2 Embedded**.
3. **Database file path**: `<path-to-project>/data/shopease` (omit `.mv.db` extension).
4. **JDBC URL**: `jdbc:h2:file:./data/shopease;AUTO_SERVER=TRUE`
5. **Username**: `sa` | **Password**: *(empty)*.
6. Test and connect.

---

## 5. Method 3: Built-In Admin Portal Demonstration

You can also demonstrate persistent data through the ShopEase UI itself:
1. Navigate to **[http://localhost:8080/login](http://localhost:8080/login)**
2. Click **Demo Admin** (or enter `admin@shopease.com` / `admin123`).
3. Click **Admin Console** in top navigation:
   - **User Registry** (`/admin/users`): Live list of all buyers, sellers, admins.
   - **Catalog Moderation** (`/admin/products`): Full product management.
   - **System Orders** (`/admin/orders`): All customer transactions and order fulfillment tracking.
