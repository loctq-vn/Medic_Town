package com.example.medictown.data.repositories;

import com.example.medictown.data.api.RetrofitClient;
import com.example.medictown.data.models.CartItemRequest;
import com.example.medictown.data.models.CartItemResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Repository class responsible for handling data operations related to the Cart.
 */
public class CartRepository {

    public interface OnActionCompletedListener {
        void onSuccess();
        void onError(String error);
    }

    public interface OnCartFetchedListener {
        void onSuccess(List<CartItemResponse> items);
        void onError(String error);
    }

    public void addToCart(String cartId, String productId, int quantity, OnActionCompletedListener listener) {
        CartItemRequest request = new CartItemRequest(cartId, productId, quantity);
        RetrofitClient.getApiService().addToCart(request).enqueue(new Callback<Void>()
         {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) listener.onSuccess();
                else listener.onError("Error: " + response.code());
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                listener.onError(t.getMessage());
            }
        });
    }

    public void fetchCartItems(String cartId, OnCartFetchedListener listener) {
        RetrofitClient.getApiService().getCartItems("eq." + cartId, "*,products(*)").enqueue(new Callback<List<CartItemResponse>>() {
            @Override
            public void onResponse(Call<List<CartItemResponse>> call, Response<List<CartItemResponse>> response) {
                if (response.isSuccessful() && response.body() != null) listener.onSuccess(response.body());
                else listener.onError("Error fetching data: " + response.code());
            }

            @Override
            public void onFailure(Call<List<CartItemResponse>> call, Throwable t) {
                listener.onError(t.getMessage());
            }
        });
    }

    public void updateQuantity(String cartItemId, int newQuantity) {
        Map<String, Integer> body = new HashMap<>();
        body.put("quantity", newQuantity);
        RetrofitClient.getApiService().updateQuantity("eq." + cartItemId, body).enqueue(new Callback<Void>() {
            @Override public void onResponse(Call<Void> call, Response<Void> res) {}
            @Override public void onFailure(Call<Void> call, Throwable t) {}
        });
    }

    public void deleteItem(String cartItemId) {
        RetrofitClient.getApiService().deleteItem("eq." + cartItemId).enqueue(new Callback<Void>() {
            @Override public void onResponse(Call<Void> call, Response<Void> res) {}
            @Override public void onFailure(Call<Void> call, Throwable t) {}
        });
    }
}