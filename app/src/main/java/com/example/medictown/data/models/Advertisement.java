package com.example.medictown.data.models;

import java.io.Serializable;
import java.util.Date;

public class Advertisement implements Serializable {
    public String id;
    public String title;
    public String description;
    public String image_url;
    public String target_type;
    public String target_id;
    public String target_url;
    public String position;
    public int priority;
    public boolean is_active;
    public Date start_date;
    public Date end_date;
    public Date created_at;
    public Date updated_at;
    public int view_count;
    public int click_count;
    public Double budget_amount;
    public double spent_amount;
    public String status;
    public String shop_id;
    public String created_by;
    public Products product;

    public Advertisement() {}
}
