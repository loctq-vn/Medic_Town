package com.example.medictown.ui.payment;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.medictown.data.models.Address;
import com.example.medictown.data.models.CartItem;
import com.example.medictown.data.models.Orders;
import com.example.medictown.data.repositories.OrderRepository;
import com.example.medictown.data.repositories.ProfileRepository;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PaymentViewModel extends ViewModel {
    private final ProfileRepository profileRepository;
    private final OrderRepository orderRepository;

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

    private final MutableLiveData<Boolean> _orderSuccess = new MutableLiveData<>();
    public LiveData<Boolean> orderSuccess = _orderSuccess;

    private final MutableLiveData<String> _error = new MutableLiveData<>();
    public LiveData<String> error = _error;

    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>(false);
    public LiveData<Boolean> isLoading = _isLoading;

    public PaymentViewModel() {
        profileRepository = new ProfileRepository();
        orderRepository = new OrderRepository();
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
                _error.postValue("Loi tai dia chi: " + t.getMessage());
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
        _totalAmount.setValue(sub);
    }

    public void placeOrder(String userId, String paymentMethod, String note) {
        Address address = _selectedAddress.getValue();
        if (address == null) {
            _error.setValue("Vui long chon dia chi nhan hang");
            return;
        }

        List<CartItem> items = _selectedItems.getValue();
        if (items == null || items.isEmpty()) {
            _error.setValue("Gio hang trong");
            return;
        }

        List<String> cartItemIds = new ArrayList<>();
        for (CartItem item : items) {
            if (item.id == null || item.id.isEmpty()) {
                _error.setValue("Vui long them san pham vao gio hang truoc khi dat hang");
                return;
            }
            cartItemIds.add(item.id);
        }

        _isLoading.setValue(true);

        Orders order = new Orders();
        order.payment_method = paymentMethod;
        order.note = note;
        order.shipping_address = address.location;
        order.cart_item_ids = cartItemIds;

        orderRepository.createOrder(order, new Callback<List<Orders>>() {
            @Override
            public void onResponse(Call<List<Orders>> call, Response<List<Orders>> response) {
                _isLoading.setValue(false);
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    _orderSuccess.setValue(true);
                } else {
                    _error.setValue("Loi khi tao don hang: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<Orders>> call, Throwable t) {
                _isLoading.setValue(false);
                _error.setValue("Loi ket noi: " + t.getMessage());
            }
        });
    }
}
