package com.example.medictown.data.repositories;

import com.example.medictown.data.api.RetrofitClient;
import com.example.medictown.data.api.SupabaseApi;
import com.example.medictown.data.api.SupabaseConfig;
import com.example.medictown.data.models.OrderItem;
import com.example.medictown.data.models.Orders;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;

public class OrderRepository {
    private final SupabaseApi apiService;

    public OrderRepository() {
        apiService = RetrofitClient.getApiService();
    }

    public void createOrder(Orders order, Callback<List<Orders>> callback) {
        apiService.createOrder(
            SupabaseConfig.SUPABASE_ANON_KEY,
            "Bearer " + SupabaseConfig.SUPABASE_ANON_KEY,
            "return=representation",
            order
        ).enqueue(callback);
    }

    public void createOrderItems(List<OrderItem> orderItems, Callback<Void> callback) {
        apiService.createOrderItems(
            SupabaseConfig.SUPABASE_ANON_KEY,
            "Bearer " + SupabaseConfig.SUPABASE_ANON_KEY,
            orderItems
        ).enqueue(callback);
    }
}
