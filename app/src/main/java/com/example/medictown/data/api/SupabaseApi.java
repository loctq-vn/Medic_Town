package com.example.medictown.data.api;

import com.example.medictown.data.models.Address;
import com.example.medictown.data.models.AuthRequest;
import com.example.medictown.data.models.AuthResponse;
import com.example.medictown.data.models.CartItem;
import com.example.medictown.data.models.Products;
import com.example.medictown.data.models.Users;

import java.util.List;

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
    @POST("address")
    Call<Void> addAddress(
            @Header("apikey") String apiKey,
            @Header("Authorization") String authToken,
            @Body Address address
    );
    @PATCH("address")
    Call<Void> setAddress(
            @Header("apikey") String apiKey,
            @Header("Authorization") String authToken,
            @Query("id") String id,
            @Body Address address
    );
    @GET("address")
    Call<List<Address>> getAddress(
            @Header("apikey") String apiKey,
            @Header("Authorization") String authToken,
            @Query("select") String select,
            @Query("user_id") String userid
    );
    @DELETE("address")
    Call<Void> deleteAddress(
            @Header("apikey") String apiKey,
            @Header("Authorization") String authToken,
            @Query("id") String id
    );

    // Cart Endpoints
    @GET("cart_items")
    Call<List<CartItem>> getCartItems(
        @Header("apikey") String apiKey,
        @Header("Authorization") String authToken,
        @Query("user_id") String userId,
        @Query("product_id") String productId,
        @Query("select") String select
    );

    @POST("cart_items")
    Call<Void> addToCart(
        @Header("apikey") String apiKey,
        @Header("Authorization") String authToken,
        @Body CartItem cartItem
    );

    @PATCH("cart_items")
    Call<Void> updateCartItem(
        @Header("apikey") String apiKey,
        @Header("Authorization") String authToken,
        @Query("id") String id,
        @Body CartItem cartItem
    );

    @DELETE("cart_items")
    Call<Void> deleteCartItem(
        @Header("apikey") String apiKey,
        @Header("Authorization") String authToken,
        @Query("id") String id
    );

    @DELETE("cart_items")
    Call<Void> clearCart(
        @Header("apikey") String apiKey,
        @Header("Authorization") String authToken,
        @Query("user_id") String userId
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
}
