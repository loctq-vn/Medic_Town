package com.example.medictown.data.api;

import com.example.medictown.data.models.AuthRequest;
import com.example.medictown.data.models.AuthResponse;
import com.example.medictown.data.models.CartItemRequest;
import com.example.medictown.data.models.CartItemResponse;
import com.example.medictown.data.models.Products;
import com.example.medictown.data.models.Users;

import java.util.List;
import java.util.Map;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Path;
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
    @GET("users")
    Call<Users> getUser(
        @Header("apikey") String apiKey,
        @Header("Authorization") String authToken,
        @Header("Accept") String accept,
        @Query("select") String select,
        @Query("id") String id
    );
    @PATCH("users")
    Call<Void> updateUser(
        @Header("apikey") String apiKey,
        @Header("Authorization") String authToken,
        @Query("id") String id,
        @Body Users user
    );

    // Auth Endpoints
    @POST("signup")
    Call<AuthResponse> signUp(
        @Header("apikey") String apiKey,
        @Body AuthRequest request
    );

    @POST("token?grant_type=password")
    Call<AuthResponse> login(
        @Header("apikey") String apiKey,
        @Body AuthRequest request
    );

    @POST("rest/v1/cart_items")
    Call<Void> addToCart(@Body CartItemRequest body);

    @GET("rest/v1/cart_items")
    Call<List<CartItemResponse>> getCartItems(
            @Query("cart_id") String cartIdOperator,
            @Query("select") String selectQuery
    );

    @PATCH("rest/v1/cart_items")
    Call<Void> updateQuantity(
            @Query("id") String itemIdOperator,
            @Body Map<String, Integer> body
    );

    @DELETE("rest/v1/cart_items")
    Call<Void> deleteItem(
            @Query("id") String itemIdOperator
    );
}
