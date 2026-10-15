package com.shopmart.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Cart class - Represents the shopping cart.
 * 
 * OOP Concepts demonstrated:
 *   - Collections: ArrayList to store cart items
 *   - Iteration: loops and stream operations
 *   - Business logic: calculations for totals, tax, shipping
 */
public class Cart {

    // ===== Constants (Indian E-Commerce Standards) =====
    private static final double GST_RATE = 0.05;                  // 5% GST
    private static final double SHIPPING_FEE = 49.0;              // flat delivery fee ₹49
    private static final double FREE_SHIPPING_THRESHOLD = 499.0;  // free delivery above ₹499

    // ===== Fields =====
    private List<CartItem> items;   // list of items in cart

    // ===== Constructor =====
    public Cart() {
        this.items = new ArrayList<>();
    }

    // ===== Getters =====
    public List<CartItem> getItems() {
        return items;
    }

    // ===== Cart Operations =====

    /**
     * Add a product to the cart.
     * If the product already exists, increase its quantity.
     * Otherwise, add it as a new cart item.
     */
    public void addItem(Product product) {
        // Check if product is already in cart
        for (CartItem item : items) {
            if (item.getProduct().getId().equals(product.getId())) {
                // Product found - increase quantity
                item.incrementQuantity();
                return;
            }
        }
        // Product not in cart - add new item with quantity 1
        items.add(new CartItem(product, 1));
    }

    /**
     * Remove a product from the cart by product ID.
     */
    public void removeItem(Long productId) {
        // Use removeIf to remove matching item
        items.removeIf(item -> item.getProduct().getId().equals(productId));
    }

    /**
     * Update the quantity of a specific product.
     * If quantity becomes 0 or less, remove the item.
     */
    public void updateQuantity(Long productId, int newQuantity) {
        if (newQuantity <= 0) {
            removeItem(productId);
            return;
        }
        for (CartItem item : items) {
            if (item.getProduct().getId().equals(productId)) {
                item.setQuantity(Math.min(newQuantity, 10)); // max 10
                return;
            }
        }
    }

    /**
     * Find a cart item by product ID.
     * Returns null if not found.
     */
    public CartItem findItem(Long productId) {
        for (CartItem item : items) {
            if (item.getProduct().getId().equals(productId)) {
                return item;
            }
        }
        return null;
    }

    // ===== Calculations =====

    /**
     * Calculate subtotal (sum of all item subtotals).
     */
    public double getSubtotal() {
        double total = 0;
        for (CartItem item : items) {
            total += item.getSubtotal();
        }
        return Math.round(total * 100.0) / 100.0;  // round to 2 decimal places
    }

    /**
     * Calculate shipping cost.
     * Free if subtotal >= $50, otherwise $5.99.
     */
    public double getShipping() {
        if (getSubtotal() >= FREE_SHIPPING_THRESHOLD) {
            return 0.0;
        }
        return SHIPPING_FEE;
    }

    /**
     * Calculate GST (5% Goods and Services Tax).
     */
    public double getTax() {
        return Math.round(getSubtotal() * GST_RATE * 100.0) / 100.0;
    }

    /**
     * Calculate grand total (subtotal + shipping + tax).
     */
    public double getTotal() {
        return Math.round((getSubtotal() + getShipping() + getTax()) * 100.0) / 100.0;
    }

    /**
     * Get total number of items in cart (sum of all quantities).
     */
    public int getTotalItems() {
        int count = 0;
        for (CartItem item : items) {
            count += item.getQuantity();
        }
        return count;
    }

    /**
     * Check if cart is empty.
     */
    public boolean isEmpty() {
        return items.isEmpty();
    }

    /**
     * Clear all items from cart.
     */
    public void clear() {
        items.clear();
    }

    /**
     * Get how much more the user needs to spend for free shipping.
     */
    public double getAmountToFreeShipping() {
        double remaining = FREE_SHIPPING_THRESHOLD - getSubtotal();
        return remaining > 0 ? Math.round(remaining * 100.0) / 100.0 : 0;
    }

    @Override
    public String toString() {
        return "Cart{items=" + items.size() + ", total=" + getTotal() + "}";
    }
}
