package com.example.medictown.notifications;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.example.medictown.MainActivity;
import com.example.medictown.R;
import com.example.medictown.data.api.RetrofitClient;
import com.example.medictown.data.api.SessionManager;
import com.example.medictown.data.models.DeviceTokenRequest;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MedicTownMessagingService extends FirebaseMessagingService {

    public static final String CHANNEL_ID = "order_status";
    private static final String CHANNEL_NAME = "Trạng thái giao hàng";
    private static final String CHANNEL_DESCRIPTION =
            "Thông báo cập nhật trạng thái đơn hàng";

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);

        SessionManager sessionManager = new SessionManager(this);

        // Firebase can generate a token before the user logs in.
        if (!sessionManager.isLoggedIn()) {
            return;
        }

        RetrofitClient.getApiService()
                .registerDeviceToken(new DeviceTokenRequest(token))
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(
                            @NonNull Call<Void> call,
                            @NonNull Response<Void> response
                    ) {
                        // Nothing else is required after successful registration.
                    }

                    @Override
                    public void onFailure(
                            @NonNull Call<Void> call,
                            @NonNull Throwable throwable
                    ) {
                        // Token registration will be attempted again after login
                        // or when MainActivity starts.
                    }
                });
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        super.onMessageReceived(message);

        SessionManager sessionManager = new SessionManager(this);
        if (!sessionManager.isLoggedIn()) {
            return;
        }

        String type = message.getData().get("type");
        if (!"order_status".equals(type)) {
            return;
        }

        String orderId = message.getData().get("order_id");
        String status = message.getData().get("status");
        String title = message.getData().get("title");
        String body = message.getData().get("body");

        showOrderNotification(orderId, status, title, body);
    }

    private void showOrderNotification(
            String orderId,
            String orderStatus,
            String title,
            String body
    ) {
        createNotificationChannel();

        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("open_order_detail", true);
        intent.putExtra("order_id", orderId);
        intent.putExtra("order_status", orderStatus);
        intent.addFlags(
                Intent.FLAG_ACTIVITY_CLEAR_TOP |
                        Intent.FLAG_ACTIVITY_SINGLE_TOP
        );

        int notificationId = orderId != null
                ? orderId.hashCode()
                : (int) System.currentTimeMillis();

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                notificationId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT |
                        PendingIntent.FLAG_IMMUTABLE
        );

        Notification notification =
                new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_launcher_monochrome)
                        .setContentTitle(
                                title != null && !title.trim().isEmpty()
                                        ? title
                                        : "Medic Town"
                        )
                        .setContentText(
                                body != null && !body.trim().isEmpty()
                                        ? body
                                        : "Trạng thái đơn hàng đã thay đổi"
                        )
                        .setStyle(
                                new NotificationCompat.BigTextStyle()
                                        .bigText(body)
                        )
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setCategory(NotificationCompat.CATEGORY_STATUS)
                        .setAutoCancel(true)
                        .setContentIntent(pendingIntent)
                        .build();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
        ) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        NotificationManagerCompat.from(this)
                .notify(notificationId, notification);
    }

    private void createNotificationChannel() {
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
        );

        channel.setDescription(CHANNEL_DESCRIPTION);
        channel.enableVibration(true);

        NotificationManager manager =
                getSystemService(NotificationManager.class);

        if (manager != null) {
            manager.createNotificationChannel(channel);
        }
    }
}