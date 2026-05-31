package com.example.medictown.data.repositories;

import com.example.medictown.data.api.RetrofitClient;
import com.example.medictown.data.api.SupabaseApi;
import com.example.medictown.data.models.CartItem;
import com.example.medictown.data.models.ProductSubcategory;

import java.util.List;

import retrofit2.Callback;
import retrofit2.Response;

public class CartRepository {
    private final SupabaseApi apiService;

    public CartRepository() {
        this.apiService = RetrofitClient.getApiService();
    }

    public void getCartItems(String userId, String token, Callback<List<CartItem>> callback) {
        RetrofitClient.setAuthToken(token);
        apiService.getCartItems(null).enqueue(callback);
    }

    public void getProductSubcategories(Callback<List<ProductSubcategory>> callback) {
        apiService.getProductSubcategories(null).enqueue(callback);
    }

    public void addToCart(String userId, String productId, int quantity, String token, Callback<Void> callback) {
        RetrofitClient.setAuthToken(token);
        CartItem newItem = new CartItem(null, productId, quantity);
        apiService.addToCart(newItem).enqueue(callback);
    }

    public void updateQuantity(String cartItemId, int quantity, String token, Callback<Void> callback) {
        RetrofitClient.setAuthToken(token);
        CartItem updateItem = new CartItem();
        updateItem.quantity = quantity;
        apiService.updateCartItem(cartItemId, updateItem).enqueue(callback);
    }

    public void removeFromCart(String cartItemId, String token, Callback<Void> callback) {
        RetrofitClient.setAuthToken(token);
        apiService.deleteCartItems(cartItemId, null).enqueue(callback);
    }

    public void removeItemsFromCart(List<String> cartItemIds, Callback<Void> callback) {
        if (cartItemIds == null || cartItemIds.isEmpty()) {
            callback.onResponse(null, Response.success(null));
            return;
        }

        StringBuilder idFilter = new StringBuilder("in.(");
        for (int i = 0; i < cartItemIds.size(); i++) {
            idFilter.append(cartItemIds.get(i));
            if (i < cartItemIds.size() - 1) {
                idFilter.append(",");
            }
        }
        idFilter.append(")");

        apiService.deleteCartItems(null, idFilter.toString()).enqueue(callback);
    }

    public void clearCart(String userId, String token, Callback<Void> callback) {
        RetrofitClient.setAuthToken(token);
        apiService.deleteCartItems(null, null).enqueue(callback);
    }
}
