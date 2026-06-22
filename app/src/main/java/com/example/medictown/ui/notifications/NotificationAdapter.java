package com.example.medictown.ui.notifications;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medictown.R;
import com.example.medictown.data.models.AppNotification;

import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {
    public interface OnNotificationClickListener {
        void onNotificationClick(AppNotification notification);
    }

    private final List<AppNotification> notifications;
    private final OnNotificationClickListener listener;

    public NotificationAdapter(List<AppNotification> notifications) {
        this(notifications, null);
    }

    public NotificationAdapter(List<AppNotification> notifications, OnNotificationClickListener listener) {
        this.notifications = notifications;
        this.listener = listener;
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        AppNotification item = notifications.get(position);

        holder.title.setText(item.title);
        holder.message.setText(item.message);
        holder.time.setText(item.createdAt);
        holder.itemView.setAlpha(item.isRead ? 0.6f : 1f);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onNotificationClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    static class NotificationViewHolder extends RecyclerView.ViewHolder {
        TextView title, message, time;

        NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.tv_notification_title);
            message = itemView.findViewById(R.id.tv_notification_message);
            time = itemView.findViewById(R.id.tv_notification_time);
        }
    }
}