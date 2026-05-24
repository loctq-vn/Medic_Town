package com.example.medictown.data.models;

import java.io.Serializable;
import java.util.Date;

public class Shop implements Serializable {
    public String id;
    public String owner_id;
    public String name;
    public String description;
    public String logo_url;
    public String address;
    public boolean is_active = true;
    public Date created_at;

    public Shop() {}

    public Shop(String name, String description, String address) {
        this.name = name;
        this.description = description;
        this.address = address;
    }
}
