package com.example.medictown.data.models;

import java.util.Date;
import java.util.Map;

public class Payments {
    public String id;
    public String order_id;
    public String method; // cash, wallet, vnpay, momo
    public double amount;
    public String transaction_id;
    public String status; // pending, processing, completed, failed, refunded
    public Map<String, Object> payment_data;
    public Date paid_at;
    public Date created_at;
    public Date updated_at;

    public Payments() {}
}
