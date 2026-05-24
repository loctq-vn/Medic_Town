package com.example.medictown.data.repositories;

import com.example.medictown.data.api.RetrofitClient;
import com.example.medictown.data.api.SupabaseApi;
import com.example.medictown.data.models.Products;
import com.example.medictown.data.models.Shop;

import java.util.List;

import retrofit2.Callback;

public class ShopRepository {
    private final SupabaseApi apiService;

    public ShopRepository() {
        this.apiService = RetrofitClient.getApiService();
    }

    public void getMyShops(Callback<List<Shop>> callback) {
        apiService.getMyShops().enqueue(callback);
    }

    public void createShop(Shop shop, Callback<Shop> callback) {
        apiService.createShop(shop).enqueue(callback);
    }

    public void getShop(String shopId, Callback<Shop> callback) {
        apiService.getShop(shopId).enqueue(callback);
    }

    public void updateShop(String shopId, Shop shop, Callback<Shop> callback) {
        apiService.updateShop(shopId, shop).enqueue(callback);
    }

    public void createProduct(String shopId, Products product, Callback<Products> callback) {
        apiService.createShopProduct(shopId, product).enqueue(callback);
    }

    public void updateProduct(String shopId, String productId, Products product, Callback<Products> callback) {
        apiService.updateShopProduct(shopId, productId, product).enqueue(callback);
    }
}
