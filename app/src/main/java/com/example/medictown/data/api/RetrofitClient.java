package com.example.medictown.data.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static Retrofit retrofit = null;
    private static volatile String authToken = null;
    private static OkHttpClient httpClient = null;
    private static Gson gson = null;

    public static void setAuthToken(String token) {
        authToken = token;
    }

    public static SupabaseApi getApiService() {
        if (retrofit == null) {
            retrofit = createRetrofit(SupabaseConfig.BACKEND_URL);
        }
        return retrofit.create(SupabaseApi.class);
    }

    public static SupabaseApi getAuthService() {
        return getApiService();
    }

    public static synchronized OkHttpClient getHttpClient() {
        if (httpClient == null) {
            httpClient = createHttpClient();
        }
        return httpClient;
    }

    public static synchronized Gson getGson() {
        if (gson == null) {
            gson = new GsonBuilder()
                    .setDateFormat("yyyy-MM-dd'T'HH:mm:ss")
                    .create();
        }
        return gson;
    }

    private static Retrofit createRetrofit(String baseUrl) {
        return new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(getHttpClient())
                .addConverterFactory(GsonConverterFactory.create(getGson()))
                .build();
    }

    private static OkHttpClient createHttpClient() {
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        return new OkHttpClient.Builder()
                .addInterceptor(chain -> {
                    okhttp3.Request original = chain.request();
                    okhttp3.Request.Builder builder = original.newBuilder()
                            .addHeader("ngrok-skip-browser-warning", "true");
                    if (authToken != null && !authToken.isEmpty() && original.header("Authorization") == null) {
                        builder.addHeader("Authorization", "Bearer " + authToken);
                    }
                    return chain.proceed(builder.build());
                })
                .addInterceptor(loggingInterceptor)
                .build();
    }
}
