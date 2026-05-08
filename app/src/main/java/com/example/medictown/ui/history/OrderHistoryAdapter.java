package com.example.medictown.ui.history;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.medictown.data.models.Orders;
import com.example.medictown.databinding.ItemOrderHistoryBinding;
import com.bumptech.glide.Glide;
import com.example.medictown.R;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class OrderHistoryAdapter extends RecyclerView.Adapter<OrderHistoryAdapter.OrderViewHolder> {
    private List<Orders> ordersList = new ArrayList<>();
    private final NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

    public interface OnOrderClickListener {
        void onDetailClick(Orders order);
    }

    private OnOrderClickListener listener;

    public void setOnOrderClickListener(OnOrderClickListener listener) {
        this.listener = listener;
    }

    public void setOrders(List<Orders> orders) {
        this.ordersList = orders;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemOrderHistoryBinding binding = ItemOrderHistoryBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new OrderViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        holder.bind(ordersList.get(position));
    }

    @Override
    public int getItemCount() {
        return ordersList.size();
    }

    class OrderViewHolder extends RecyclerView.ViewHolder {
        private final ItemOrderHistoryBinding binding;

        public OrderViewHolder(ItemOrderHistoryBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Orders order) {
            binding.tvOrderId.setText("#MC-" + (order.id != null && order.id.length() > 8 ? order.id.substring(0, 8) : order.id));
            binding.tvOrderDate.setText(order.created_at != null ? "Ngày đặt: " + dateFormat.format(order.created_at) : "");
            binding.tvOrderTotal.setText(formatter.format(order.total_amount));
            binding.tvOrderStatus.setText(getStatusText(order.status));

            binding.btnDetails.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDetailClick(order);
                }
            });
            
            // Show first item name and more if any
            if (order.order_items != null && !order.order_items.isEmpty()) {
                String firstItem = order.order_items.get(0).product_name;
                if (order.order_items.size() > 1) {
                    binding.tvOrderSummary.setText(firstItem + " và " + (order.order_items.size() - 1) + " sản phẩm khác");
                } else {
                    binding.tvOrderSummary.setText(firstItem);
                }

                // Load preview image
                if (order.order_items.get(0).product_image != null) {
                    Glide.with(binding.ivOrderPreview.getContext())
                            .load(order.order_items.get(0).product_image)
                            .placeholder(R.drawable.ic_product)
                            .into(binding.ivOrderPreview);
                }
            } else {
                binding.tvOrderSummary.setText("Không có thông tin sản phẩm");
            }
        }

        private String getStatusText(String status) {
            if (status == null) return "Chờ xác nhận";
            switch (status) {
                case "pending": return "Chờ xác nhận";
                case "confirmed": return "Đã xác nhận";
                case "shipping": return "Đang giao";
                case "completed": return "Đã hoàn thành";
                case "cancelled": return "Đã hủy";
                default: return status;
            }
        }
    }
}
