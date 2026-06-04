package com.example.medictown.data.repositories;

import com.example.medictown.data.api.RetrofitClient;
import com.example.medictown.data.api.SupabaseApi;
import com.example.medictown.data.models.FakePaymentMethodRequest;
import com.example.medictown.data.models.OrderCreateRequest;
import com.example.medictown.data.models.Orders;
import com.example.medictown.data.models.Payments;

import java.util.List;

import retrofit2.Callback;

public class OrderRepository {
    private final SupabaseApi apiService;

    public OrderRepository() {
        apiService = RetrofitClient.getApiService();
    }

    public void createOrder(OrderCreateRequest order, Callback<List<Orders>> callback) {
        apiService.createOrder(order).enqueue(callback);
    }

    public void createMomoCheckout(OrderCreateRequest order, Callback<Payments> callback) {
        apiService.createMomoCheckout(order).enqueue(callback);
    }

    public void createFakePaymentMethod(FakePaymentMethodRequest request, Callback<Payments> callback) {
        apiService.createFakePaymentMethod(request).enqueue(callback);
    }
}
