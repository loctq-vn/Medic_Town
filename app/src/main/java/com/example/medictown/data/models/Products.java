package com.example.medictown.data.models;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class Products implements Serializable {
    public String id;
    public String name;
    public String brand;
    public double price;
    public Double sale_price;
    public String unit;
    public int stock;
    public List<String> images;
    public String usage;
    public String uses;
    public String side_effects;
    public String precautions;
    public String storage;
    public String manufacturer;
    public boolean requires_prescription;
    public boolean is_featured;
    public boolean is_best_seller;
    public boolean is_active;
    public String shop_id;
    public String subcategory_id;
    public Date created_at;

    public Products() {} // Required for Firebase
}
