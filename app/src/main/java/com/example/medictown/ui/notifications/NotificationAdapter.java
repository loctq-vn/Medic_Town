package com.example.medictown.ui.notifications;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medictown.R;
import com.example.medictown.data.models.AppNotification;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

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
        holder.time.setText(getRelativeTime(item.createdAt));
        
        // Trạng thái đã đọc/chưa đọc
        holder.unreadDot.setVisibility(item.isRead ? View.GONE : View.VISIBLE);
        holder.itemView.setAlpha(item.isRead ? 0.7f : 1.0f);

        // Icon theo loại thông báo
        if ("order".equalsIgnoreCase(item.type)) {
            holder.ivIcon.setImageResource(R.drawable.ic_history);
        } else if ("prescription".equalsIgnoreCase(item.type)) {
            holder.ivIcon.setImageResource(R.drawable.ic_donthuoc);
        } else {
            holder.ivIcon.setImageResource(R.drawable.ic_notifications);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onNotificationClick(item);
            }
        });
    }

    private String getRelativeTime(String dateStr) {
        if (dateStr == null) return "";
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", Locale.getDefault());
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date date = sdf.parse(dateStr);
            if (date == null) return dateStr;

            long now = System.currentTimeMillis();
            long diff = now - date.getTime();

            if (diff < 60000) return "Vừa xong";
            if (diff < 3600000) return (diff / 60000) + " phút trước";
            if (diff < 86400000) return (diff / 3600000) + " giờ trước";
            
            SimpleDateFormat displayFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            return displayFormat.format(date);
        } catch (ParseException e) {
            return dateStr;
        }
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    static class NotificationViewHolder extends RecyclerView.ViewHolder {
        TextView title, message, time;
        ImageView ivIcon;
        View unreadDot;

        NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.tv_notification_title);
            message = itemView.findViewById(R.id.tv_notification_message);
            time = itemView.findViewById(R.id.tv_notification_time);
            ivIcon = itemView.findViewById(R.id.iv_notification_icon);
            unreadDot = itemView.findViewById(R.id.view_unread_dot);
        }
    }
}
