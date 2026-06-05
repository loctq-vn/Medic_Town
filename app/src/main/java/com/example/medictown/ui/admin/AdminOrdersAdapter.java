package com.example.medictown.ui.admin;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.medictown.R;
import com.example.medictown.data.models.Orders;
import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AdminOrdersAdapter extends RecyclerView.Adapter<AdminOrdersAdapter.ViewHolder> {
    private List<Orders> orders = new ArrayList<>();
    private OnOrderActionListener listener;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

    public interface OnOrderActionListener {
        void onQuickAction(Orders order);
        void onDetails(Orders order);
        void onCancel(Orders order);
    }

    public void setOrders(List<Orders> orders) {
        this.orders = orders != null ? orders : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void setOnOrderActionListener(OnOrderActionListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_order, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Orders order = orders.get(position);
        String orderId = order.id != null ? order.id : "";
        int itemCount = order.order_items != null ? order.order_items.size() : 0;

        holder.itemView.setAlpha(1f);
        holder.tvOrderId.setText("ID: #" + (orderId.length() > 8 ? orderId.substring(0, 8) : orderId));
        holder.tvCustomerName.setText(order.shipping_name != null ? order.shipping_name : "Khách hàng");
        holder.tvOrderTime.setText(order.created_at != null ? dateFormat.format(order.created_at) : "Vừa xong");

        bindStatus(holder, order.status);
        bindProduct(holder, order, itemCount, orderId);

        String paymentMethod = displayPaymentMethod(order.getPaymentMethod());
        holder.tvOrderSummary.setText(String.format(Locale.getDefault(), "%d sản phẩm • %s", itemCount, paymentMethod));
        holder.tvTotalPrice.setText(String.format(Locale.getDefault(), "%,.0fđ", order.total_amount != null ? order.total_amount : 0));

        holder.btnQuickAction.setOnClickListener(v -> {
            if (listener != null) listener.onQuickAction(order);
        });

        holder.btnDetails.setOnClickListener(v -> {
            if (listener != null) listener.onDetails(order);
        });

        holder.btnCancel.setOnClickListener(v -> {
            if (listener != null) listener.onCancel(order);
        });
    }

    private void bindStatus(ViewHolder holder, String rawStatus) {
        String status = rawStatus != null ? rawStatus.toLowerCase() : "pending";
        String statusDisplay = "CHỜ XÁC NHẬN";
        int statusColor = 0xFFC6E4F4;
        int textColor = 0xFF2E4B57;
        int actionBtnColor = holder.itemView.getContext().getColor(R.color.admin_primary);

        switch (status) {
            case "pending":
                holder.btnQuickAction.setText("Xác nhận");
                holder.btnQuickAction.setVisibility(View.VISIBLE);
                holder.btnCancel.setVisibility(View.VISIBLE);
                holder.btnDetails.setVisibility(View.VISIBLE);
                break;
            case "confirmed":
                statusDisplay = "ĐÃ XÁC NHẬN";
                holder.btnQuickAction.setText("Giao hàng");
                holder.btnQuickAction.setVisibility(View.VISIBLE);
                holder.btnCancel.setVisibility(View.VISIBLE);
                holder.btnDetails.setVisibility(View.VISIBLE);
                statusColor = 0xFFFFE0B2; // Light Orange
                textColor = 0xFFE65100; // Dark Orange
                break;
            case "shipping":
                statusDisplay = "ĐANG GIAO";
                holder.btnQuickAction.setText("Theo dõi");
                holder.btnQuickAction.setVisibility(View.VISIBLE);
                holder.btnCancel.setVisibility(View.GONE);
                holder.btnDetails.setVisibility(View.GONE);
                statusColor = 0xFF0052CC;
                textColor = 0xFFFFFFFF;
                break;
            case "completed":
                statusDisplay = "HOÀN THÀNH";
                holder.btnQuickAction.setVisibility(View.GONE);
                holder.btnCancel.setVisibility(View.GONE);
                holder.btnDetails.setVisibility(View.VISIBLE);
                statusColor = 0xFFC8E6C9;
                textColor = 0xFF2E7D32;
                break;
            case "cancelled":
                statusDisplay = "ĐÃ HỦY";
                holder.btnQuickAction.setVisibility(View.GONE);
                holder.btnCancel.setVisibility(View.GONE);
                holder.btnDetails.setVisibility(View.VISIBLE);
                statusColor = 0xFFFFDAD6;
                textColor = 0xFF93000A;
                holder.itemView.setAlpha(0.75f);
                break;
        }

        holder.tvStatus.setText(statusDisplay);
        holder.tvStatus.getBackground().setTint(statusColor);
        holder.tvStatus.setTextColor(textColor);

        holder.btnQuickAction.setBackgroundTintList(ColorStateList.valueOf(actionBtnColor));
        holder.btnQuickAction.setTextColor(0xFFFFFFFF);
    }

    private void bindProduct(ViewHolder holder, Orders order, int itemCount, String orderId) {
        String imageUrl = null;
        if (itemCount > 0) {
            if (order.order_items.get(0).product_name != null) {
                holder.tvProductName.setText(order.order_items.get(0).product_name + (itemCount > 1 ? " (x" + itemCount + ")" : ""));
            }
            imageUrl = order.order_items.get(0).product_image;
        } else {
            holder.tvProductName.setText("Đơn hàng #" + (orderId.length() > 8 ? orderId.substring(0, 8) : orderId));
        }

        Glide.with(holder.itemView.getContext())
                .load(imageUrl)
                .placeholder(R.drawable.ic_medicine_placeholder)
                .error(R.drawable.ic_medicine_placeholder)
                .into(holder.ivProduct);
    }

    private String displayPaymentMethod(String method) {
        if (method == null || method.trim().isEmpty()) {
            return "COD";
        }
        if ("cash".equalsIgnoreCase(method) || "cod".equalsIgnoreCase(method)) {
            return "COD";
        }
        return method.toUpperCase();
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderId, tvStatus, tvCustomerName, tvOrderTime, tvProductName, tvOrderSummary, tvTotalPrice;
        MaterialButton btnDetails, btnQuickAction, btnCancel;
        ImageView ivProduct;

        ViewHolder(View itemView) {
            super(itemView);
            tvOrderId = itemView.findViewById(R.id.tvOrderId);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvCustomerName = itemView.findViewById(R.id.tvCustomerName);
            tvOrderTime = itemView.findViewById(R.id.tvOrderTime);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvOrderSummary = itemView.findViewById(R.id.tvOrderSummary);
            tvTotalPrice = itemView.findViewById(R.id.tvTotalPrice);
            btnDetails = itemView.findViewById(R.id.btnDetails);
            btnQuickAction = itemView.findViewById(R.id.btnQuickAction);
            btnCancel = itemView.findViewById(R.id.btnCancel);
            ivProduct = itemView.findViewById(R.id.ivProduct);
        }
    }
}
