package com.example.medictown.ui.payment;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.medictown.data.models.Address;
import com.example.medictown.data.models.CartItem;
import com.example.medictown.data.repositories.ProfileRepository;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PaymentViewModel extends ViewModel {
    private final ProfileRepository profileRepository;

    private final MutableLiveData<List<CartItem>> _selectedItems = new MutableLiveData<>(new ArrayList<>());
    public LiveData<List<CartItem>> selectedItems = _selectedItems;

    private final MutableLiveData<Double> _subtotal = new MutableLiveData<>(0.0);
    public LiveData<Double> subtotal = _subtotal;

    private final MutableLiveData<Double> _totalAmount = new MutableLiveData<>(0.0);
    public LiveData<Double> totalAmount = _totalAmount;

    private final MutableLiveData<List<Address>> _addresses = new MutableLiveData<>();
    public LiveData<List<Address>> addresses = _addresses;

    private final MutableLiveData<Address> _selectedAddress = new MutableLiveData<>();
    public LiveData<Address> selectedAddress = _selectedAddress;

    public PaymentViewModel() {
        profileRepository = new ProfileRepository();
    }

    public void fetchAddresses(String userId) {
        profileRepository.getAddress(userId, new Callback<List<Address>>() {
            @Override
            public void onResponse(Call<List<Address>> call, Response<List<Address>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    _addresses.postValue(response.body());
                    if (!response.body().isEmpty() && _selectedAddress.getValue() == null) {
                        _selectedAddress.postValue(response.body().get(0));
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Address>> call, Throwable t) {
                // Handle failure
            }
        });
    }

    public void selectAddress(Address address) {
        _selectedAddress.setValue(address);
    }

    public void setSelectedItems(List<CartItem> items) {
        _selectedItems.setValue(items);
        calculateTotal(items);
    }

    private void calculateTotal(List<CartItem> items) {
        double sub = 0;
        for (CartItem item : items) {
            if (item.products != null) {
                double price = (item.products.sale_price != null && item.products.sale_price > 0)
                        ? item.products.sale_price
                        : item.products.price;
                sub += price * item.quantity;
            }
        }
        _subtotal.setValue(sub);
        
        double shippingFee = 0; // Fixed for now as per design
        _totalAmount.setValue(sub + shippingFee);
    }
}
