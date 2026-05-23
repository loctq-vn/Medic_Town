package com.example.medictown.ui.admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
        holder.tvOrderId.setText("#" + order.id.substring(0, 8));
        holder.tvStatus.setText(order.status.toUpperCase());
        holder.tvCustomerName.setText(order.shipping_name);
        holder.tvOrderSummary.setText(String.format("$%.2f", order.total_amount));

        if ("pending".equalsIgnoreCase(order.status)) {
            holder.btnQuickAction.setVisibility(View.VISIBLE);
            holder.btnQuickAction.setText("Confirm");
        } else if ("confirmed".equalsIgnoreCase(order.status)) {
            holder.btnQuickAction.setVisibility(View.VISIBLE);
            holder.btnQuickAction.setText("Ship");
        } else {
            holder.btnQuickAction.setVisibility(View.GONE);
        }

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
        TextView tvOrderId, tvStatus, tvCustomerName, tvOrderSummary;
        MaterialButton btnDetails, btnQuickAction;

        ViewHolder(View itemView) {
            super(itemView);
            tvOrderId = itemView.findViewById(R.id.tvOrderId);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvCustomerName = itemView.findViewById(R.id.tvCustomerName);
            tvOrderSummary = itemView.findViewById(R.id.tvOrderSummary);
            btnDetails = itemView.findViewById(R.id.btnDetails);
            btnQuickAction = itemView.findViewById(R.id.btnQuickAction);
        }
    }
}
