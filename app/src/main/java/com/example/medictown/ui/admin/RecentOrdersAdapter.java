package com.example.medictown.ui.admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medictown.R;
import com.example.medictown.data.models.Orders;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class RecentOrdersAdapter extends RecyclerView.Adapter<RecentOrdersAdapter.ViewHolder> {
    private List<Orders> orders = new ArrayList<>();

    public void setOrders(List<Orders> orders) {
        this.orders = orders != null ? orders : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_recent_order, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Orders order = orders.get(position);
        String orderId = order.id != null ? order.id : "";
        String shortOrderId = orderId.length() > 8 ? orderId.substring(0, 8) : orderId;

        holder.tvRecentOrderId.setText("#" + shortOrderId);
        holder.tvRecentCustomerName.setText(
                order.shipping_name != null && !order.shipping_name.isEmpty()
                        ? order.shipping_name
                        : "Khach hang"
        );
        holder.tvRecentAmount.setText(String.format(
                Locale.getDefault(),
                "%,.0f\u0111",
                order.total_amount != null ? order.total_amount : 0
        ));

        String status = order.status != null ? order.status.toLowerCase(Locale.ROOT) : "pending";
        holder.tvRecentStatus.setText(orderStatusLabel(status));

        int bgColor;
        int textColor;
        switch (status) {
            case "completed":
                bgColor = 0xFFC8E6C9;
                textColor = 0xFF2E7D32;
                break;
            case "cancelled":
                bgColor = 0xFFFFCDD2;
                textColor = 0xFFC62828;
                break;
            case "confirmed":
                bgColor = 0xFFFFE0B2;
                textColor = 0xFFE65100;
                break;
            case "shipping":
                bgColor = 0xFF0052CC;
                textColor = 0xFFFFFFFF;
                break;
            default:
                bgColor = 0xFFC6E4F4;
                textColor = 0xFF2E4B57;
                break;
        }
        holder.tvRecentStatus.getBackground().setTint(bgColor);
        holder.tvRecentStatus.setTextColor(textColor);
    }

    private String orderStatusLabel(String status) {
        switch (status) {
            case "confirmed":
                return "\u0110\u00e3 x\u00e1c nh\u1eadn";
            case "shipping":
                return "\u0110ang giao";
            case "completed":
                return "Ho\u00e0n th\u00e0nh";
            case "cancelled":
                return "\u0110\u00e3 h\u1ee7y";
            case "pending":
            default:
                return "Ch\u1edd x\u00e1c nh\u1eadn";
        }
    }

    @Override
    public int getItemCount() {
        return Math.min(orders.size(), 5);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvRecentOrderId, tvRecentCustomerName, tvRecentAmount, tvRecentStatus;

        ViewHolder(View itemView) {
            super(itemView);
            tvRecentOrderId = itemView.findViewById(R.id.tvRecentOrderId);
            tvRecentCustomerName = itemView.findViewById(R.id.tvRecentCustomerName);
            tvRecentAmount = itemView.findViewById(R.id.tvRecentAmount);
            tvRecentStatus = itemView.findViewById(R.id.tvRecentStatus);
        }
    }
}
