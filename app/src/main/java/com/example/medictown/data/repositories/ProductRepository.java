package com.example.medictown.data.repositories;

import com.example.medictown.data.models.Categories;
import com.example.medictown.data.models.Products;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;

public class ProductRepository {
    private final FirebaseFirestore db;

    public ProductRepository() {
        this.db = FirebaseFirestore.getInstance();
    }

    public Task<QuerySnapshot> getFeaturedProducts() {
        return db.collection("products")
                .whereEqualTo("is_featured", true)
                .whereEqualTo("is_active", true)
                .get();
    }

    public Task<QuerySnapshot> getCategories() {
        return db.collection("categories")
                .whereEqualTo("is_active", true)
                .orderBy("order")
                .get();
    }
}
