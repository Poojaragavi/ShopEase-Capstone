package com.shopease.controller;

import com.shopease.dto.ProductDTO;
import com.shopease.service.CartService;
import com.shopease.service.ProductService;
import java.io.IOException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet handling Home / Landing page displaying categories, featured deals, and new arrivals.
 */
@WebServlet(name = "HomeServlet", urlPatterns = {"", "/home"})
public class HomeServlet extends BaseServlet {
    private final ProductService productService = new ProductService();
    private final CartService cartService = new CartService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        List<ProductDTO> allProducts = productService.getAllProducts();
        List<String> categories = productService.getAllCategories();

        req.setAttribute("featuredProducts", allProducts.stream().limit(8).toList());
        req.setAttribute("discountedProducts", allProducts.stream()
                .filter(p -> p.getDiscountPercentage() != null && p.getDiscountPercentage().doubleValue() > 0)
                .limit(6).toList());
        req.setAttribute("categories", categories);

        req.getRequestDispatcher("/WEB-INF/views/buyer/home.jsp").forward(req, resp);
    }
}
