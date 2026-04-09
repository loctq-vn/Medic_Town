package com.example.medictown.data.models;

import java.util.Date;
import java.util.List;

public class Reviews {
    public String id;
    public String user_id;
    public String order_id;
    public String product_id;
    public int rating;
    public String comment;
    public List<String> images;
    public Date created_at;
    public Date deleted_at;

    public Reviews() {}
}
