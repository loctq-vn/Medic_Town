package com.example.medictown.data.models;

import java.util.Date;

public class Orders {
    public String id;
    public String user_id;
    public String status; // pending, confirmed, shipping, completed, cancelled
    public String payment_method;
    public double total_amount;
    public String prescription_url;
    public String note;
    public String shipping_name;
    public String shipping_phone;
    public String shipping_street;
    public String shipping_district;
    public String shipping_city;
    public Date created_at;
    public Date updated_at;

    public Orders() {}
}
