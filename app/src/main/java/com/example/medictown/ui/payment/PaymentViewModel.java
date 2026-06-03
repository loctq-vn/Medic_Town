package com.example.medictown.ui.payment;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.medictown.data.models.Address;
import com.example.medictown.data.models.CartItem;
import com.example.medictown.data.models.OrderCreateRequest;
import com.example.medictown.data.models.OrderItem;
import com.example.medictown.data.models.Orders;
import com.example.medictown.data.repositories.OrderRepository;
import com.example.medictown.data.repositories.ProfileRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

    private final MutableLiveData<String> _momoPaymentUrl = new MutableLiveData<>();
    public LiveData<String> momoPaymentUrl = _momoPaymentUrl;

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
        List<OrderItem> directItems = new ArrayList<>();
        for (CartItem item : items) {
            if (item.id != null && !item.id.isEmpty()) {
                cartItemIds.add(item.id);
            } else if (item.product_id != null && !item.product_id.isEmpty()) {
                OrderItem directItem = new OrderItem();
                directItem.product_id = item.product_id;
                directItem.quantity = item.quantity;
                directItems.add(directItem);
            } else {
                _error.setValue("San pham khong hop le");
                return;
            }
        }

        _isLoading.setValue(true);

        OrderCreateRequest order = new OrderCreateRequest();
        order.payment_method = paymentMethod;
        order.note = note;
        order.shipping_address = address.location;
        order.cart_item_ids = cartItemIds;
        order.direct_items = directItems;

        orderRepository.createOrder(order, new Callback<List<Orders>>() {
            @Override
            public void onResponse(Call<List<Orders>> call, Response<List<Orders>> response) {
                _isLoading.setValue(false);
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    String momoUrl = findMomoPaymentUrl(response.body());
                    if (momoUrl != null && !momoUrl.isEmpty()) {
                        _momoPaymentUrl.setValue(momoUrl);
                    } else if ("Momo".equalsIgnoreCase(paymentMethod)) {
                        String momoError = findMomoPaymentError(response.body());
                        _error.setValue(momoError != null ? momoError : "Khong tao duoc lien ket thanh toan MoMo");
                    } else {
                        _orderSuccess.setValue(true);
                    }
                } else {
                    _error.setValue("Loi khi tao don hang: " + response.code() + " - " + readErrorBody(response));
                }
            }

            @Override
            public void onFailure(Call<List<Orders>> call, Throwable t) {
                _isLoading.setValue(false);
                _error.setValue("Loi ket noi: " + t.getMessage());
            }
        });
    }

    private String findMomoPaymentUrl(List<Orders> orders) {
        for (Orders order : orders) {
            if (order.payments == null) {
                continue;
            }
            for (com.example.medictown.data.models.Payments payment : order.payments) {
                if (payment == null || !"momo".equalsIgnoreCase(payment.method) || payment.payment_data == null) {
                    continue;
                }
                String deeplink = getStringValue(payment.payment_data, "deeplink");
                if (deeplink != null && !deeplink.isEmpty()) {
                    return deeplink;
                }
                String payUrl = getStringValue(payment.payment_data, "payUrl");
                if (payUrl != null && !payUrl.isEmpty()) {
                    return payUrl;
                }
            }
        }
        return null;
    }

    private String findMomoPaymentError(List<Orders> orders) {
        for (Orders order : orders) {
            if (order.payments == null) {
                continue;
            }
            for (com.example.medictown.data.models.Payments payment : order.payments) {
                if (payment == null || !"momo".equalsIgnoreCase(payment.method) || payment.payment_data == null) {
                    continue;
                }
                Object response = payment.payment_data.get("momo_create_response");
                if (!(response instanceof Map)) {
                    continue;
                }
                @SuppressWarnings("unchecked")
                Map<String, Object> momoResponse = (Map<String, Object>) response;
                String message = getStringValue(momoResponse, "message");
                if (message != null && !message.isEmpty()) {
                    return "MoMo: " + message;
                }
            }
        }
        return null;
    }

    private String getStringValue(Map<String, Object> data, String key) {
        Object value = data.get(key);
        return value instanceof String ? (String) value : null;
    }

    private String readErrorBody(Response<?> response) {
        if (response.errorBody() == null) {
            return "Khong co chi tiet loi";
        }
        try {
            return response.errorBody().string();
        } catch (Exception e) {
            return "Khong doc duoc chi tiet loi";
        }
    }
}
