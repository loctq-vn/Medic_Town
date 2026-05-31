package com.example.medictown.ui.admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.medictown.R;
import com.example.medictown.data.models.Products;
import java.util.ArrayList;
import java.util.List;

public class AdminInventoryAdapter extends RecyclerView.Adapter<AdminInventoryAdapter.ViewHolder> {
    private List<Products> products = new ArrayList<>();
    private OnProductActionListener listener;

    public interface OnProductActionListener {
        void onEditProduct(Products product);
    }

    public void setProducts(List<Products> products) {
        this.products = products;
        notifyDataSetChanged();
    }

    public void setOnProductActionListener(OnProductActionListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_inventory, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Products product = products.get(position);
        holder.tvProductName.setText(product.name);
        holder.tvSKU.setText("ID: " + (product.id.length() > 8 ? product.id.substring(0, 8) : product.id));
        holder.tvStockCount.setText(String.valueOf(product.stock));
        holder.tvPrice.setText(formatPriceWithUnit(product));

        // Set status label based on stock
        String statusText;
        int statusColor;
        int textColor;

        if (product.stock <= 0) {
            statusText = "HẾT HÀNG";
            statusColor = 0xFFFFCDD2; // Light Red
            textColor = 0xFFC62828; // Dark Red
        } else if (product.stock < 10) {
            statusText = "SẮP HẾT";
            statusColor = 0xFFFFECB3; // Light Amber
            textColor = 0xFF856404; // Dark Amber
        } else {
            statusText = "CÒN HÀNG";
            statusColor = 0xFFC8E6C9; // Light Green
            textColor = 0xFF2E7D32; // Dark Green
        }

        View tvStatusLabel = holder.itemView.findViewById(R.id.tvStatusLabel);
        if (tvStatusLabel instanceof TextView) {
            ((TextView) tvStatusLabel).setText(statusText);
            tvStatusLabel.getBackground().setTint(statusColor);
            ((TextView) tvStatusLabel).setTextColor(textColor);
        }

        String imageUrl = (product.images != null && !product.images.isEmpty()) ? product.images.get(0) : null;
        Glide.with(holder.itemView.getContext())
                .load(imageUrl)
                .placeholder(R.drawable.ic_product)
                .into(holder.ivProduct);

        holder.btnEdit.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEditProduct(product);
            }
        });
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    private String formatPriceWithUnit(Products product) {
        String formattedPrice = String.format(java.util.Locale.getDefault(), "%,.0fđ", product.price);
        if (product.unit == null || product.unit.trim().isEmpty()) {
            return formattedPrice;
        }
        return formattedPrice + " / " + product.unit.trim();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProduct;
        TextView tvProductName, tvSKU, tvStockCount, tvPrice;
        android.widget.ImageButton btnEdit;

        ViewHolder(View itemView) {
            super(itemView);
            ivProduct = itemView.findViewById(R.id.ivProduct);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvSKU = itemView.findViewById(R.id.tvSKU);
            tvStockCount = itemView.findViewById(R.id.tvStockCount);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            btnEdit = itemView.findViewById(R.id.btnEdit);
        }
    }
}
