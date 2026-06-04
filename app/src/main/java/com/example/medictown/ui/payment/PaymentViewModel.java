package com.example.medictown.ui.payment;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.medictown.data.models.Address;
import com.example.medictown.data.models.CartItem;
import com.example.medictown.data.models.FakePaymentMethodRequest;
import com.example.medictown.data.models.OrderCreateRequest;
import com.example.medictown.data.models.OrderItem;
import com.example.medictown.data.models.Orders;
import com.example.medictown.data.models.Payments;
import com.example.medictown.data.repositories.OrderRepository;
import com.example.medictown.data.repositories.ProfileRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PaymentViewModel extends ViewModel {
    public static class MomoPaymentTarget {
        public final String primaryUrl;
        public final String fallbackUrl;

        public MomoPaymentTarget(String primaryUrl, String fallbackUrl) {
            this.primaryUrl = primaryUrl;
            this.fallbackUrl = fallbackUrl;
        }
    }

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

    private final MutableLiveData<MomoPaymentTarget> _momoPaymentTarget = new MutableLiveData<>();
    public LiveData<MomoPaymentTarget> momoPaymentTarget = _momoPaymentTarget;

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
        order.shipping_name = address.recipient_name;
        order.shipping_phone = address.phone_number;
        order.shipping_address = address.location;
        order.cart_item_ids = cartItemIds;
        order.direct_items = directItems;

        if (isRealMomoPayment(paymentMethod)) {
            createMomoCheckout(order);
            return;
        }

        orderRepository.createOrder(order, new Callback<List<Orders>>() {
            @Override
            public void onResponse(Call<List<Orders>> call, Response<List<Orders>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    if (isFakePaymentMethod(paymentMethod)) {
                        createFakePayments(response.body(), paymentMethod);
                    } else {
                        _isLoading.setValue(false);
                        _orderSuccess.setValue(true);
                    }
                } else {
                    _isLoading.setValue(false);
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

    private boolean isRealMomoPayment(String paymentMethod) {
        return "Momo".equalsIgnoreCase(paymentMethod) || "momo".equalsIgnoreCase(paymentMethod);
    }

    private boolean isFakePaymentMethod(String paymentMethod) {
        return "fake_momo".equalsIgnoreCase(paymentMethod) || "fake_vnpay".equalsIgnoreCase(paymentMethod);
    }

    private void createFakePayments(List<Orders> orders, String paymentMethod) {
        if (orders == null || orders.isEmpty()) {
            _isLoading.setValue(false);
            _error.setValue("Khong tim thay don hang de tao thanh toan fake");
            return;
        }

        final int[] remaining = {orders.size()};
        final boolean[] hasFailure = {false};

        for (Orders order : orders) {
            if (order == null || order.id == null || order.id.trim().isEmpty()) {
                hasFailure[0] = true;
                finishFakePaymentRequest(remaining, hasFailure);
                continue;
            }

            FakePaymentMethodRequest request = new FakePaymentMethodRequest(order.id, paymentMethod);
            orderRepository.createFakePaymentMethod(request, new Callback<Payments>() {
                @Override
                public void onResponse(Call<Payments> call, Response<Payments> response) {
                    if (!response.isSuccessful() || response.body() == null) {
                        hasFailure[0] = true;
                        _error.setValue("Loi khi tao thanh toan fake: " + response.code() + " - " + readErrorBody(response));
                    }
                    finishFakePaymentRequest(remaining, hasFailure);
                }

                @Override
                public void onFailure(Call<Payments> call, Throwable t) {
                    hasFailure[0] = true;
                    _error.setValue("Loi ket noi: " + t.getMessage());
                    finishFakePaymentRequest(remaining, hasFailure);
                }
            });
        }
    }

    private void finishFakePaymentRequest(int[] remaining, boolean[] hasFailure) {
        remaining[0]--;
        if (remaining[0] > 0) {
            return;
        }
        _isLoading.setValue(false);
        if (!hasFailure[0]) {
            _orderSuccess.setValue(true);
        }
    }

    private void createMomoCheckout(OrderCreateRequest order) {
        orderRepository.createMomoCheckout(order, new Callback<Payments>() {
            @Override
            public void onResponse(Call<Payments> call, Response<Payments> response) {
                _isLoading.setValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    MomoPaymentTarget target = buildMomoPaymentTarget(response.body());
                    if (target != null) {
                        _momoPaymentTarget.setValue(target);
                    } else {
                        String momoError = findMomoPaymentError(response.body());
                        _error.setValue(momoError != null ? momoError : "Khong tao duoc lien ket thanh toan MoMo");
                    }
                } else {
                    _error.setValue("Loi khi tao thanh toan MoMo: " + response.code() + " - " + readErrorBody(response));
                }
            }

            @Override
            public void onFailure(Call<Payments> call, Throwable t) {
                _isLoading.setValue(false);
                _error.setValue("Loi ket noi: " + t.getMessage());
            }
        });
    }

    private MomoPaymentTarget buildMomoPaymentTarget(Payments payment) {
        if (payment == null || payment.payment_data == null) {
            return null;
        }
        String deeplink = getStringValue(payment.payment_data, "deeplink");
        String payUrl = getStringValue(payment.payment_data, "payUrl");
        String primaryUrl = hasText(deeplink) ? deeplink : payUrl;
        if (!hasText(primaryUrl)) {
            return null;
        }

        String fallbackUrl = null;
        if (hasText(deeplink) && hasText(payUrl) && !deeplink.equals(payUrl)) {
            fallbackUrl = payUrl;
        }
        return new MomoPaymentTarget(primaryUrl, fallbackUrl);
    }

    private String findMomoPaymentError(Payments payment) {
        if (payment == null || payment.payment_data == null) {
            return null;
        }
        Object response = payment.payment_data.get("momo_create_response");
        if (!(response instanceof Map)) {
            return null;
        }
        @SuppressWarnings("unchecked")
        Map<String, Object> momoResponse = (Map<String, Object>) response;
        String message = getStringValue(momoResponse, "message");
        return message != null && !message.isEmpty() ? "MoMo: " + message : null;
    }

    private String getStringValue(Map<String, Object> data, String key) {
        Object value = data.get(key);
        return value instanceof String ? (String) value : null;
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private String readErrorBody(Response<?> response) {
        if (response.errorBody() == null) {
            return "Khong co chi tiet loi";
        }
        try {
            String rawError = response.errorBody().string();
            JSONObject errorJson = new JSONObject(rawError);
            Object detail = errorJson.opt("detail");
            if (detail instanceof String) {
                return (String) detail;
            }
            if (detail instanceof JSONObject) {
                JSONObject detailJson = (JSONObject) detail;
                String message = detailJson.optString("message", "");
                if (!message.isEmpty()) {
                    return message;
                }
            }
            return rawError;
        } catch (Exception e) {
            return "Khong doc duoc chi tiet loi";
        }
    }
}
