package com.example.medictown.data.repositories;

import com.example.medictown.data.api.RetrofitClient;
import com.example.medictown.data.api.SupabaseApi;
import com.example.medictown.data.api.SupabaseConfig;
import com.example.medictown.data.models.Orders;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;

public class HistoryRepository {
    private final SupabaseApi apiService;
    private final String apiKey = SupabaseConfig.SUPABASE_ANON_KEY;

    public HistoryRepository() {
        this.apiService = RetrofitClient.getApiService();
    }

    public void getUserOrders(String userId, Callback<List<Orders>> callback) {
        String authHeader = "Bearer " + apiKey;
        // Fetch orders with order_items using select=*,order_items(*)
        // Order by created_at descending
        apiService.getOrders(apiKey, authHeader, "eq." + userId, "*,order_items(*)", "created_at.desc")
                .enqueue(callback);
    }
}
