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
    @GET("products")
    Call<List<Products>> searchProducts(
        @Header("apikey") String apiKey,
        @Header("Authorization") String authToken,
        @Query("name") String nameFilter,
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

    // Orders Endpoints
    @POST("orders")
    Call<List<com.example.medictown.data.models.Orders>> createOrder(
        @Header("apikey") String apiKey,
        @Header("Authorization") String authToken,
        @Header("Prefer") String prefer,
        @Body com.example.medictown.data.models.Orders order
    );

    @POST("order_items")
    Call<Void> createOrderItems(
        @Header("apikey") String apiKey,
        @Header("Authorization") String authToken,
        @Body List<com.example.medictown.data.models.OrderItem> orderItems
    );

    @GET("orders")
    Call<List<com.example.medictown.data.models.Orders>> getOrders(
        @Header("apikey") String apiKey,
        @Header("Authorization") String authToken,
        @Query("user_id") String userIdFilter,
        @Query("select") String select,
        @Query("order") String orderBy
    );

    // Reviews Endpoints
    @GET("reviews")
    Call<List<com.example.medictown.data.models.Reviews>> getReviews(
        @Header("apikey") String apiKey,
        @Header("Authorization") String authToken,
        @Query("order_item_id") String orderItemId,
        @Query("select") String select
    );

    @GET("reviews")
    Call<List<com.example.medictown.data.models.Reviews>> getReviewsByProduct(
        @Header("apikey") String apiKey,
        @Header("Authorization") String authToken,
        @Query("product_id") String productId,
        @Query("select") String select
    );

    @POST("reviews")
    Call<Void> createReview(
        @Header("apikey") String apiKey,
        @Header("Authorization") String authToken,
        @Body com.example.medictown.data.models.Reviews review
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

    @POST("token?grant_type=id_token")
    Call<AuthResponse> loginWithGoogle(
        @Header("apikey") String apiKey,
        @Body com.example.medictown.data.models.GoogleAuthRequest request
    );
}
