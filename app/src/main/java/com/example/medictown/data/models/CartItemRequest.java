package com.example.medictown.data.models;

/**
 * Payload model used when inserting a new item into the database.
 */
public class CartItemRequest {
    public String cart_id;
    public String product_id;
    public int quantity;

    public CartItemRequest(String cart_id, String product_id, int quantity) {
        this.cart_id = cart_id;
        this.product_id = product_id;
        this.quantity = quantity;
    }
}