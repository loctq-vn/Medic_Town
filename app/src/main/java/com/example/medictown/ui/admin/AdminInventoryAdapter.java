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
        holder.tvSKU.setText("ID: " + product.id);
        holder.tvStockCount.setText("Stock: " + product.stock);
        holder.tvPrice.setText("$" + String.format("%.2f", product.price));

        if (product.stock < 10) {
            holder.tvStockCount.setTextColor(holder.itemView.getContext().getColor(R.color.error));
        } else {
            holder.tvStockCount.setTextColor(holder.itemView.getContext().getColor(R.color.on_surface));
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
