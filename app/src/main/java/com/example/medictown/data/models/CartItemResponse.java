package com.example.medictown.data.models;

/**
 * Response model used when retrieving cart items. Includes the joined Products object.
 */
public class CartItemResponse {
    public String id;
    public String cart_id;
    public String product_id;
    public int quantity;
    public Products products; // Supabase auto-joins this based on foreign key
}