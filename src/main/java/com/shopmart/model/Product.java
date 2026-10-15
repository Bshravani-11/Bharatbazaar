package com.shopmart.model;

/**
 * Product class - Represents a single product in the store.
 * 
 * OOP Concepts demonstrated:
 *   - Encapsulation: private fields with public getters/setters
 *   - Constructor overloading
 */
public class Product {

    // ===== Fields (private - encapsulation) =====
    private Long id;
    private String name;
    private String description;
    private double price;
    private String category;    // e.g., "Electronics", "Clothing"
    private String imageUrl;
    private int stock;          // how many items are available

    // ===== Constructors =====

    // Default constructor (no arguments)
    public Product() {
    }

    // Parameterized constructor
    public Product(Long id, String name, String description, double price,
                   String category, String imageUrl, int stock) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.category = category;
        this.imageUrl = imageUrl;
        this.stock = stock;
    }

    // ===== Getters and Setters =====

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    // ===== Helper Methods =====

    /**
     * Check if product is available in stock.
     */
    public boolean isInStock() {
        return stock > 0;
    }

    /**
     * String representation for debugging/logging.
     */
    @Override
    public String toString() {
        return "Product{id=" + id + ", name='" + name + "', price=" + price + "}";
    }
}
