package com.example.medictown.ui.payment;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.medictown.data.models.Address;
import com.example.medictown.data.models.CartItem;
import com.example.medictown.data.models.OrderItem;
import com.example.medictown.data.models.Orders;
import com.example.medictown.data.repositories.CartRepository;
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
    private final CartRepository cartRepository;

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
        cartRepository = new CartRepository();
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

    public void placeOrder(String userId, String paymentMethod, String note) {
        Address address = _selectedAddress.getValue();
        if (address == null) {
            _error.setValue("Vui lòng chọn địa chỉ nhận hàng");
            return;
        }

        List<CartItem> items = _selectedItems.getValue();
        if (items == null || items.isEmpty()) {
            _error.setValue("Giỏ hàng trống");
            return;
        }

        _isLoading.setValue(true);

        Orders order = new Orders();
        order.user_id = userId;
        order.status = "pending";
        order.payment_method = paymentMethod;
        order.total_amount = _totalAmount.getValue();
        order.note = note;
        order.shipping_name = address.recipient_name;
        order.shipping_phone = address.phone_number;
        order.shipping_address = address.location;

        orderRepository.createOrder(order, new Callback<List<Orders>>() {
            @Override
            public void onResponse(Call<List<Orders>> call, Response<List<Orders>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    String orderId = response.body().get(0).id;
                    createOrderItems(orderId, items, userId);
                } else {
                    _isLoading.setValue(false);
                    _error.setValue("Lỗi khi tạo đơn hàng: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<Orders>> call, Throwable t) {
                _isLoading.setValue(false);
                _error.setValue("Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    private void createOrderItems(String orderId, List<CartItem> cartItems, String userId) {
        List<OrderItem> orderItems = new ArrayList<>();
        for (CartItem cartItem : cartItems) {
            OrderItem item = new OrderItem();
            item.order_id = orderId;
            item.product_id = cartItem.product_id;
            if (cartItem.products != null) {
                item.product_name = cartItem.products.name;
                if (cartItem.products.images != null && !cartItem.products.images.isEmpty()) {
                    item.product_image = cartItem.products.images.get(0);
                }
                item.price = (cartItem.products.sale_price != null && cartItem.products.sale_price > 0)
                        ? cartItem.products.sale_price
                        : cartItem.products.price;
            }
            item.quantity = cartItem.quantity;
            orderItems.add(item);
        }

        orderRepository.createOrderItems(orderItems, new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    clearCart(userId);
                } else {
                    _isLoading.setValue(false);
                    _error.setValue("Lỗi khi tạo chi tiết đơn hàng: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                _isLoading.setValue(false);
                _error.setValue("Lỗi kết nối khi tạo chi tiết: " + t.getMessage());
            }
        });
    }

    private void clearCart(String userId) {
        cartRepository.clearCart(userId, null, new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                _isLoading.setValue(false);
                _orderSuccess.setValue(true);
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                // Thậm chí nếu xóa giỏ hàng lỗi, đơn hàng đã được tạo thành công
                _isLoading.setValue(false);
                _orderSuccess.setValue(true);
            }
        });
    }
}
