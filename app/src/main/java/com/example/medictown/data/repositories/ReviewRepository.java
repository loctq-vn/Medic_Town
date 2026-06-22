package com.example.medictown.data.repositories;

import com.example.medictown.data.api.RetrofitClient;
import com.example.medictown.data.api.SupabaseApi;
import com.example.medictown.data.models.Reviews;

import java.util.List;

import retrofit2.Callback;

public class ReviewRepository {
    private final SupabaseApi api;

    public ReviewRepository() {
        this.api = RetrofitClient.getApiService();
    }

    public void checkReviewExists(String orderItemId, Callback<List<Reviews>> callback) {
        api.getReviews(orderItemId, null).enqueue(callback);
    }

    public void getReviewsForOrderItems(List<String> orderItemIds, Callback<List<Reviews>> callback) {
        StringBuilder sb = new StringBuilder("in.(");
        for (int i = 0; i < orderItemIds.size(); i++) {
            sb.append(orderItemIds.get(i));
            if (i < orderItemIds.size() - 1) sb.append(",");
        }
        sb.append(")");

        api.getReviews(sb.toString(), null).enqueue(callback);
    }

    public void getProductReviews(String productId, Callback<List<Reviews>> callback) {
        api.getReviewsByProduct(productId).enqueue(callback);
    }

    public void submitReview(Reviews review, Callback<Void> callback) {
        api.createReview(review).enqueue(callback);
    }
}
