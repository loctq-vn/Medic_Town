package com.example.medictown.ui.notifications;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medictown.R;
import com.example.medictown.data.api.RetrofitClient;
import com.example.medictown.data.models.AppNotification;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.content.Intent;

import com.example.medictown.MainActivity;


public class NotificationCenterActivity extends AppCompatActivity {
    private final List<AppNotification> notifications = new ArrayList<>();
    private NotificationAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_center);

        MaterialToolbar toolbar = findViewById(R.id.toolbar_notifications);
        toolbar.setNavigationIcon(R.drawable.ic_back);
        toolbar.setNavigationOnClickListener(v -> finish());

        RecyclerView recyclerView = findViewById(R.id.rv_all_notifications);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new NotificationAdapter(notifications, this::handleNotificationClick);
        recyclerView.setAdapter(adapter);

        loadNotifications();
    }

    private void loadNotifications() {
        RetrofitClient.getApiService().getNotifications(50).enqueue(new Callback<List<AppNotification>>() {
            @Override
            public void onResponse(@NonNull Call<List<AppNotification>> call, @NonNull Response<List<AppNotification>> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(NotificationCenterActivity.this, "Cannot load notifications", Toast.LENGTH_SHORT).show();
                    return;
                }

                notifications.clear();
                notifications.addAll(response.body());
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(@NonNull Call<List<AppNotification>> call, @NonNull Throwable throwable) {
                Toast.makeText(NotificationCenterActivity.this, "Cannot load notifications", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleNotificationClick(AppNotification notification) {
        if (notification == null) {
            return;
        }

        markNotificationRead(notification);

        if (notification.orderId == null || notification.orderId.trim().isEmpty()) {
            Toast.makeText(this, "This notification is not linked to an order", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("open_order_detail", true);
        intent.putExtra("order_id", notification.orderId);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        startActivity(intent);
        finish();
    }

    private void markNotificationRead(AppNotification notification) {
        if (notification.id == null || notification.id.trim().isEmpty()) {
            return;
        }

        if (!notification.isRead) {
            notification.isRead = true;
            adapter.notifyDataSetChanged();
        }

        RetrofitClient.getApiService().markNotificationRead(notification.id).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                // Already updated locally.
            }

            @Override
            public void onFailure(Call<Void> call, Throwable throwable) {
                Toast.makeText(NotificationCenterActivity.this, "Cannot mark notification as read", Toast.LENGTH_SHORT).show();
            }
        });
    }
}