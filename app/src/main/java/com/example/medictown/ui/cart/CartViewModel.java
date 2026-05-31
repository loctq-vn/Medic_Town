package com.example.medictown.ui.cart;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.medictown.data.repositories.CartRepository;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import com.example.medictown.data.models.CartItem;
import com.example.medictown.data.models.ProductSubcategory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CartViewModel extends ViewModel {
    private final CartRepository cartRepository;
    private final MutableLiveData<String> _addToCartStatus = new MutableLiveData<>();
    public LiveData<String> addToCartStatus = _addToCartStatus;

    private final MutableLiveData<List<CartItem>> _cartItems = new MutableLiveData<>();
    public LiveData<List<CartItem>> cartItems = _cartItems;

    private final MutableLiveData<Map<String, String>> _subcategoryNames = new MutableLiveData<>(new HashMap<>());
    public LiveData<Map<String, String>> subcategoryNames = _subcategoryNames;

    public CartViewModel() {
        cartRepository = new CartRepository();
    }

    public void fetchCartItems(String userId, String token) {
        cartRepository.getCartItems(userId, token, new Callback<List<CartItem>>() {
            @Override
            public void onResponse(Call<List<CartItem>> call, Response<List<CartItem>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    _cartItems.postValue(response.body());
                } else {
                    _cartItems.postValue(null);
                }
            }

            @Override
            public void onFailure(Call<List<CartItem>> call, Throwable t) {
                _cartItems.postValue(null);
            }
        });
    }

    public void fetchProductSubcategories() {
        cartRepository.getProductSubcategories(new Callback<List<ProductSubcategory>>() {
            @Override
            public void onResponse(Call<List<ProductSubcategory>> call, Response<List<ProductSubcategory>> response) {
                Map<String, String> names = new HashMap<>();
                if (response.isSuccessful() && response.body() != null) {
                    for (ProductSubcategory subcategory : response.body()) {
                        if (subcategory.id != null && subcategory.name != null && !subcategory.name.trim().isEmpty()) {
                            names.put(subcategory.id, subcategory.name);
                        }
                    }
                }
                _subcategoryNames.postValue(names);
            }

            @Override
            public void onFailure(Call<List<ProductSubcategory>> call, Throwable t) {
                _subcategoryNames.postValue(new HashMap<>());
            }
        });
    }

    public void updateQuantity(String cartItemId, int quantity, String token, String userId) {
        cartRepository.updateQuantity(cartItemId, quantity, token, new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    fetchCartItems(userId, token);
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {}
        });
    }

    public void removeFromCart(String cartItemId, String token, String userId) {
        cartRepository.removeFromCart(cartItemId, token, new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    fetchCartItems(userId, token);
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {}
        });
    }

    public void clearCart(String userId, String token) {
        cartRepository.clearCart(userId, token, new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    fetchCartItems(userId, token);
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {}
        });
    }

    public void addToCart(String userId, String productId, int quantity, String token) {
        cartRepository.addToCart(userId, productId, quantity, token, new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    _addToCartStatus.postValue("Thêm vào giỏ hàng thành công!");
                } else {
                    _addToCartStatus.postValue("Lỗi: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                _addToCartStatus.postValue("Lỗi kết nối: " + t.getMessage());
            }
        });
    }
}
