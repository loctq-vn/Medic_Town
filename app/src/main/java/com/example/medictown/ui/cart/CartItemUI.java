package com.example.medictown.ui.cart;

import com.example.medictown.data.models.Products;

public class CartItemUI {
    private Products product;
    private int quantity;
    private boolean isSelected;

    public CartItemUI(Products product, int quantity) {
        this.product = product;
        this.quantity = quantity;
        this.isSelected = true;
    }

    // --- Các hàm để lấy và sửa dữ liệu ---
    public Products getProduct() { return product; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public boolean isSelected() { return isSelected; }
    public void setSelected(boolean selected) { this.isSelected = selected; }

    // Hàm tính giá tiền thông minh: Nếu có giá sale thì lấy giá sale, không thì lấy giá gốc
    public double getEffectivePrice() {
        if (product.sale_price != null && product.sale_price > 0) {
            return product.sale_price;
        }
        return product.price;
    }
}