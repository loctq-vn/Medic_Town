package com.example.medictown.data.api;

import com.example.medictown.data.models.Products;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;

public interface SupabaseApi {
    @GET("products")
    Call<List<Products>> getProducts(
        @Header("apikey") String apiKey,
        @Header("Authorization") String authToken,
        @Query("select") String select
    );

    @GET("products")
    Call<List<Products>> getFeaturedProducts(
        @Header("apikey") String apiKey,
        @Header("Authorization") String authToken,
        @Query("is_featured") String isFeatured,
        @Query("is_active") String isActive,
        @Query("select") String select
    );
}
