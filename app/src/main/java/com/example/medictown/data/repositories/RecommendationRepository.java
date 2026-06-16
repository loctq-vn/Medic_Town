package com.example.medictown.data.repositories;

import com.example.medictown.data.api.RetrofitClient;
import com.example.medictown.data.api.SupabaseApi;
import com.example.medictown.data.models.ProductEventRequest;
import com.example.medictown.data.models.Products;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RecommendationRepository {
    private final SupabaseApi apiService;

    public RecommendationRepository() {
        this.apiService = RetrofitClient.getApiService();
    }

    public void recordEvent(String productId, String eventType) {
        recordEvent(productId, eventType, Collections.emptyMap());
    }

    public void recordEvent(String productId, String eventType, Map<String, Object> metadata) {
        if (productId == null || productId.trim().isEmpty()) {
            return;
        }
        Map<String, Object> safeMetadata = metadata == null ? Collections.emptyMap() : metadata;
        ProductEventRequest request = new ProductEventRequest(productId, eventType, safeMetadata);
        apiService.recordProductEvent(request).enqueue(noOpCallback());
    }

    public void getMyRecommendations(int limit, Callback<List<Products>> callback) {
        apiService.getMyRecommendations(limit).enqueue(callback);
    }

    public void getProductRecommendations(String productId, int limit, Callback<List<Products>> callback) {
        apiService.getProductRecommendations(productId, limit).enqueue(callback);
    }

    private Callback<Void> noOpCallback() {
        return new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
            }

            @Override
            public void onFailure(Call<Void> call, Throwable throwable) {
            }
        };
    }
}
