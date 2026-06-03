package com.example.medictown.data.models;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class Orders implements Serializable {
    public String id;
    public String user_id;
    public String status; // pending, confirmed, shipping, completed, cancelled
    public Double total_amount;
    public String prescription_url;
    public String note;
    public String shipping_name;
    public String shipping_phone;

    public Date created_at;
    public Date updated_at;

    public String shipping_address;
    public List<String> cart_item_ids;
    public List<OrderItem> direct_items;

    public List<OrderItem> order_items;
    public List<Payments> payments;

    public Orders() {}

    public Payments getPrimaryPayment() {
        if (payments == null || payments.isEmpty()) {
            return null;
        }
        return payments.get(0);
    }

    public String getPaymentMethod() {
        Payments payment = getPrimaryPayment();
        return payment != null ? payment.method : null;
    }
}
