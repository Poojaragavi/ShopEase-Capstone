package com.shopease.util;

import com.shopease.dao.ProductDAO;
import com.shopease.dao.UserDAO;
import com.shopease.factory.DaoFactory;
import com.shopease.model.Product;
import com.shopease.model.Role;
import com.shopease.model.User;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Seeds initial demo accounts and realistic Indian catalog products across 7 categories.
 * Executes idempotently: preserves existing data on restart without overwriting or duplicating.
 */
public final class DatabaseSeeder {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseSeeder.class);

    private DatabaseSeeder() {
        // Utility class
    }

    public static void seedIfEmpty() {
        UserDAO userDAO = DaoFactory.getUserDAO();
        ProductDAO productDAO = DaoFactory.getProductDAO();

        logger.info("Checking database seed status...");

        // 1. Seed Demo Accounts
        seedUser(userDAO, "Aditi Sharma", "buyer@shopease.com", "buyer123", Role.BUYER);
        User seller = seedUser(userDAO, "Rajesh Superstore", "seller@shopease.com", "seller123", Role.SELLER);
        seedUser(userDAO, "ShopEase Administrator", "admin@shopease.com", "admin123", Role.ADMIN);

        // 2. Seed Products if catalog is empty
        if (productDAO.findAll().isEmpty() && seller != null && seller.getId() != null) {
            logger.info("Catalog is empty. Seeding realistic Indian products across 7 categories...");
            Long sellerId = seller.getId();

            // 1. Grocery
            seedProduct(productDAO, sellerId, "India Gate Super Basmati Rice (5kg)",
                    "Aromatic aged long-grain basmati rice perfect for biryani, pulao, and everyday feast.",
                    "Grocery", new BigDecimal("499.00"), new BigDecimal("650.00"), 45,
                    "https://images.unsplash.com/photo-1586201375761-83865001e31c?w=500&auto=format&fit=crop&q=60");

            seedProduct(productDAO, sellerId, "Fortune Sunlite Refined Sunflower Oil (1L)",
                    "Light and healthy edible cooking oil enriched with Vitamins A and D.",
                    "Grocery", new BigDecimal("135.00"), new BigDecimal("160.00"), 80,
                    "https://images.unsplash.com/photo-1474979266404-7eaacbcd87c5?w=500&auto=format&fit=crop&q=60");

            seedProduct(productDAO, sellerId, "Aashirvaad Superior MP Whole Wheat Atta (10kg)",
                    "100% pure whole wheat flour processed with traditional stone chakki grinding.",
                    "Grocery", new BigDecimal("415.00"), new BigDecimal("480.00"), 30,
                    "https://images.unsplash.com/photo-1509440159596-0249088772ff?w=500&auto=format&fit=crop&q=60");

            seedProduct(productDAO, sellerId, "Tata Salt Vacuum Evaporated Iodized Salt (1kg)",
                    "Desh ka namak. Pure iodized salt ensuring mental and physical health.",
                    "Grocery", new BigDecimal("28.00"), new BigDecimal("30.00"), 120,
                    "https://images.unsplash.com/photo-1518110925495-5fe2f303f2ea?w=500&auto=format&fit=crop&q=60");

            seedProduct(productDAO, sellerId, "Catch Royal Shahi Garam Masala (100g)",
                    "A rich blend of handpicked authentic whole spices for delicious Indian gravies.",
                    "Grocery", new BigDecimal("82.00"), new BigDecimal("95.00"), 3, // Low stock demo
                    "https://images.unsplash.com/photo-1596040033229-a9821ebd058d?w=500&auto=format&fit=crop&q=60");

            seedProduct(productDAO, sellerId, "Organic Kashmiri Saffron / Kesar (1g)",
                    "Premium Grade-1 pure Kashmiri Mongra saffron threads for desserts and milk.",
                    "Grocery", new BigDecimal("349.00"), new BigDecimal("420.00"), 0, // Out of stock demo
                    "https://images.unsplash.com/photo-1599940824399-b87987ceb72a?w=500&auto=format&fit=crop&q=60");

            // 2. Fragrance
            seedProduct(productDAO, sellerId, "Bella Vita Luxury Man Organic Perfume (100ml)",
                    "Woody and musky long-lasting Eau De Parfum designed for day and night elegance.",
                    "Fragrance", new BigDecimal("649.00"), new BigDecimal("999.00"), 25,
                    "https://images.unsplash.com/photo-1523293182086-7651a899d37f?w=500&auto=format&fit=crop&q=60");

            seedProduct(productDAO, sellerId, "Fogg Xtremo Scent For Men (100ml)",
                    "Intense concentrated perfume spray with zero gas, offering 800+ aromatic sprays.",
                    "Fragrance", new BigDecimal("320.00"), new BigDecimal("500.00"), 50,
                    "https://images.unsplash.com/photo-1592945403244-b3fbafd7f539?w=500&auto=format&fit=crop&q=60");

            seedProduct(productDAO, sellerId, "Ajmal Wisal Dhabab Oriental EDP (50ml)",
                    "Exquisite blend of floral top notes, spicy heart, and woody ambery base.",
                    "Fragrance", new BigDecimal("1499.00"), new BigDecimal("2200.00"), 4, // Low stock demo
                    "https://images.unsplash.com/photo-1541643600914-78b084683601?w=500&auto=format&fit=crop&q=60");

            seedProduct(productDAO, sellerId, "Plum BodyLovin' Vanilla Vibes Mist (150ml)",
                    "Sweet, warm vanilla body mist infused with soothing aloe juice.",
                    "Fragrance", new BigDecimal("375.00"), new BigDecimal("525.00"), 35,
                    "https://images.unsplash.com/photo-1594035910387-fea47794261f?w=500&auto=format&fit=crop&q=60");

            seedProduct(productDAO, sellerId, "Pure Sandalwood / Chandan Natural Attar (12ml)",
                    "Alcohol-free roll-on traditional Indian attar crafted from pure botanical extracts.",
                    "Fragrance", new BigDecimal("499.00"), new BigDecimal("650.00"), 0, // Out of stock demo
                    "https://images.unsplash.com/photo-1615397349754-cfa2066a298e?w=500&auto=format&fit=crop&q=60");

            // 3. Juice
            seedProduct(productDAO, sellerId, "Real Fruit Power Alphonso Mango Juice (1L)",
                    "Rich nectar prepared from handpicked Ratnagiri Alphonso mangoes.",
                    "Juice", new BigDecimal("115.00"), new BigDecimal("130.00"), 60,
                    "https://images.unsplash.com/photo-1546833998-877b37c2e5c6?w=500&auto=format&fit=crop&q=60");

            seedProduct(productDAO, sellerId, "Tropicana 100% Orange Delight Juice (1L)",
                    "No added sugar juice loaded with natural Vitamin C for your morning vitality.",
                    "Juice", new BigDecimal("140.00"), new BigDecimal("160.00"), 40,
                    "https://images.unsplash.com/photo-1613478223719-2ab802602423?w=500&auto=format&fit=crop&q=60");

            seedProduct(productDAO, sellerId, "Raw Pressery Pomegranate Cold Pressed Juice (250ml)",
                    "100% pure cold-pressed ruby red pomegranate juice packed with antioxidants.",
                    "Juice", new BigDecimal("99.00"), new BigDecimal("120.00"), 2, // Low stock demo
                    "https://images.unsplash.com/photo-1557800636-894a64c1696f?w=500&auto=format&fit=crop&q=60");

            seedProduct(productDAO, sellerId, "Paper Boat Aam Panna Traditional Drink (1L)",
                    "Refreshing raw mango cooler seasoned with roasted cumin and black salt.",
                    "Juice", new BigDecimal("110.00"), new BigDecimal("125.00"), 55,
                    "https://images.unsplash.com/photo-1534353473418-4cfa6c56fd38?w=500&auto=format&fit=crop&q=60");

            seedProduct(productDAO, sellerId, "B Natural Himalayan Mixed Fruit Juice (1L)",
                    "Crafted using fruits nurtured in the pristine orchards of the Himalayas.",
                    "Juice", new BigDecimal("105.00"), new BigDecimal("120.00"), 35,
                    "https://images.unsplash.com/photo-1600271886742-f049cd451bba?w=500&auto=format&fit=crop&q=60");

            // 4. Ice Cream
            seedProduct(productDAO, sellerId, "Amul Gold Gourmet Belgian Chocolate Tub (1L)",
                    "Rich dark chocolate gelato loaded with crunchy roasted almonds and fudge ripple.",
                    "Ice Cream", new BigDecimal("275.00"), new BigDecimal("320.00"), 20,
                    "https://images.unsplash.com/photo-1570197788417-0e82375c9371?w=500&auto=format&fit=crop&q=60");

            seedProduct(productDAO, sellerId, "Kwality Wall's Feast Chocolate Crunch Bar (4x90ml)",
                    "Creamy chocolate core wrapped in crispy wafer and dark chocolate shell.",
                    "Ice Cream", new BigDecimal("180.00"), new BigDecimal("200.00"), 30,
                    "https://images.unsplash.com/photo-1563805042-7684c019e1cb?w=500&auto=format&fit=crop&q=60");

            seedProduct(productDAO, sellerId, "Naturals Authentic Sitaphal / Custard Apple Tub (500ml)",
                    "Made with fresh custard apple pulp, full-cream milk, and pure cane sugar.",
                    "Ice Cream", new BigDecimal("190.00"), new BigDecimal("220.00"), 5, // Low stock demo
                    "https://images.unsplash.com/photo-1501443762994-82bd5dace89a?w=500&auto=format&fit=crop&q=60");

            seedProduct(productDAO, sellerId, "Mother Dairy Shahi Kesar Pista Kulfi (Pack of 5)",
                    "Slow-cooked rabdi kulfi infused with Kashmiri saffron threads and pistachio nuts.",
                    "Ice Cream", new BigDecimal("225.00"), new BigDecimal("250.00"), 25,
                    "https://images.unsplash.com/photo-1516559828984-fb3b99548b21?w=500&auto=format&fit=crop&q=60");

            seedProduct(productDAO, sellerId, "Baskin Robbins Mississippi Mud Ice Cream (450ml)",
                    "Decadent milk chocolate fudge with brownie chunks and fudge ribbon.",
                    "Ice Cream", new BigDecimal("349.00"), new BigDecimal("410.00"), 0, // Out of stock demo
                    "https://images.unsplash.com/photo-1497034825429-c343d7c6a68f?w=500&auto=format&fit=crop&q=60");

            // 5. Snacks
            seedProduct(productDAO, sellerId, "Haldiram's Nagpur Bhujia Sev (1kg)",
                    "Crunchy and spicy moth bean flour noodles seasoned with rich Rajasthani spices.",
                    "Snacks", new BigDecimal("260.00"), new BigDecimal("299.00"), 50,
                    "https://images.unsplash.com/photo-1599490659213-e2b9527bd087?w=500&auto=format&fit=crop&q=60");

            seedProduct(productDAO, sellerId, "Lay's India's Magic Masala Chips (Pack of 4x50g)",
                    "Crispy ridged potato chips seasoned with authentic zesty Indian spice blend.",
                    "Snacks", new BigDecimal("80.00"), new BigDecimal("80.00"), 75,
                    "https://images.unsplash.com/photo-1566478989037-eec170784d0b?w=500&auto=format&fit=crop&q=60");

            seedProduct(productDAO, sellerId, "Happilo California Roasted & Salted Almonds (500g)",
                    "Crunchy non-GMO California badam packed with vitamin E and healthy proteins.",
                    "Snacks", new BigDecimal("449.00"), new BigDecimal("625.00"), 4, // Low stock demo
                    "https://images.unsplash.com/photo-1508061253366-f7da158b6d46?w=500&auto=format&fit=crop&q=60");

            seedProduct(productDAO, sellerId, "Sunfeast Dark Fantasy Choco Fills Biscuits (300g)",
                    "Crispy chocolate crust biscuit filled with molten chocolate hazelnut cream.",
                    "Snacks", new BigDecimal("130.00"), new BigDecimal("160.00"), 40,
                    "https://images.unsplash.com/photo-1558961363-fa8fdf82db35?w=500&auto=format&fit=crop&q=60");

            seedProduct(productDAO, sellerId, "Bikaji Bikaneri Soan Papdi Festive Pack (500g)",
                    "Flaky, melt-in-mouth traditional desi ghee sweet garnished with almonds and pistachios.",
                    "Snacks", new BigDecimal("175.00"), new BigDecimal("210.00"), 30,
                    "https://images.unsplash.com/photo-1541781774459-bb2af2f05b55?w=500&auto=format&fit=crop&q=60");

            // 6. Stationery
            seedProduct(productDAO, sellerId, "Classmate Pulse Hardbound Notebook (Pack of 6, 200 Pages)",
                    "Single-ruled premium chlorine-free paper notebooks with sturdy attractive cover designs.",
                    "Stationery", new BigDecimal("380.00"), new BigDecimal("450.00"), 40,
                    "https://images.unsplash.com/photo-1583485088034-697b5bc54ccd?w=500&auto=format&fit=crop&q=60");

            seedProduct(productDAO, sellerId, "Parker Beta Premium Gold Fountain Pen",
                    "Smooth writing stainless steel nib fountain pen with golden finish accents.",
                    "Stationery", new BigDecimal("299.00"), new BigDecimal("375.00"), 20,
                    "https://images.unsplash.com/photo-1585336261026-4186644199b8?w=500&auto=format&fit=crop&q=60");

            seedProduct(productDAO, sellerId, "Faber-Castell Triangular Colour Pencils (Set of 24)",
                    "Vibrant break-resistant soft lead pencils for students and sketching artists.",
                    "Stationery", new BigDecimal("185.00"), new BigDecimal("220.00"), 3, // Low stock demo
                    "https://images.unsplash.com/photo-1513542789411-b6a5d4f31634?w=500&auto=format&fit=crop&q=60");

            seedProduct(productDAO, sellerId, "Camlin Kokuyo Mathematical Drawing Instrument Box",
                    "Self-centering compass and divider kit with high-transparency precision rulers.",
                    "Stationery", new BigDecimal("140.00"), new BigDecimal("160.00"), 35,
                    "https://images.unsplash.com/photo-1509228468518-180dd4864904?w=500&auto=format&fit=crop&q=60");

            seedProduct(productDAO, sellerId, "3M Post-it Neon Sticky Notes Cube (400 Sheets)",
                    "Repositionable adhesive notes in vibrant neon colors for reminders and study indexing.",
                    "Stationery", new BigDecimal("195.00"), new BigDecimal("240.00"), 0, // Out of stock demo
                    "https://images.unsplash.com/photo-1586075010923-2dd4570fb338?w=500&auto=format&fit=crop&q=60");

            // 7. Makeup
            seedProduct(productDAO, sellerId, "Maybelline SuperStay Matte Ink Liquid Lipstick (5ml)",
                    "Flawless 16-hour transfer-proof saturated matte liquid color in shade Pioneer.",
                    "Makeup", new BigDecimal("549.00"), new BigDecimal("699.00"), 30,
                    "https://images.unsplash.com/photo-1586495777744-4413f21062fa?w=500&auto=format&fit=crop&q=60");

            seedProduct(productDAO, sellerId, "Lakme Eyeconic Waterproof Kajal Pencil (0.35g)",
                    "Deep black 24-hour smudge-proof dermatologically tested kajal with intense matte payoff.",
                    "Makeup", new BigDecimal("180.00"), new BigDecimal("210.00"), 80,
                    "https://images.unsplash.com/photo-1522337360788-8b13dee7a37e?w=500&auto=format&fit=crop&q=60");

            seedProduct(productDAO, sellerId, "Sugar Cosmetics All Set To Go Banana Face Powder",
                    "Ultra-fine setting powder that blurs pores, controls oil shine, and locks makeup.",
                    "Makeup", new BigDecimal("499.00"), new BigDecimal("599.00"), 5, // Low stock demo
                    "https://images.unsplash.com/photo-1512496015851-a90fb38ba796?w=500&auto=format&fit=crop&q=60");

            seedProduct(productDAO, sellerId, "Swiss Beauty Ultimate 9-Colour Eyeshadow Palette",
                    "Silky blendable shades featuring high-pigment matte and shimmer metallic finishes.",
                    "Makeup", new BigDecimal("249.00"), new BigDecimal("299.00"), 25,
                    "https://images.unsplash.com/photo-1516975080664-ed2fc6a32937?w=500&auto=format&fit=crop&q=60");

            seedProduct(productDAO, sellerId, "Mamaearth Naturally Matte Lip Serum (3.5ml)",
                    "Hydrating Vitamin C and Rosehip oil lip color that nourishes with zero dryness.",
                    "Makeup", new BigDecimal("399.00"), new BigDecimal("499.00"), 20,
                    "https://images.unsplash.com/photo-1596462502278-27bfdc403348?w=500&auto=format&fit=crop&q=60");

            logger.info("Successfully seeded 35+ realistic Indian products across 7 categories!");
        } else {
            logger.info("Database already contains catalog products. Preserving persistent state.");
        }
    }

    private static User seedUser(UserDAO userDAO, String name, String email, String password, Role role) {
        String normalized = email.trim().toLowerCase();
        Optional<User> existing = userDAO.findByEmail(normalized);
        if (existing.isPresent()) {
            return existing.get();
        }
        User user = new User();
        user.setName(name);
        user.setEmail(normalized);
        user.setPasswordHash(PasswordUtil.hashPassword(password));
        user.setRole(role);
        user.setCreatedAt(LocalDateTime.now());
        User created = userDAO.create(user);
        logger.info("Seeded demo account: {} ({})", email, role);
        return created;
    }

    private static void seedProduct(ProductDAO productDAO, Long sellerId, String name, String desc,
                                    String category, BigDecimal price, BigDecimal origPrice, int stock, String img) {
        Product p = new Product();
        p.setSellerId(sellerId);
        p.setName(name);
        p.setDescription(desc);
        p.setCategory(category);
        p.setPrice(price);
        p.setOriginalPrice(origPrice);
        p.setStockQty(stock);
        p.setImageUrl(img);
        p.calculateDiscount();
        productDAO.create(p);
    }
}
