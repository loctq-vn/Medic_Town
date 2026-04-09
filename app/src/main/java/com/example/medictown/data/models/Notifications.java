package com.example.medictown.data.models;

import java.util.Date;

public class Notifications {
    public String id;
    public String user_id;
    public String title;
    public String body;
    public String type; // order_update, promotion, system, chat
    public boolean is_read;
    public Date created_at;

    public Notifications() {}
}
