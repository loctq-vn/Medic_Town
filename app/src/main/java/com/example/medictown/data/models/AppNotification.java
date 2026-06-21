package com.example.medictown.data.models;

import com.google.gson.annotations.SerializedName;

public class AppNotification {
    public String id;
    public String type;
    public String title;

    @SerializedName("body")
    public String message;

    @SerializedName("order_id")
    public String orderId;

    @SerializedName("is_read")
    public boolean isRead;

    @SerializedName("created_at")
    public String createdAt;

    public AppNotification(String id, String title, String message, String createdAt, boolean isRead) {
        this.id = id;
        this.title = title;
        this.message = message;
        this.createdAt = createdAt;
        this.isRead = isRead;
        this.type = "general";
    }
    public AppNotification() {
    }
}

