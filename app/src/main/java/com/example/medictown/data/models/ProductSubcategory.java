package com.example.medictown.data.models;

import java.io.Serializable;

public class ProductSubcategory implements Serializable {
    public String id;
    public String category_id;
    public String name;
    public String description;
    public boolean requires_prescription_default;
    public int sort_order;
    public boolean is_active;

    @Override
    public String toString() {
        return name != null ? name : id;
    }
}
