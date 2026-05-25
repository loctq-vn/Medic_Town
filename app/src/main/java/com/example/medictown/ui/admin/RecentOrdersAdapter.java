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
        this.orders = orders;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_recent_order, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Orders order = orders.get(position);
        holder.tvRecentOrderId.setText("#" + (order.id.length() > 8 ? order.id.substring(0, 8) : order.id));
        holder.tvRecentCustomerName.setText(order.shipping_name);
        holder.tvRecentAmount.setText(String.format(Locale.getDefault(), "%,.0fđ", order.total_amount));
        
        String status = order.status != null ? order.status.toUpperCase() : "PENDING";
        holder.tvRecentStatus.setText(status);
        
        // Simple status styling
        int bgColor;
        int textColor;
        switch (status) {
            case "COMPLETED":
                bgColor = 0xFFC8E6C9; // Light Green
                textColor = 0xFF2E7D32; // Dark Green
                break;
            case "CANCELLED":
                bgColor = 0xFFFFCDD2; // Light Red
                textColor = 0xFFC62828; // Dark Red
                break;
            default:
                bgColor = 0xFFC6E4F4; // Light Blue
                textColor = 0xFF2E4B57; // Dark Blue
                break;
        }
        holder.tvRecentStatus.getBackground().setTint(bgColor);
        holder.tvRecentStatus.setTextColor(textColor);
    }

    @Override
    public int getItemCount() {
        return Math.min(orders.size(), 5); // Only show top 5
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
