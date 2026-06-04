package com.example.medictown.data.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static Retrofit retrofit = null;
    private static String authToken = null;

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

    private static Retrofit createRetrofit(String baseUrl) {
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient client = new OkHttpClient.Builder()
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
