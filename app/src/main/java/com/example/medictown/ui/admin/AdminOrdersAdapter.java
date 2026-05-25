package com.example.medictown.ui.admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.medictown.R;
import com.example.medictown.data.models.Orders;
import com.google.android.material.button.MaterialButton;
import java.util.ArrayList;
import java.util.List;

public class AdminOrdersAdapter extends RecyclerView.Adapter<AdminOrdersAdapter.ViewHolder> {
    private List<Orders> orders = new ArrayList<>();
    private OnOrderActionListener listener;

    public interface OnOrderActionListener {
        void onQuickAction(Orders order);
        void onDetails(Orders order);
    }

    public void setOrders(List<Orders> orders) {
        this.orders = orders;
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
        holder.tvOrderId.setText("ID: #" + (order.id.length() > 8 ? order.id.substring(0, 8) : order.id));
        holder.tvCustomerName.setText(order.shipping_name);
        holder.tvOrderTime.setText(order.created_at != null ? order.created_at.toString() : "Vừa xong");
        
        String status = order.status != null ? order.status.toLowerCase() : "pending";
        String statusDisplay = "CHỜ XÁC NHẬN";
        int statusColor = 0xFFC6E4F4; // Secondary Container blue
        int textColor = 0xFF2E4B57;
        int actionBtnColor = -1; // Default primary

        switch (status) {
            case "pending":
                statusDisplay = "CHỜ XÁC NHẬN";
                holder.btnQuickAction.setText("Xác nhận");
                holder.btnQuickAction.setVisibility(View.VISIBLE);
                break;
            case "confirmed":
                statusDisplay = "ĐÃ XÁC NHẬN";
                holder.btnQuickAction.setText("Giao hàng");
                holder.btnQuickAction.setVisibility(View.VISIBLE);
                statusColor = 0xFFE1BEE7;
                textColor = 0xFF4A148C;
                break;
            case "shipping":
                statusDisplay = "ĐANG GIAO";
                holder.btnQuickAction.setText("Theo dõi");
                holder.btnQuickAction.setVisibility(View.VISIBLE);
                statusColor = 0xFF0052CC;
                textColor = 0xFFFFFFFF;
                actionBtnColor = 0xFFF3F4F6; // Light gray surface
                break;
            case "completed":
                statusDisplay = "HOÀN THÀNH";
                holder.btnQuickAction.setVisibility(View.GONE);
                statusColor = 0xFFC8E6C9;
                textColor = 0xFF2E7D32;
                break;
            case "cancelled":
                statusDisplay = "ĐÃ HỦY";
                holder.btnQuickAction.setText("Re-stock");
                holder.btnQuickAction.setVisibility(View.VISIBLE);
                statusColor = 0xFFFFDAD6;
                textColor = 0xFF93000A;
                holder.itemView.setAlpha(0.75f);
                break;
        }

        holder.tvStatus.setText(statusDisplay);
        holder.tvStatus.getBackground().setTint(statusColor);
        holder.tvStatus.setTextColor(textColor);
        
        if (actionBtnColor != -1) {
            holder.btnQuickAction.setBackgroundTintList(android.content.res.ColorStateList.valueOf(actionBtnColor));
            holder.btnQuickAction.setTextColor(0xFF1F2937);
        } else {
            holder.btnQuickAction.setBackgroundTintList(null); // Reset to default primary
            holder.btnQuickAction.setTextColor(0xFFFFFFFF);
        }

        int itemCount = order.order_items != null ? order.order_items.size() : 0;
        if (itemCount > 0 && order.order_items.get(0).product_name != null) {
            holder.tvProductName.setText(order.order_items.get(0).product_name + (itemCount > 1 ? " (x" + itemCount + ")" : ""));
        } else {
            holder.tvProductName.setText("Đơn hàng #" + (order.id.length() > 8 ? order.id.substring(0, 8) : order.id));
        }
        
        holder.tvOrderSummary.setText(String.format(java.util.Locale.getDefault(), "%d sản phẩm • %s", itemCount, order.payment_method));
        holder.tvTotalPrice.setText(String.format(java.util.Locale.getDefault(), "%,.0fđ", order.total_amount));

        holder.btnQuickAction.setOnClickListener(v -> {
            if (listener != null) listener.onQuickAction(order);
        });

        holder.btnDetails.setOnClickListener(v -> {
            if (listener != null) listener.onDetails(order);
        });
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderId, tvStatus, tvCustomerName, tvOrderTime, tvProductName, tvOrderSummary, tvTotalPrice;
        MaterialButton btnDetails, btnQuickAction;
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
            ivProduct = itemView.findViewById(R.id.ivProduct);
        }
    }
}
