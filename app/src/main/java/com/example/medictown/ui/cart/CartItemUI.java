package com.example.medictown.ui.cart;

import com.example.medictown.data.models.Products;

/**
 * UI wrapper class to hold product data, quantity, and selection status for the RecyclerView.
 */
public class CartItemUI {
    private String cartItemId; // ID từ bảng cart_items
    private Products product;
    private int quantity;
    private boolean isSelected;

    public CartItemUI(String cartItemId, Products product, int quantity) {
        this.cartItemId = cartItemId;
        this.product = product;
        this.quantity = quantity;
        this.isSelected = true;
    }

    public String getCartItemId() { return cartItemId; }
    public Products getProduct() { return product; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public boolean isSelected() { return isSelected; }
    public void setSelected(boolean selected) { this.isSelected = selected; }

    public double getEffectivePrice() {
        if (product.sale_price != null && product.sale_price > 0) return product.sale_price;
        return product.price;
    }
}