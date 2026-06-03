package com.example.medictown.data.models;

import java.util.List;

public class OrderCreateRequest {
    public String payment_method;
    public String note;
    public String shipping_address;
    public List<String> cart_item_ids;
    public List<OrderItem> direct_items;

    public OrderCreateRequest() {}
}
