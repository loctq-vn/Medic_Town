package com.example.medictown.data.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static Retrofit retrofitRest = null;
    private static Retrofit retrofitAuth = null;

    public static SupabaseApi getApiService() {
        if (retrofitRest == null) {
            retrofitRest = createRetrofit(SupabaseConfig.SUPABASE_URL);
        }
        return retrofitRest.create(SupabaseApi.class);
    }

    public static SupabaseApi getAuthService() {
        if (retrofitAuth == null) {
            retrofitAuth = createRetrofit(SupabaseConfig.AUTH_URL);
        }
        return retrofitAuth.create(SupabaseApi.class);
    }
    private static Retrofit createRetrofit(String baseUrl) {
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(interceptor)
                .build();

        Gson gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss")
                .create();

        return new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
    }
}
