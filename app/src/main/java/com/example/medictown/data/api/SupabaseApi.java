package com.example.medictown.data.api;

import com.example.medictown.data.models.Address;
import com.example.medictown.data.models.Advertisement;
import com.example.medictown.data.models.AdvertisementRequest;
import com.example.medictown.data.models.AuthRequest;
import com.example.medictown.data.models.AuthResponse;
import com.example.medictown.data.models.CartItem;
import com.example.medictown.data.models.ChatMessage;
import com.example.medictown.data.models.ChatMessagePage;
import com.example.medictown.data.models.ChatMessageRequest;
import com.example.medictown.data.models.ChatReadRequest;
import com.example.medictown.data.models.ChatReadResult;
import com.example.medictown.data.models.Conversation;
import com.example.medictown.data.models.FakePaymentMethodRequest;
import com.example.medictown.data.models.GoogleAuthRequest;
import com.example.medictown.data.models.OrderCreateRequest;
import com.example.medictown.data.models.Orders;
import com.example.medictown.data.models.Payments;
import com.example.medictown.data.models.ProductCategory;
import com.example.medictown.data.models.ProductSubcategory;
import com.example.medictown.data.models.Products;
import com.example.medictown.data.models.RevenueDailySummary;
import com.example.medictown.data.models.RevenueDashboard;
import com.example.medictown.data.models.Reviews;
import com.example.medictown.data.models.Shop;
import com.example.medictown.data.models.SellerConversationItem;
import com.example.medictown.data.models.Users;
import com.example.medictown.data.models.DeviceTokenRequest;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.Path;
import retrofit2.http.POST;
import retrofit2.http.Query;
import retrofit2.http.HTTP;
import retrofit2.http.PUT;

public interface SupabaseApi {
    @GET("api/ads")
    Call<List<Advertisement>> getAds(
            @Query("position") String position,
            @Query("limit") int limit
    );

    @POST("api/ads/{ad_id}/view")
    Call<Void> recordAdView(@Path("ad_id") String adId);

    @POST("api/ads/{ad_id}/click")
    Call<Void> recordAdClick(@Path("ad_id") String adId);

    @GET("api/products")
    Call<List<Products>> getProducts(
            @Query("category_id") String categoryId,
            @Query("subcategory_id") String subcategoryId,
            @Query("shop_id") String shopId,
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
            @Query("shop_id") String shopId,
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

    @GET("api/shops/{shop_id}/ads")
    Call<List<Advertisement>> getShopAdvertisements(
            @Path("shop_id") String shopId,
            @Query("search") String search,
            @Query("status") String status,
            @Query("position") String position,
            @Query("limit") int limit,
            @Query("offset") int offset,
            @Query("fetch_all") boolean fetchAll
    );

    @POST("api/shops/{shop_id}/ads")
    Call<Advertisement> createShopAdvertisement(
            @Path("shop_id") String shopId,
            @Body AdvertisementRequest advertisement
    );

    @PATCH("api/shops/{shop_id}/ads/{ad_id}")
    Call<Advertisement> updateShopAdvertisement(
            @Path("shop_id") String shopId,
            @Path("ad_id") String advertisementId,
            @Body AdvertisementRequest advertisement
    );

    @PATCH("api/shops/{shop_id}/ads/{ad_id}/status")
    Call<Advertisement> updateShopAdvertisementStatus(
            @Path("shop_id") String shopId,
            @Path("ad_id") String advertisementId,
            @Body java.util.Map<String, Object> update
    );

    @DELETE("api/shops/{shop_id}/ads/{ad_id}")
    Call<Void> deleteShopAdvertisement(
            @Path("shop_id") String shopId,
            @Path("ad_id") String advertisementId
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

    @GET("api/revenue/daily-summary")
    Call<RevenueDailySummary> getRevenueDailySummary(
            @Query("shop_id") String shopId,
            @Query("from") String fromDate,
            @Query("to") String toDate
    );

    @GET("api/revenue/top-products")
    Call<List<RevenueDashboard.TopProduct>> getRevenueTopProducts(
            @Query("shop_id") String shopId,
            @Query("from") String fromDate,
            @Query("to") String toDate
    );

    @POST("api/auth/register")
    Call<AuthResponse> signUp(@Body AuthRequest request);

    @POST("api/auth/login")
    Call<AuthResponse> login(@Body AuthRequest request);

    @POST("api/auth/google")
    Call<AuthResponse> loginWithGoogle(@Body GoogleAuthRequest request);

    @POST("api/chat/conversation")
    Call<Conversation> getOrCreateChatConversation();

    @GET("api/chat/messages")
    Call<ChatMessagePage> getChatMessages(
            @Query("conversation_id") String conversationId,
            @Query("before") String before,
            @Query("before_id") String beforeId,
            @Query("after") String after,
            @Query("after_id") String afterId,
            @Query("limit") int limit
    );

    @POST("api/chat/messages")
    Call<ChatMessage> sendChatMessage(@Body ChatMessageRequest request);

    @PATCH("api/chat/read")
    Call<ChatReadResult> markChatRead(@Body ChatReadRequest request);

    @GET("api/chat/seller/conversations")
    Call<List<SellerConversationItem>> getSellerChatConversations();

    @PUT("api/notifications/device-token")
    Call<Void> registerDeviceToken(@Body DeviceTokenRequest request);

    @HTTP(method = "DELETE",
            path = "api/notifications/device-token",
            hasBody = true)
    Call<Void> unregisterDeviceToken(@Body DeviceTokenRequest request);
}
