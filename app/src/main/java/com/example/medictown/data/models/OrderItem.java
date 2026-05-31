package com.example.medictown.data.models;

import java.io.Serializable;

public class OrderItem implements Serializable {
    public String id;
    public String order_id;
    public String product_id;
    public String product_name;
    public String product_image;
    public double price;
    public int quantity;

    public OrderItem() {}
}
