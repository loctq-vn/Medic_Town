package com.example.medictown.data.repositories;

import com.example.medictown.data.api.RetrofitClient;
import com.example.medictown.data.api.SupabaseApi;
import com.example.medictown.data.models.Orders;

import java.util.List;

import retrofit2.Callback;

public class OrderRepository {
    private final SupabaseApi apiService;

    public OrderRepository() {
        apiService = RetrofitClient.getApiService();
    }

    public void createOrder(Orders order, Callback<List<Orders>> callback) {
        apiService.createOrder(order).enqueue(callback);
    }
}
