package com.example.medictown.data.repositories;

import android.content.Context;
import android.net.Uri;
import android.widget.Toast;

import com.example.medictown.data.api.RetrofitClient;
import com.example.medictown.data.api.SessionManager;
import com.example.medictown.data.api.SupabaseApi;
import com.example.medictown.data.api.SupabaseConfig;
import com.example.medictown.data.models.ProductCategory;
import com.example.medictown.data.models.ProductSubcategory;
import com.example.medictown.data.models.Products;
import com.example.medictown.data.models.Shop;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import retrofit2.Callback;

public class ShopRepository {
    private final SupabaseApi apiService;

    public ShopRepository() {
        this.apiService = RetrofitClient.getApiService();
    }

    public void getMyShops(Callback<List<Shop>> callback) {
        apiService.getMyShops().enqueue(callback);
    }

    public void createShop(Shop shop, Callback<Shop> callback) {
        apiService.createShop(shop).enqueue(callback);
    }

    public void getShop(String shopId, Callback<Shop> callback) {
        apiService.getShop(shopId).enqueue(callback);
    }

    public void updateShop(String shopId, Shop shop, Callback<Shop> callback) {
        apiService.updateShop(shopId, shop).enqueue(callback);
    }

    public void createProduct(String shopId, Products product, Callback<Products> callback) {
        apiService.createShopProduct(shopId, product).enqueue(callback);
    }

    public void updateProduct(String shopId, String productId, Products product, Callback<Products> callback) {
        apiService.updateShopProduct(shopId, productId, product).enqueue(callback);
    }

    public void getProductCategories(Callback<List<ProductCategory>> callback) {
        apiService.getProductCategories().enqueue(callback);
    }

    public void getProductSubcategories(String categoryId, Callback<List<ProductSubcategory>> callback) {
        apiService.getProductSubcategories(categoryId).enqueue(callback);
    }

    public void uploadProductImage(Context context, String shopId, Uri fileUri, okhttp3.Callback callback) {
        OkHttpClient client = new OkHttpClient();
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(fileUri);
            byte[] bytes = getBytes(inputStream);

            String fileName = "product_" + System.currentTimeMillis() + ".jpg";
            String mimeType = context.getContentResolver().getType(fileUri);
            if (mimeType == null) mimeType = "image/jpeg";

            RequestBody requestBody = RequestBody.create(bytes, MediaType.parse(mimeType));
            String token = new SessionManager(context).getToken();

            Request request = new Request.Builder()
                    .url(SupabaseConfig.BACKEND_URL + "api/shops/" + shopId + "/products/images?filename=" + fileName)
                    .post(requestBody)
                    .addHeader("Authorization", "Bearer " + token)
                    .addHeader("Content-Type", mimeType)
                    .build();
            client.newCall(request).enqueue(callback);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "Không thể tải ảnh sản phẩm", Toast.LENGTH_SHORT).show();
        }
    }

    private byte[] getBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];
        int len;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }
}
