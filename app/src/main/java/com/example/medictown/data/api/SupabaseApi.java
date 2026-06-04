package com.example.medictown.data.api;

import com.example.medictown.data.models.Address;
import com.example.medictown.data.models.AuthRequest;
import com.example.medictown.data.models.AuthResponse;
import com.example.medictown.data.models.CartItem;
import com.example.medictown.data.models.FakePaymentMethodRequest;
import com.example.medictown.data.models.GoogleAuthRequest;
import com.example.medictown.data.models.OrderCreateRequest;
import com.example.medictown.data.models.Orders;
import com.example.medictown.data.models.Payments;
import com.example.medictown.data.models.ProductCategory;
import com.example.medictown.data.models.ProductSubcategory;
import com.example.medictown.data.models.Products;
import com.example.medictown.data.models.RevenueDashboard;
import com.example.medictown.data.models.Reviews;
import com.example.medictown.data.models.Shop;
import com.example.medictown.data.models.Users;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.Path;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface SupabaseApi {
    @GET("api/products")
    Call<List<Products>> getProducts(
            @Query("category_id") String categoryId,
            @Query("subcategory_id") String subcategoryId,
            @Query("limit") int limit,
            @Query("offset") int offset
    );

    @GET("api/products/featured")
    Call<List<Products>> getFeaturedProducts(
            @Query("limit") int limit,
            @Query("offset") int offset
    );

    @GET("api/products")
    Call<List<Products>> searchProducts(
            @Query("search") String search,
            @Query("category_id") String categoryId,
            @Query("subcategory_id") String subcategoryId,
            @Query("limit") int limit,
            @Query("offset") int offset
    );

    @GET("api/products/categories")
    Call<List<ProductCategory>> getProductCategories();

    @GET("api/products/subcategories")
    Call<List<ProductSubcategory>> getProductSubcategories(
            @Query("category_id") String categoryId
    );

    @GET("api/users/me")
    Call<Users> getUser();

    @PATCH("api/users/me")
    Call<Void> updateUser(@Body Users user);

    @POST("api/address")
    Call<Void> addAddress(@Body Address address);

    @PATCH("api/address")
    Call<Void> setAddress(
            @Query("id") String id,
            @Body Address address
    );

    @GET("api/address")
    Call<List<Address>> getAddress();

    @DELETE("api/address")
    Call<Void> deleteAddress(@Query("id") String id);

    @GET("api/cart_items")
    Call<List<CartItem>> getCartItems(@Query("product_id") String productId);

    @POST("api/cart_items")
    Call<Void> addToCart(@Body CartItem cartItem);

    @PATCH("api/cart_items")
    Call<Void> updateCartItem(
            @Query("id") String id,
            @Body CartItem cartItem
    );

    @DELETE("api/cart_items")
    Call<Void> deleteCartItems(
            @Query("id") String id,
            @Query("ids") String ids
    );

    @POST("api/orders")
    Call<List<Orders>> createOrder(@Body OrderCreateRequest order);

    @POST("api/payments/momo/checkout")
    Call<Payments> createMomoCheckout(@Body OrderCreateRequest order);

    @POST("api/payments/fake/method")
    Call<Payments> createFakePaymentMethod(@Body FakePaymentMethodRequest request);

    @GET("api/orders")
    Call<List<Orders>> getOrders();

    @GET("api/reviews")
    Call<List<Reviews>> getReviews(
            @Query("order_item_id") String orderItemId,
            @Query("product_id") String productId
    );

    @GET("api/reviews/products/{product_id}")
    Call<List<Reviews>> getReviewsByProduct(@retrofit2.http.Path("product_id") String productId);

    @POST("api/reviews")
    Call<Void> createReview(@Body Reviews review);

    @GET("api/shops/my")
    Call<List<Shop>> getMyShops();

    @POST("api/shops")
    Call<Shop> createShop(@Body Shop shop);

    @GET("api/shops/{shop_id}")
    Call<Shop> getShop(@Path("shop_id") String shopId);

    @PATCH("api/shops/{shop_id}")
    Call<Shop> updateShop(
            @Path("shop_id") String shopId,
            @Body Shop shop
    );

    @GET("api/shops/{shop_id}/products")
    Call<List<Products>> getShopProducts(@Path("shop_id") String shopId);

    @POST("api/shops/{shop_id}/products")
    Call<Products> createShopProduct(
            @Path("shop_id") String shopId,
            @Body Products product
    );

    @PATCH("api/shops/{shop_id}/products/{product_id}")
    Call<Products> updateShopProduct(
            @Path("shop_id") String shopId,
            @Path("product_id") String productId,
            @Body Products product
    );

    @PATCH("api/shops/{shop_id}/products/{product_id}")
    Call<Products> updateShopProductFields(
            @Path("shop_id") String shopId,
            @Path("product_id") String productId,
            @Body java.util.Map<String, Object> update
    );

    @GET("api/shops/{shop_id}/orders")
    Call<List<Orders>> getShopOrders(@Path("shop_id") String shopId);

    @PATCH("api/shops/{shop_id}/orders/{order_id}/status")
    Call<Orders> updateShopOrderStatus(
            @Path("shop_id") String shopId,
            @Path("order_id") String orderId,
            @Body java.util.Map<String, Object> update
    );

    @GET("api/revenue/dashboard")
    Call<RevenueDashboard> getRevenueDashboard(
            @Query("shop_id") String shopId,
            @Query("from") String fromDate,
            @Query("to") String toDate,
            @Query("groupBy") String groupBy
    );

    @POST("api/auth/register")
    Call<AuthResponse> signUp(@Body AuthRequest request);

    @POST("api/auth/login")
    Call<AuthResponse> login(@Body AuthRequest request);

    @POST("api/auth/google")
    Call<AuthResponse> loginWithGoogle(@Body GoogleAuthRequest request);
}
