package com.shopmart.model;

/**
 * CartItem class - Represents one item in the shopping cart.
 * 
 * Each CartItem links a Product with a quantity.
 * 
 * OOP Concepts demonstrated:
 *   - Composition: CartItem HAS-A Product (object within object)
 *   - Encapsulation: calculated field (subtotal) via method
 */
public class CartItem {

    // ===== Fields =====
    private Product product;    // the product added to cart
    private int quantity;       // how many of this product

    // ===== Constructors =====

    public CartItem() {
    }

    public CartItem(Product product, int quantity) {
        this.product = product;
        this.quantity = quantity;
    }

    // ===== Getters and Setters =====

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    // ===== Business Logic =====

    /**
     * Calculate subtotal for this cart item.
     * Subtotal = price × quantity
     */
    public double getSubtotal() {
        return product.getPrice() * quantity;
    }

    /**
     * Increase quantity by 1 (max 10 per product).
     */
    public void incrementQuantity() {
        if (quantity < 10) {
            quantity++;
        }
    }

    /**
     * Decrease quantity by 1 (minimum 1).
     */
    public void decrementQuantity() {
        if (quantity > 1) {
            quantity--;
        }
    }

    @Override
    public String toString() {
        return "CartItem{product=" + product.getName() +
               ", qty=" + quantity +
               ", subtotal=" + getSubtotal() + "}";
    }
}
