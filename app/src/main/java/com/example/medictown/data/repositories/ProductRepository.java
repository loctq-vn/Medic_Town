package com.example.medictown.data.repositories;

import com.example.medictown.data.api.RetrofitClient;
import com.example.medictown.data.api.SupabaseApi;
import com.example.medictown.data.models.Products;

import java.util.List;

import retrofit2.Callback;

public class ProductRepository {
    private static final int DEFAULT_LIMIT = 100;
    private final SupabaseApi apiService;

    public ProductRepository() {
        this.apiService = RetrofitClient.getApiService();
    }

    public void getAllProducts(Callback<List<Products>> callback) {
        apiService.getProducts(DEFAULT_LIMIT, 0).enqueue(callback);
    }

    public void getFeaturedProducts(Callback<List<Products>> callback) {
        apiService.getFeaturedProducts(DEFAULT_LIMIT, 0).enqueue(callback);
    }

    public void searchProducts(String query, Callback<List<Products>> callback) {
        apiService.searchProducts(query, DEFAULT_LIMIT, 0).enqueue(callback);
    }
}
