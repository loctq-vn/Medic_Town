package com.example.medictown.data.models;

import java.io.Serializable;

public class ProductCategory implements Serializable {
    public String id;
    public String name;
    public String description;
    public int sort_order;
    public boolean is_active;

    @Override
    public String toString() {
        return name != null ? name : id;
    }
}
