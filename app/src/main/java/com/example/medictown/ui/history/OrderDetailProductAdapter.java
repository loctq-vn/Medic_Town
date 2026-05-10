package com.example.medictown.ui.history;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.medictown.R;
import com.example.medictown.data.models.OrderItem;
import com.example.medictown.databinding.ItemOrderDetailProductBinding;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class OrderDetailProductAdapter extends RecyclerView.Adapter<OrderDetailProductAdapter.ViewHolder> {
    private final List<OrderItem> items;
    private final NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
    private String orderStatus;
    private OnReviewClickListener reviewClickListener;
    private List<String> reviewedItemIds = new ArrayList<>();

    public interface OnReviewClickListener {
        void onReviewClick(OrderItem item);
    }

    public OrderDetailProductAdapter(List<OrderItem> items) {
        this.items = items;
    }

    public void setOnReviewClickListener(String orderStatus, OnReviewClickListener listener) {
        this.orderStatus = orderStatus;
        this.reviewClickListener = listener;
        notifyDataSetChanged();
    }

    public void setReviewedItemIds(List<String> reviewedItemIds) {
        this.reviewedItemIds = reviewedItemIds;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemOrderDetailProductBinding binding = ItemOrderDetailProductBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemOrderDetailProductBinding binding;

        public ViewHolder(ItemOrderDetailProductBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(OrderItem item) {
            binding.tvProductName.setText(item.product_name);
            binding.tvQuantity.setText(binding.getRoot().getContext().getString(R.string.quantity_format, String.format(Locale.getDefault(), "%02d", item.quantity)));
            binding.tvPrice.setText(formatter.format(item.price * item.quantity));

            if (item.product_image != null) {
                Glide.with(binding.ivProduct.getContext())
                        .load(item.product_image)
                        .placeholder(R.drawable.ic_product)
                        .into(binding.ivProduct);
            }

            if ("completed".equals(orderStatus)) {
                if (reviewedItemIds != null && reviewedItemIds.contains(item.id)) {
                    binding.btnReviewItem.setVisibility(android.view.View.VISIBLE);
                    binding.btnReviewItem.setText("Đã đánh giá");
                    binding.btnReviewItem.setEnabled(false);
                } else {
                    binding.btnReviewItem.setVisibility(android.view.View.VISIBLE);
                    binding.btnReviewItem.setText("Đánh giá");
                    binding.btnReviewItem.setEnabled(true);
                    binding.btnReviewItem.setOnClickListener(v -> {
                        if (reviewClickListener != null) {
                            reviewClickListener.onReviewClick(item);
                        }
                    });
                }
            } else {
                binding.btnReviewItem.setVisibility(android.view.View.GONE);
            }
        }
    }
}
