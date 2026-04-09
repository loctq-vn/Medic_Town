package com.example.medictown.data.models;

public class Banners {
    public String id;
    public String image_url;
    public String link_type; // "product"|"category"|"none"
    public String link_id;
    public int order;
    public boolean is_active;

    public Banners() {}
}
