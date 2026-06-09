package com.example.medictown.data.repositories;

import android.content.Context;
import android.net.Uri;

import com.example.medictown.data.api.RetrofitClient;
import com.example.medictown.data.api.SessionManager;
import com.example.medictown.data.api.SupabaseApi;
import com.example.medictown.data.api.SupabaseConfig;
import com.example.medictown.data.models.Address;
import com.example.medictown.data.models.Users;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import retrofit2.Callback;

public class ProfileRepository {
    private final SupabaseApi apiService;

    public ProfileRepository() {
        this.apiService = RetrofitClient.getApiService();
    }

    public void getUser(String userId, Callback<Users> callback) {
        apiService.getUser().enqueue(callback);
    }

    public void setUser(Users user, Callback<Void> callback) {
        apiService.updateUser(user).enqueue(callback);
    }

    public void getAddress(String userId, Callback<List<Address>> callback) {
        apiService.getAddress().enqueue(callback);
    }

    public void setAddress(Address address, Callback<Void> callback) {
        apiService.setAddress(address.id, address).enqueue(callback);
    }

    public void addAddress(Address address, Callback<Void> callback) {
        apiService.addAddress(address).enqueue(callback);
    }

    public void deleteAddress(String addressId, Callback<Void> callback) {
        apiService.deleteAddress(addressId).enqueue(callback);
    }

    public void uploadToSupabase(Context context, Uri fileUri, okhttp3.Callback callback) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(fileUri);
            byte[] bytes = getBytes(inputStream);
            ImageType imageType = detectImageType(bytes);

            String fileName = "upload_" + System.currentTimeMillis() + "." + imageType.extension;
            String mimeType = imageType.mimeType;

            RequestBody requestBody = RequestBody.create(bytes, MediaType.parse(mimeType));
            String token = new SessionManager(context).getToken();

            Request request = new Request.Builder()
                    .url(SupabaseConfig.BACKEND_URL + "api/users/me/avatar?filename=" + fileName)
                    .post(requestBody)
                    .addHeader("Authorization", "Bearer " + token)
                    .addHeader("ngrok-skip-browser-warning", "true")
                    .addHeader("Content-Type", mimeType)
                    .build();
            RetrofitClient.getHttpClient().newCall(request).enqueue(callback);
        } catch (Exception e) {
            e.printStackTrace();
            callback.onFailure(null, e instanceof IOException
                    ? (IOException) e
                    : new IOException("Unable to read selected avatar", e));
        }
    }

    public byte[] getBytes(InputStream inputStream) throws IOException {
        if (inputStream == null) {
            throw new IOException("Unable to open selected avatar");
        }
        try (InputStream stream = inputStream;
             ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[8192];
            int len;
            while ((len = stream.read(buffer)) != -1) {
                byteBuffer.write(buffer, 0, len);
            }
            return byteBuffer.toByteArray();
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

    private static class ImageType {
        final String extension;
        final String mimeType;

        ImageType(String extension, String mimeType) {
            this.extension = extension;
            this.mimeType = mimeType;
        }
    }
}
