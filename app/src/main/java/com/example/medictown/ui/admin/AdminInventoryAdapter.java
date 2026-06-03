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
import java.util.Locale;

public class AdminInventoryAdapter extends RecyclerView.Adapter<AdminInventoryAdapter.ViewHolder> {
    private List<Products> products = new ArrayList<>();
    private OnProductActionListener listener;

    public interface OnProductActionListener {
        void onEditProduct(Products product);
    }

    public void setProducts(List<Products> products) {
        this.products = products == null ? new ArrayList<>() : products;
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
        holder.tvProductName.setText(isBlank(product.name) ? "S\u1ea3n ph\u1ea9m" : product.name);
        holder.tvDescription.setText(buildDescription(product));
        holder.tvSKU.setText("ID: " + buildShortId(product.id));
        holder.tvStockCount.setText(String.valueOf(product.stock));
        holder.tvUnit.setText(isBlank(product.unit) ? "\u0111\u01a1n v\u1ecb" : product.unit.trim());
        holder.tvPrice.setText(formatPriceWithUnit(product));

        int statusColor;
        int textColor;
        String statusText;
        if (product.stock <= 0) {
            statusText = "H\u1ebeT H\u00c0NG";
            statusColor = 0xFFFFCDD2;
            textColor = 0xFFC62828;
        } else if (product.stock < 10) {
            statusText = "S\u1eaeP H\u1ebeT";
            statusColor = 0xFFFFECB3;
            textColor = 0xFF856404;
        } else {
            statusText = "C\u00d2N H\u00c0NG";
            statusColor = 0xFFC8E6C9;
            textColor = 0xFF2E7D32;
        }
        holder.tvStatusLabel.setText(statusText);
        holder.tvStatusLabel.getBackground().setTint(statusColor);
        holder.tvStatusLabel.setTextColor(textColor);

        String imageUrl = (product.images != null && !product.images.isEmpty()) ? product.images.get(0) : null;
        Glide.with(holder.itemView.getContext())
                .load(imageUrl)
                .placeholder(R.drawable.ic_product)
                .error(R.drawable.ic_product)
                .into(holder.ivProduct);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEditProduct(product);
            }
        });
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    private String buildShortId(String id) {
        if (isBlank(id)) {
            return "-";
        }
        String trimmedId = id.trim();
        return trimmedId.length() > 8 ? trimmedId.substring(0, 8).toUpperCase(Locale.ROOT) : trimmedId.toUpperCase(Locale.ROOT);
    }

    private String buildDescription(Products product) {
        List<String> parts = new ArrayList<>();
        if (!isBlank(product.brand)) {
            parts.add(product.brand.trim());
        }
        if (!isBlank(product.manufacturer)) {
            parts.add(product.manufacturer.trim());
        }
        if (!isBlank(product.uses)) {
            parts.add(product.uses.trim());
        }
        if (parts.isEmpty()) {
            return "\u0110ang c\u1eadp nh\u1eadt th\u00f4ng tin s\u1ea3n ph\u1ea9m";
        }
        return String.join(" • ", parts);
    }

    private String formatPriceWithUnit(Products product) {
        return String.format(Locale.getDefault(), "%,.0f\u0111", product.price);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProduct;
        TextView tvProductName, tvDescription, tvSKU, tvStatusLabel, tvStockCount, tvUnit, tvPrice;

        ViewHolder(View itemView) {
            super(itemView);
            ivProduct = itemView.findViewById(R.id.ivProduct);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvSKU = itemView.findViewById(R.id.tvSKU);
            tvStatusLabel = itemView.findViewById(R.id.tvStatusLabel);
            tvStockCount = itemView.findViewById(R.id.tvStockCount);
            tvUnit = itemView.findViewById(R.id.tvUnit);
            tvPrice = itemView.findViewById(R.id.tvPrice);
        }
    }
}
