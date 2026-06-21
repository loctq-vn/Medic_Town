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

        adapter = new NotificationAdapter(notifications);
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
}