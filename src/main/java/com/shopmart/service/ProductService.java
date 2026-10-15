package com.shopmart.service;

import com.shopmart.model.Product;

import java.util.ArrayList;
import java.util.List;

/**
 * ProductService - Manages the product catalog.
 * 
 * In a real application, this would fetch data from a database.
 * For this project, we use a hardcoded list (in-memory storage).
 * 
 * OOP Concepts:
 *   - Service class encapsulates business logic and data access
 *   - ArrayList and List operations
 *   - Filtering and searching logic
 */
public class ProductService {

    // In-memory product catalog
    private List<Product> products;

    /**
     * Constructor - Initialize the product catalog with sample data.
     */
    public ProductService() {
        products = new ArrayList<>();

        // ===== Adding Indian Products to Catalog (Prices in INR ₹) =====

        products.add(new Product(1L,
                "boAt Rockerz 450 Bluetooth Headphones",
                "High bass on-ear wireless headphones with 40mm dynamic drivers and 15-hour playback.",
                1499.00, "Electronics",
                "/images/1-boat-headphones.jpg", 25));

        products.add(new Product(2L,
                "FabIndia Men's Pure Cotton Kurta",
                "Handcrafted comfortable ethnic cotton kurta in vibrant indigo blue. Ideal for festive and casual wear.",
                1199.00, "Fashion",
                "/images/2-cotton-kurta.jpg", 30));

        products.add(new Product(3L,
                "Banarasi Silk Saree with Zari Border",
                "Traditional handwoven art silk saree with ornate floral zari weaving and matching blouse piece.",
                1899.00, "Fashion",
                "/images/3-banarasi-saree.jpg", 20));

        products.add(new Product(4L,
                "Prestige Tri-Ply Stainless Steel Flask (1000ml)",
                "Double-wall insulated bottle that keeps water hot or cold for 24 hours. BPA free and leak-proof.",
                749.00, "Home & Kitchen",
                "/images/4-prestige-flask.jpg", 40));

        products.add(new Product(5L,
                "Sparx Men's Lightweight Running Shoes",
                "Breathable mesh sports shoes with cushioned EVA sole for high grip and daily jogging comfort.",
                999.00, "Footwear",
                "/images/5-sparx-shoes.jpg", 35));

        products.add(new Product(6L,
                "Mi 10000mAh Power Bank (18W Fast Charge)",
                "Dual output USB-A & Type-C fast charging power bank with 12 layers of circuit protection.",
                1299.00, "Electronics",
                "/images/6-mi-powerbank.jpg", 50));

        products.add(new Product(7L,
                "Handcrafted Terracotta Ceramic Chai Cups (Set of 6)",
                "Traditional Kulhad-style ceramic tea cups made by Indian artisans. Microwave and dishwasher safe.",
                499.00, "Home & Kitchen",
                "/images/7-terracotta-cups.jpg", 45));

        products.add(new Product(8L,
                "Dabur Organic Kashmiri Wild Honey (500g)",
                "100% pure raw forest honey rich in natural antioxidants. Certified organic with no added sugar.",
                349.00, "Grocery",
                "/images/8-wild-honey.jpg", 60));

        products.add(new Product(9L,
                "Noise ColorFit Pulse 2 Max Smartwatch",
                "1.85-inch display smartwatch with Bluetooth calling, 150+ watch faces, SpO2 & heart rate monitor.",
                1799.00, "Electronics",
                "/images/9-smartwatch.jpg", 20));

        products.add(new Product(10L,
                "Wildcraft 35L Water Resistant Travel Backpack",
                "Ergonomic travel and college backpack with dedicated laptop sleeve, rain cover, and mesh bottle holder.",
                1399.00, "Accessories",
                "/images/10-travel-backpack.jpg", 28));

        products.add(new Product(11L,
                "Fastrack Polarized Aviator Sunglasses",
                "Classic aviator sunglasses with UV400 protective polarized lenses and lightweight metallic frame.",
                899.00, "Accessories",
                "/images/11-aviator-sunglasses.jpg", 40));

        products.add(new Product(12L,
                "Tata Tea Gold (1kg) Premium Blend",
                "Exquisite blend of Assam CTC teas with gently rolled 15% long leaves for rich aroma and brisk taste.",
                460.00, "Grocery",
                "/images/12-tata-tea-gold.jpg", 55));
    }

    // ===== Service Methods =====

    /**
     * Get all products.
     */
    public List<Product> getAllProducts() {
        return products;
    }

    /**
     * Find a product by its ID.
     * Returns null if not found.
     */
    public Product getProductById(Long id) {
        for (Product product : products) {
            if (product.getId().equals(id)) {
                return product;
            }
        }
        return null;
    }

    /**
     * Get products filtered by category.
     * If category is "all" or null, return all products.
     */
    public List<Product> getProductsByCategory(String category) {
        if (category == null || category.equals("all")) {
            return products;
        }

        List<Product> filtered = new ArrayList<>();
        for (Product product : products) {
            if (product.getCategory().equalsIgnoreCase(category)) {
                filtered.add(product);
            }
        }
        return filtered;
    }

    /**
     * Search products by name or description.
     * Case-insensitive search.
     */
    public List<Product> searchProducts(String query) {
        if (query == null || query.trim().isEmpty()) {
            return products;
        }

        String searchTerm = query.toLowerCase();
        List<Product> results = new ArrayList<>();

        for (Product product : products) {
            // Check if query matches name, description, or category
            if (product.getName().toLowerCase().contains(searchTerm) ||
                product.getDescription().toLowerCase().contains(searchTerm) ||
                product.getCategory().toLowerCase().contains(searchTerm)) {
                results.add(product);
            }
        }
        return results;
    }

    /**
     * Get all unique category names.
     */
    public List<String> getAllCategories() {
        List<String> categories = new ArrayList<>();
        for (Product product : products) {
            if (!categories.contains(product.getCategory())) {
                categories.add(product.getCategory());
            }
        }
        return categories;
    }
}
