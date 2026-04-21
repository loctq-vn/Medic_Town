package com.example.medictown.ui.cart;// Thay bằng package name của bạn

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medictown.R;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    // Hiện tại chỉ trả về 2 item giả (dummy) để giao diện hiển thị ra như thiết kế
    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cart_product, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        // Code behind xử lý dữ liệu truyền vào tvProductName, tvPrice... sẽ nằm ở đây
    }

    @Override
    public int getItemCount() {
        return 2; // Hiển thị 2 cục item như trong hình
    }

    static class CartViewHolder extends RecyclerView.ViewHolder {
        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            // Ánh xạ các nút ở đây: findViewById(...)
        }
    }
}