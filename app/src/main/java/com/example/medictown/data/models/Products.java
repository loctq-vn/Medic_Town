package com.example.medictown.data.models;

import java.util.Date;
import java.util.List;

public class Products {
    public String id;
    public String category_id;
    public String seller_id;
    public String name;
    public String brand;
    public double price;
    public Double sale_price;
    public int stock;
    public List<String> images;
    public String description;
    public String usage;
    public String indications;
    public String contraindications;
    public String manufacturer;
    public boolean requires_prescription;
    public boolean is_featured;
    public boolean is_best_seller;
    public boolean is_active;
    public Date created_at;

    public Products() {} // Required for Firebase
}
