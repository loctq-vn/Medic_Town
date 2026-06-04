package com.example.medictown.ui.admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medictown.R;
import com.example.medictown.data.models.RevenueDashboard;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class RecentOrdersAdapter extends RecyclerView.Adapter<RecentOrdersAdapter.ViewHolder> {
    private List<RevenueDashboard.RecentOrder> orders = new ArrayList<>();

    public void setOrders(List<RevenueDashboard.RecentOrder> orders) {
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
        RevenueDashboard.RecentOrder order = orders.get(position);
        String orderId = order.orderId != null ? order.orderId : "";
        String shortOrderId = orderId.length() > 8 ? orderId.substring(0, 8) : orderId;

        holder.tvRecentOrderId.setText("#" + shortOrderId);
        holder.tvRecentCustomerName.setText(
                order.customerName != null && !order.customerName.isEmpty()
                        ? order.customerName
                        : "Khach hang"
        );
        holder.tvRecentAmount.setText(String.format(Locale.getDefault(), "%,.0fđ", order.amount));

        String status = order.orderStatus != null ? order.orderStatus.toUpperCase() : "PENDING";
        holder.tvRecentStatus.setText(status);

        int bgColor;
        int textColor;
        switch (status) {
            case "COMPLETED":
                bgColor = 0xFFC8E6C9;
                textColor = 0xFF2E7D32;
                break;
            case "CANCELLED":
                bgColor = 0xFFFFCDD2;
                textColor = 0xFFC62828;
                break;
            case "CONFIRMED":
                bgColor = 0xFFFFE0B2;
                textColor = 0xFFE65100;
                break;
            default:
                bgColor = 0xFFC6E4F4;
                textColor = 0xFF2E4B57;
                break;
        }
        holder.tvRecentStatus.getBackground().setTint(bgColor);
        holder.tvRecentStatus.setTextColor(textColor);
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
