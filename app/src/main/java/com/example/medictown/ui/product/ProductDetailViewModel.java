package com.example.medictown.ui.product;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.medictown.data.repositories.CartRepository;

public class ProductDetailViewModel extends ViewModel {
    private final CartRepository repository;
    private final MutableLiveData<Boolean> isAddToCartSuccessful = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    // TODO: Replace with authenticated user's cart ID
    private static final String CURRENT_CART_ID = "replace-with-real-cart-uuid";

    public ProductDetailViewModel() {
        this.repository = new CartRepository();
    }

    public LiveData<Boolean> getIsAddToCartSuccessful() { return isAddToCartSuccessful; }
    public LiveData<String> getErrorMessage() { return errorMessage; }

    public void addProductToCart(String productId) {
        repository.addToCart(CURRENT_CART_ID, productId, 1, new CartRepository.OnActionCompletedListener() {
            @Override
            public void onSuccess() { isAddToCartSuccessful.postValue(true); }

            @Override
            public void onError(String error) { errorMessage.postValue(error); }
        });
    }
}