package com.example.medictown.notifications;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.medictown.data.api.RetrofitClient;
import com.example.medictown.data.api.SessionManager;
import com.example.medictown.data.models.DeviceTokenRequest;
import com.google.firebase.messaging.FirebaseMessaging;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public final class NotificationTokenManager {

    private static final String TAG = "NotificationToken";

    private NotificationTokenManager() {
        // Utility class.
    }

    public static void registerCurrentToken(Context context) {
        Context appContext = context.getApplicationContext();
        SessionManager sessionManager = new SessionManager(appContext);

        if (!sessionManager.isLoggedIn()) {
            Log.d(TAG, "Token registration skipped: user is not logged in");
            return;
        }

        FirebaseMessaging.getInstance()
                .getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.w(
                                TAG,
                                "Unable to retrieve Firebase token",
                                task.getException()
                        );
                        return;
                    }

                    String token = task.getResult();

                    if (token == null || token.trim().isEmpty()) {
                        Log.w(TAG, "Firebase returned an empty token");
                        return;
                    }

                    registerTokenWithBackend(token);
                });
    }

    private static void registerTokenWithBackend(String token) {
        DeviceTokenRequest request = new DeviceTokenRequest(token);

        RetrofitClient.getApiService()
                .registerDeviceToken(request)
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(
                            @NonNull Call<Void> call,
                            @NonNull Response<Void> response
                    ) {
                        if (response.isSuccessful()) {
                            Log.d(TAG, "Firebase token registered successfully");
                        } else {
                            Log.w(
                                    TAG,
                                    "Token registration failed: HTTP "
                                            + response.code()
                            );
                        }
                    }

                    @Override
                    public void onFailure(
                            @NonNull Call<Void> call,
                            @NonNull Throwable throwable
                    ) {
                        Log.e(
                                TAG,
                                "Token registration request failed",
                                throwable
                        );
                    }
                });
    }
}