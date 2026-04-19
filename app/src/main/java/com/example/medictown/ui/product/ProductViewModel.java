package com.example.medictown.ui.product;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.medictown.data.models.Products;
import com.example.medictown.data.repositories.ProductRepository;
import java.util.List;

public class ProductViewModel extends ViewModel {
    private final ProductRepository repository;
    private final MutableLiveData<List<Products>> featuredProducts = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);

    public ProductViewModel() {
        this.repository = new ProductRepository();
        loadFeaturedProducts();
    }

    public LiveData<List<Products>> getFeaturedProducts() {
        return featuredProducts;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public void loadFeaturedProducts() {
        isLoading.setValue(true);
        repository.getFeaturedProducts().addOnCompleteListener(task -> {
            isLoading.setValue(false);
            if (task.isSuccessful() && task.getResult() != null) {
                featuredProducts.setValue(task.getResult().toObjects(Products.class));
            }
        });
    }
}
