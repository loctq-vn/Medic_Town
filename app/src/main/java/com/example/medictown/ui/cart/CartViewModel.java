package com.example.medictown.ui.cart;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.medictown.data.models.CartItemResponse;
import com.example.medictown.data.repositories.CartRepository;
import java.util.ArrayList;
import java.util.List;

public class CartViewModel extends ViewModel {

    private final MutableLiveData<List<CartItemUI>> cartItemsList = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Double> totalAmount = new MutableLiveData<>(0.0);
    private final CartRepository repository;

    // TODO: Replace with authenticated user's cart ID
    private static final String CURRENT_CART_ID = "replace-with-real-cart-uuid";

    public CartViewModel() {
        repository = new CartRepository();
        loadCartFromDB();
    }

    public LiveData<List<CartItemUI>> getCartItems() { return cartItemsList; }
    public LiveData<Double> getTotalAmount() { return totalAmount; }

    private void loadCartFromDB() {
        repository.fetchCartItems(CURRENT_CART_ID, new CartRepository.OnCartFetchedListener() {
            @Override
            public void onSuccess(List<CartItemResponse> responses) {
                List<CartItemUI> uiList = new ArrayList<>();
                for (CartItemResponse res : responses) {
                    if (res.products != null) {
                        uiList.add(new CartItemUI(res.id, res.products, res.quantity));
                    }
                }
                cartItemsList.postValue(uiList);
                calculateTotal();
            }
            @Override public void onError(String error) { /* Handle UI error */ }
        });
    }

    public void changeQuantity(String cartItemId, int change) {
        List<CartItemUI> currentList = cartItemsList.getValue();
        if (currentList != null) {
            for (int i = 0; i < currentList.size(); i++) {
                CartItemUI item = currentList.get(i);
                if (item.getCartItemId().equals(cartItemId)) {
                    int newQuantity = item.getQuantity() + change;
                    if (newQuantity > 0) {
                        item.setQuantity(newQuantity);
                        repository.updateQuantity(cartItemId, newQuantity);
                    } else {
                        currentList.remove(i);
                        repository.deleteItem(cartItemId);
                    }
                    break;
                }
            }
            cartItemsList.setValue(currentList);
            calculateTotal();
        }
    }

    public void removeItem(String cartItemId) {
        List<CartItemUI> currentList = cartItemsList.getValue();
        if (currentList != null) {
            currentList.removeIf(item -> item.getCartItemId().equals(cartItemId));
            cartItemsList.setValue(currentList);
            calculateTotal();
            repository.deleteItem(cartItemId);
        }
    }

    private void calculateTotal() {
        List<CartItemUI> currentList = cartItemsList.getValue();
        double total = 0;
        if (currentList != null) {
            for (CartItemUI item : currentList) {
                if (item.isSelected()) total += (item.getEffectivePrice() * item.getQuantity());
            }
        }
        totalAmount.postValue(total);
    }
}