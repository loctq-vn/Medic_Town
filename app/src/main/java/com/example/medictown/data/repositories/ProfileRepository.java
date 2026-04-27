package com.example.medictown.data.repositories;

import android.content.Context;
import android.net.Uri;
import android.widget.Toast;

import com.example.medictown.data.api.RetrofitClient;
import com.example.medictown.data.api.SupabaseApi;
import com.example.medictown.data.api.SupabaseConfig;
import com.example.medictown.data.models.Users;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import retrofit2.Callback;
public class ProfileRepository {
    private final SupabaseApi apiService;
    public ProfileRepository() {
        this.apiService = RetrofitClient.getApiService();
    }
    public void getUser(String userId, Callback<Users> callback) {
        apiService.getUser(
            SupabaseConfig.SUPABASE_ANON_KEY,
            "Bearer " + SupabaseConfig.SUPABASE_ANON_KEY,
            "application/vnd.pgrst.object+json",
            "*",
            "eq." + userId
        ).enqueue(callback);
    }
    public void setUser(Users user, Callback<Void> callback){
        apiService.updateUser(
                SupabaseConfig.SUPABASE_ANON_KEY,
                "Bearer " + SupabaseConfig.SUPABASE_ANON_KEY,
                "eq." + user.id,
                user
        ).enqueue(callback);
    }
    public void uploadToSupabase(Context context, Uri fileUri, okhttp3.Callback callback){
        OkHttpClient client = new OkHttpClient();
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(fileUri);
            byte[] bytes = getBytes(inputStream);

            String fileName = "upload_" + System.currentTimeMillis() + ".jpg";
            String mimeType = context.getContentResolver().getType(fileUri);
            if (mimeType == null) mimeType = "image/jpeg";

            RequestBody requestBody = RequestBody.create(bytes, MediaType.parse(mimeType));

            String url = "https://wuhoqresirnxjnxulviw.supabase.co/storage/v1/object/avatars/" + fileName;

            Request request = new Request.Builder()
                    .url(url)
                    .post(requestBody)
                    .addHeader("Authorization", "Bearer " + SupabaseConfig.SUPABASE_ANON_KEY)
                    .addHeader("Content-Type", mimeType)
                    .build();
            client.newCall(request).enqueue(callback);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context,"lỗi",Toast.LENGTH_SHORT).show();
        }
    }
    public byte[] getBytes(InputStream inputStream) throws IOException {
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
