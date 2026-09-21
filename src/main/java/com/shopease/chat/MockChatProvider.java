package com.shopease.chat;

import java.util.Locale;

/**
 * High-performance offline FAQ-aware Mock Chat Provider for ShopEase.
 * Answers domain-specific questions about categories, payment, orders, sellers, reviews, and delivery.
 */
public class MockChatProvider implements ChatProvider {

    @Override
    public String getReply(String userMessage, String context) {
        if (userMessage == null || userMessage.trim().isEmpty()) {
            return "Hello! I am your ShopEase AI Shopping Assistant. How can I help you find products, track orders, or explore categories today?";
        }

        String msg = userMessage.toLowerCase(Locale.ENGLISH).trim();

        if (msg.contains("hello") || msg.contains("hi") || msg.contains("hey")) {
            return "Hello and welcome to ShopEase! 🛒 I'm here to help you browse our 7 product categories, check current deals, or assist with your cart and orders.";
        }

        if (msg.contains("category") || msg.contains("categories") || msg.contains("what do you sell") || msg.contains("catalog")) {
            return "ShopEase offers 7 distinct product categories: \n" +
                    "1. Grocery (Rice, Flour, Oils, Spices)\n" +
                    "2. Fragrance (Perfumes, Deodorants, Attar)\n" +
                    "3. Juice (Fresh Mango, Orange, Guava, Apple)\n" +
                    "4. Ice Cream (Vanilla, Belgian Chocolate, Kulfi, Mango)\n" +
                    "5. Snacks (Masala Chips, Cookies, Dry Fruits)\n" +
                    "6. Stationery (Notebooks, Pen Sets, Art Kits)\n" +
                    "7. Makeup (Lipsticks, Kohl, Foundations, Palettes).\n" +
                    "You can filter by category directly from our Products page!";
        }

        if (msg.contains("grocery") || msg.contains("rice") || msg.contains("oil") || msg.contains("spice") || msg.contains("flour")) {
            return "Yes! ShopEase features a rich Grocery department stocked with everyday staples like Basmati Rice, Cold-Pressed Mustard & Sunflower Oils, Whole Wheat Atta, and aromatic Indian spices at attractive ₹ INR discounts.";
        }

        if (msg.contains("ice cream") || msg.contains("dessert") || msg.contains("kulfi")) {
            return "Our Ice Cream category includes classic Vanilla bean, rich Belgian Chocolate tubs, authentic Malai Kulfi, and exotic Alphonso Mango gelato delivered in temperature-controlled packaging.";
        }

        if (msg.contains("fragrance") || msg.contains("perfume") || msg.contains("deodorant")) {
            return "Explore premium fragrances on ShopEase! We feature long-lasting Eau de Parfum, refreshing daily body sprays, and traditional floral attars from verified sellers.";
        }

        if (msg.contains("juice") || msg.contains("drink") || msg.contains("beverage")) {
            return "Our Juice selection offers 100% natural fruit juices without added preservatives: Mango Nectar, Valencia Orange, Pink Guava, and Crisp Himalayan Apple.";
        }

        if (msg.contains("stationery") || msg.contains("notebook") || msg.contains("pen") || msg.contains("book")) {
            return "ShopEase Stationery includes premium hardbound notebooks, gel & fountain pen sets, sticky notes, and complete student geometry and sketching sets.";
        }

        if (msg.contains("makeup") || msg.contains("cosmetic") || msg.contains("lipstick") || msg.contains("beauty")) {
            return "Our Makeup section includes matte liquid lipsticks, waterproof kajal, lightweight CC creams, and vibrant eyeshadow palettes for all skin tones.";
        }

        if (msg.contains("order") || msg.contains("track") || msg.contains("status")) {
            return "You can track your orders by navigating to 'My Orders' from the top navigation bar. Order statuses update in real time from PENDING ➔ CONFIRMED ➔ SHIPPED ➔ DELIVERED.";
        }

        if (msg.contains("pay") || msg.contains("payment") || msg.contains("upi") || msg.contains("card") || msg.contains("cod")) {
            return "ShopEase uses an instant simulated Mock Payment Gateway supporting simulated UPI, Debit/Credit Cards, and Net Banking in ₹ INR. No real money or card details are charged during this capstone demonstration.";
        }

        if (msg.contains("review") || msg.contains("rating") || msg.contains("feedback") || msg.contains("star")) {
            return "Verified buyers can review products! Once your order status reaches 'DELIVERED', an 'Add Review' button appears on your order details page allowing 1–5 star ratings and feedback.";
        }

        if (msg.contains("seller") || msg.contains("sell") || msg.contains("vendor") || msg.contains("merchant")) {
            return "ShopEase is a multi-seller platform! Registered sellers have access to a dedicated Seller Dashboard to list products, manage inventory stock, view incoming orders, and track sales revenue.";
        }

        if (msg.contains("save for later") || msg.contains("saved")) {
            return "You can save items for later directly in your Cart! Click 'Save for Later' on any cart line item to move it to your saved list, and click 'Move to Cart' whenever you are ready to buy.";
        }

        if (msg.contains("delivery") || msg.contains("shipping") || msg.contains("pincode")) {
            return "ShopEase delivers across India! During checkout, simply provide your shipping address and 6-digit postal pincode for express door delivery.";
        }

        if (msg.contains("discount") || msg.contains("deal") || msg.contains("offer") || msg.contains("price")) {
            return "All products on ShopEase are priced in Indian Rupees (₹ INR). Look out for products marked with discount badges showing up to 30%–50% savings off original retail prices!";
        }

        if (msg.contains("return") || msg.contains("refund") || msg.contains("cancel")) {
            return "Orders can be cancelled before dispatch when status is PENDING or CONFIRMED. For delivered items, ShopEase supports a 7-day hassle-free replacement policy for defective or damaged goods.";
        }

        return "Thank you for asking! ShopEase is your trusted multi-seller marketplace offering Groceries, Fragrances, Juices, Ice Creams, Snacks, Stationery, and Makeup. Please let me know if you would like recommendations or help with your order!";
    }

    @Override
    public String getProviderName() {
        return "MockChatProvider";
    }
}
