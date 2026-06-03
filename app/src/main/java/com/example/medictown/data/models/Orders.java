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

        Payments selected = null;
        for (Payments payment : payments) {
            if (payment == null) {
                continue;
            }
            if (selected == null || comparePaymentPriority(payment, selected) > 0) {
                selected = payment;
            }
        }
        return selected;
    }

    public String getPaymentMethod() {
        Payments payment = getPrimaryPayment();
        return payment != null ? payment.method : null;
    }

    private int comparePaymentPriority(Payments left, Payments right) {
        int statusDiff = paymentStatusPriority(left.status) - paymentStatusPriority(right.status);
        if (statusDiff != 0) {
            return statusDiff;
        }

        Date leftDate = left.updated_at != null ? left.updated_at : left.created_at;
        Date rightDate = right.updated_at != null ? right.updated_at : right.created_at;
        if (leftDate == null && rightDate == null) {
            return 0;
        }
        if (leftDate == null) {
            return -1;
        }
        if (rightDate == null) {
            return 1;
        }
        return leftDate.compareTo(rightDate);
    }

    private int paymentStatusPriority(String status) {
        if (status == null) {
            return 0;
        }
        switch (status.toLowerCase()) {
            case "completed":
                return 6;
            case "processing":
                return 5;
            case "pending":
                return 4;
            case "refunded":
                return 3;
            case "failed":
            case "expired":
            case "cancelled":
                return 2;
            default:
                return 1;
        }
    }
}
