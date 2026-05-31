package com.example.medictown.ui.admin;

import android.view.LayoutInflater;
import android.view.View;
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

public class AdminOrderDetailProductAdapter extends RecyclerView.Adapter<AdminOrderDetailProductAdapter.ViewHolder> {
    private final NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
    private List<OrderItem> items = new ArrayList<>();

    public void setItems(List<OrderItem> items) {
        this.items = items != null ? items : new ArrayList<>();
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
        OrderItem item = items.get(position);
        holder.binding.tvProductName.setText(item.product_name != null ? item.product_name : "Sản phẩm");
        holder.binding.tvQuantity.setText(String.format(Locale.getDefault(),
                "Đơn giá: %s • SL: %02d", formatter.format(item.price), item.quantity));
        holder.binding.tvPrice.setText(formatter.format(item.price * item.quantity));
        holder.binding.btnReviewItem.setVisibility(View.GONE);

        Glide.with(holder.itemView.getContext())
                .load(item.product_image)
                .placeholder(R.drawable.ic_product)
                .error(R.drawable.ic_product)
                .into(holder.binding.ivProduct);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ItemOrderDetailProductBinding binding;

        ViewHolder(ItemOrderDetailProductBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
