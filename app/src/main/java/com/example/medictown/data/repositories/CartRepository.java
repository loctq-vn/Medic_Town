package com.example.medictown.data.repositories;

import com.example.medictown.data.api.RetrofitClient;
import com.example.medictown.data.api.SupabaseApi;
import com.example.medictown.data.api.SupabaseConfig;
import com.example.medictown.data.models.CartItem;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CartRepository {
    private final SupabaseApi apiService;
    private final String apiKey = SupabaseConfig.SUPABASE_ANON_KEY;

    public CartRepository() {
        this.apiService = RetrofitClient.getApiService();
    }

    public void getCartItems(String userId, String token, Callback<List<CartItem>> callback) {
        String authHeader = "Bearer " + apiKey;
        // select=*,products(*) to get product details
        apiService.getCartItems(apiKey, authHeader, "eq." + userId, null, "*,products(*)").enqueue(callback);
    }

    public void addToCart(String userId, String productId, int quantity, String token, Callback<Void> callback) {
        String authHeader = "Bearer " + apiKey;

        // Check if item already exists in cart
        apiService.getCartItems(apiKey, authHeader, "eq." + userId, "eq." + productId, "*").enqueue(new Callback<List<CartItem>>() {
            @Override
            public void onResponse(Call<List<CartItem>> call, Response<List<CartItem>> response) {
                if (response.isSuccessful()) {
                    if (response.body() != null && !response.body().isEmpty()) {
                        // Item exists, update quantity
                        CartItem existingItem = response.body().get(0);
                        int newQuantity = existingItem.quantity + quantity;
                        updateQuantity(existingItem.id, newQuantity, token, callback);
                    } else {
                        // Item doesn't exist, create new
                        CartItem newItem = new CartItem(userId, productId, quantity);
                        apiService.addToCart(apiKey, authHeader, newItem).enqueue(callback);
                    }
                } else {
                    callback.onFailure(null, new Exception("Failed to check cart items: " + response.code()));
                }
            }

            @Override
            public void onFailure(Call<List<CartItem>> call, Throwable t) {
                callback.onFailure(null, t);
            }
        });
    }

    public void updateQuantity(String cartItemId, int quantity, String token, Callback<Void> callback) {
        String authHeader = "Bearer " + apiKey;
        CartItem updateItem = new CartItem();
        updateItem.quantity = quantity;
        apiService.updateCartItem(apiKey, authHeader, "eq." + cartItemId, updateItem).enqueue(callback);
    }

    public void removeFromCart(String cartItemId, String token, Callback<Void> callback) {
        String authHeader = "Bearer " + apiKey;
        apiService.deleteCartItem(apiKey, authHeader, "eq." + cartItemId).enqueue(callback);
    }

    public void clearCart(String userId, String token, Callback<Void> callback) {
        String authHeader = "Bearer " + apiKey;
        apiService.clearCart(apiKey, authHeader, "eq." + userId).enqueue(callback);
    }
}
