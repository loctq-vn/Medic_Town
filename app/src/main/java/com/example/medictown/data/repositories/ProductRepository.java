package com.example.medictown.data.repositories;

import com.example.medictown.data.api.SupabaseConfig;
import com.example.medictown.data.api.RetrofitClient;
import com.example.medictown.data.api.SupabaseApi;
import com.example.medictown.data.models.Products;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProductRepository {
    private final SupabaseApi apiService;

    public ProductRepository() {
        this.apiService = RetrofitClient.getApiService();
    }

    public void getAllProducts(Callback<List<Products>> callback) {
        apiService.getProducts(
            SupabaseConfig.SUPABASE_ANON_KEY,
            "Bearer " + SupabaseConfig.SUPABASE_ANON_KEY,
            "*"
        ).enqueue(callback);
    }

    public void getFeaturedProducts(Callback<List<Products>> callback) {
        apiService.getFeaturedProducts(
            SupabaseConfig.SUPABASE_ANON_KEY,
            "Bearer " + SupabaseConfig.SUPABASE_ANON_KEY,
            "true",
            "true",
            "*"
        ).enqueue(callback);
    }
}
