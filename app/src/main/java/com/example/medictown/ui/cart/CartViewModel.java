package com.example.medictown.ui.cart;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.medictown.data.models.Products;
import java.util.ArrayList;
import java.util.List;

public class CartViewModel extends ViewModel {

    // Danh sách sản phẩm trong giỏ (Dùng MutableLiveData để khi danh sách thay đổi, giao diện tự động update)
    private final MutableLiveData<List<CartItemUI>> cartItemsList = new MutableLiveData<>(new ArrayList<>());

    // Biến lưu tổng tiền
    private final MutableLiveData<Double> totalAmount = new MutableLiveData<>(0.0);

    // Cho phép Fragment lấy dữ liệu ra để hiển thị
    public LiveData<List<CartItemUI>> getCartItems() { return cartItemsList; }
    public LiveData<Double> getTotalAmount() { return totalAmount; }

    // 1. CHỨC NĂNG: Tăng hoặc giảm số lượng
    public void changeQuantity(String productId, int change) {
        List<CartItemUI> currentList = cartItemsList.getValue();
        if (currentList != null) {
            for (int i = 0; i < currentList.size(); i++) {
                CartItemUI item = currentList.get(i);

                // Tìm đúng cái sản phẩm người dùng vừa bấm
                if (item.getProduct().id.equals(productId)) {
                    int newQuantity = item.getQuantity() + change; // Cộng hoặc trừ đi change

                    if (newQuantity > 0) {
                        item.setQuantity(newQuantity); // Cập nhật số lượng mới
                    } else {
                        currentList.remove(i); // Nếu lùi về 0 thì đá nó khỏi giỏ hàng luôn
                    }
                    break;
                }
            }
            cartItemsList.setValue(currentList); // Cập nhật lại danh sách
            calculateTotal(); // Tính lại tiền
        }
    }

    // 2. CHỨC NĂNG: Bấm nút thùng rác để xóa
    public void removeItem(String productId) {
        List<CartItemUI> currentList = cartItemsList.getValue();
        if (currentList != null) {
            // Xóa món hàng có ID tương ứng
            currentList.removeIf(item -> item.getProduct().id.equals(productId));
            cartItemsList.setValue(currentList);
            calculateTotal(); // Tính lại tiền
        }
    }

    // 3. CHỨC NĂNG: Tính tổng tiền
    private void calculateTotal() {
        List<CartItemUI> currentList = cartItemsList.getValue();
        double total = 0;
        if (currentList != null) {
            for (CartItemUI item : currentList) {
                if (item.isSelected()) { // Chỉ cộng tiền những món có tick chọn
                    total += (item.getEffectivePrice() * item.getQuantity());
                }
            }
        }
        totalAmount.setValue(total); // Báo cho giao diện biết tổng tiền mới
    }

    // --- HÀM TẠM THỜI ĐỂ TEST GIAO DIỆN ---
    public void addDummyData(Products p) {
        List<CartItemUI> currentList = cartItemsList.getValue();
        if (currentList != null) {
            currentList.add(new CartItemUI(p, 1));
            cartItemsList.setValue(currentList);
            calculateTotal();
        }
    }
}