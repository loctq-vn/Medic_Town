package com.example.medictown.data.repositories;

import android.content.Context;
import android.net.Uri;

import com.example.medictown.data.api.RetrofitClient;
import com.example.medictown.data.api.SessionManager;
import com.example.medictown.data.api.SupabaseApi;
import com.example.medictown.data.api.SupabaseConfig;
import com.example.medictown.data.models.Advertisement;
import com.example.medictown.data.models.AdvertisementRequest;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import retrofit2.Callback;

public class AdvertisementRepository {
    private final SupabaseApi apiService = RetrofitClient.getApiService();

    public void getShopAdvertisements(
            String shopId,
            Callback<List<Advertisement>> callback
    ) {
        apiService.getShopAdvertisements(shopId, null, null, null, 100, 0, true)
                .enqueue(callback);
    }

    public void create(
            String shopId,
            AdvertisementRequest request,
            Callback<Advertisement> callback
    ) {
        apiService.createShopAdvertisement(shopId, request).enqueue(callback);
    }

    public void update(
            String shopId,
            String advertisementId,
            AdvertisementRequest request,
            Callback<Advertisement> callback
    ) {
        apiService.updateShopAdvertisement(shopId, advertisementId, request).enqueue(callback);
    }

    public void updateActive(
            String shopId,
            String advertisementId,
            boolean active,
            Callback<Advertisement> callback
    ) {
        Map<String, Object> body = Collections.singletonMap("is_active", active);
        apiService.updateShopAdvertisementStatus(shopId, advertisementId, body).enqueue(callback);
    }

    public void delete(String shopId, String advertisementId, Callback<Void> callback) {
        apiService.deleteShopAdvertisement(shopId, advertisementId).enqueue(callback);
    }

    public void recordView(String advertisementId) {
        apiService.recordAdView(advertisementId).enqueue(noOpCallback());
    }

    public void recordClick(String advertisementId) {
        apiService.recordAdClick(advertisementId).enqueue(noOpCallback());
    }

    public void uploadImage(
            Context context,
            String shopId,
            Uri fileUri,
            okhttp3.Callback callback
    ) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(fileUri);
            byte[] bytes = readBytes(inputStream);
            ImageType imageType = detectImageType(bytes);
            String mimeType = imageType.mimeType;
            String fileName = "advertisement_" + System.currentTimeMillis()
                    + "." + imageType.extension;
            String token = new SessionManager(context).getToken();

            Request request = new Request.Builder()
                    .url(SupabaseConfig.BACKEND_URL + "api/shops/" + shopId
                            + "/ads/images?filename=" + fileName)
                    .post(RequestBody.create(bytes, MediaType.parse(mimeType)))
                    .addHeader("Authorization", "Bearer " + token)
                    .addHeader("ngrok-skip-browser-warning", "true")
                    .addHeader("Content-Type", mimeType)
                    .build();
            RetrofitClient.getHttpClient().newCall(request).enqueue(callback);
        } catch (Exception exception) {
            callback.onFailure(null, exception instanceof IOException
                    ? (IOException) exception
                    : new IOException("Unable to read advertisement image", exception));
        }
    }

    private ImageType detectImageType(byte[] bytes) throws IOException {
        if (bytes.length >= 3
                && (bytes[0] & 0xFF) == 0xFF
                && (bytes[1] & 0xFF) == 0xD8
                && (bytes[2] & 0xFF) == 0xFF) {
            return new ImageType("jpg", "image/jpeg");
        }
        if (bytes.length >= 8
                && (bytes[0] & 0xFF) == 0x89
                && bytes[1] == 'P'
                && bytes[2] == 'N'
                && bytes[3] == 'G'
                && bytes[4] == '\r'
                && bytes[5] == '\n'
                && bytes[6] == 0x1A
                && bytes[7] == '\n') {
            return new ImageType("png", "image/png");
        }
        if (bytes.length >= 12
                && bytes[0] == 'R'
                && bytes[1] == 'I'
                && bytes[2] == 'F'
                && bytes[3] == 'F'
                && bytes[8] == 'W'
                && bytes[9] == 'E'
                && bytes[10] == 'B'
                && bytes[11] == 'P') {
            return new ImageType("webp", "image/webp");
        }
        throw new IOException(
                "Định dạng ảnh không được hỗ trợ. Vui lòng chọn JPG, PNG hoặc WebP"
        );
    }

    private byte[] readBytes(InputStream inputStream) throws IOException {
        if (inputStream == null) throw new IOException("Image is unavailable");
        try (InputStream stream = inputStream;
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[8192];
            int count;
            while ((count = stream.read(buffer)) != -1) {
                output.write(buffer, 0, count);
            }
            return output.toByteArray();
        }
    }

    private static class ImageType {
        final String extension;
        final String mimeType;

        ImageType(String extension, String mimeType) {
            this.extension = extension;
            this.mimeType = mimeType;
        }
    }

    private Callback<Void> noOpCallback() {
        return new Callback<Void>() {
            @Override
            public void onResponse(retrofit2.Call<Void> call, retrofit2.Response<Void> response) {
            }

            @Override
            public void onFailure(retrofit2.Call<Void> call, Throwable throwable) {
            }
        };
    }
}
