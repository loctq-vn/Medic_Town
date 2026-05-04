package com.example.medictown.data.models;

import java.util.Date;

public class CartItem {
    public String id;
    public String user_id;
    public String product_id;
    public int quantity;
    public Date added_at;
    public Date updated_at;

    // To hold joined product data (matches key in JSON when using select=*,products(*))
    public Products products;

    // Local UI state
    public transient boolean isSelected = true;

    public CartItem() {}

    public CartItem(String user_id, String product_id, int quantity) {
        this.user_id = user_id;
        this.product_id = product_id;
        this.quantity = quantity;
    }
}
