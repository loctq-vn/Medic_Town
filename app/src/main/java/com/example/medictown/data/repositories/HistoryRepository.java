package com.example.medictown.data.repositories;

import com.example.medictown.data.api.RetrofitClient;
import com.example.medictown.data.api.SupabaseApi;
import com.example.medictown.data.models.Orders;

import java.util.List;

import retrofit2.Callback;

public class HistoryRepository {
    private final SupabaseApi apiService;

    public HistoryRepository() {
        this.apiService = RetrofitClient.getApiService();
    }

    public void getUserOrders(String userId, Callback<List<Orders>> callback) {
        apiService.getOrders().enqueue(callback);
    }
}
