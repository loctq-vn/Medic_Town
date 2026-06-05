package com.example.medictown.ui.admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.medictown.R;
import com.example.medictown.data.models.Products;
import com.google.android.material.card.MaterialCardView;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AdminInventoryAdapter extends RecyclerView.Adapter<AdminInventoryAdapter.ViewHolder> {
    private List<Products> products = new ArrayList<>();
    private OnProductActionListener listener;

    public interface OnProductActionListener {
        void onEditProduct(Products product);
        void onCloneProduct(Products product);
        void onStopSellingProduct(Products product);
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
        holder.tvSKU.setText("SKU: " + buildShortId(product.id));
        bindPrice(holder, product);
        applyItemState(holder, product);
        bindStockDisplay(holder, product);
        bindStatus(holder, product);
        bindImage(holder, product);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEditProduct(product);
            }
        });
        holder.btnProductMenu.setOnClickListener(v -> showProductMenu(holder.btnProductMenu, product));
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    private void bindStatus(ViewHolder holder, Products product) {
        int statusColor;
        int textColor;
        String statusText;
        if (!product.is_active) {
            statusText = "Ng\u1eebng b\u00e1n";
            statusColor = 0xFFE4E7EC;
            textColor = 0xFF6B7280;
        } else if (product.stock <= 0) {
            statusText = "H\u1ebft h\u00e0ng";
            statusColor = 0xFFFFDAD6;
            textColor = 0xFFD23B3B;
        } else if (product.stock <= 20) {
            statusText = "S\u1eafp h\u1ebft h\u00e0ng";
            statusColor = 0xFFFFECB3;
            textColor = 0xFFB7791F;
        } else {
            statusText = "C\u00f2n h\u00e0ng";
            statusColor = 0xFFC8E6C9;
            textColor = 0xFF2E7D32;
        }
        holder.tvStatusLabel.setText(statusText);
        holder.tvStatusLabel.getBackground().mutate().setTint(statusColor);
        holder.tvStatusLabel.setTextColor(textColor);
    }

    private void bindStockDisplay(ViewHolder holder, Products product) {
        holder.tvStockCount.setText("S\u1ed1 l\u01b0\u1ee3ng: " + product.stock);
        holder.tvStockCount.setTextColor(holder.itemView.getContext().getColor(R.color.on_surface_variant));
        holder.tvUnit.setVisibility(View.GONE);
    }

    private void showProductMenu(View anchor, Products product) {
        PopupMenu popupMenu = new PopupMenu(anchor.getContext(), anchor);
        popupMenu.getMenu().add("Ch\u1ec9nh s\u1eeda");
        popupMenu.getMenu().add("T\u1ea1o b\u1ea3n sao");
        popupMenu.getMenu().add("Ng\u1eebng b\u00e1n");
        popupMenu.setOnMenuItemClickListener(item -> {
            if (listener == null) {
                return true;
            }
            String title = item.getTitle().toString();
            if ("Ch\u1ec9nh s\u1eeda".equals(title)) {
                listener.onEditProduct(product);
            } else if ("T\u1ea1o b\u1ea3n sao".equals(title)) {
                listener.onCloneProduct(product);
            } else if ("Ng\u1eebng b\u00e1n".equals(title)) {
                listener.onStopSellingProduct(product);
            }
            return true;
        });
        popupMenu.show();
    }

    private void bindImage(ViewHolder holder, Products product) {
        String imageUrl = (product.images != null && !product.images.isEmpty()) ? product.images.get(0) : null;
        Glide.with(holder.itemView.getContext())
                .load(imageUrl)
                .placeholder(R.drawable.ic_product)
                .error(R.drawable.ic_product)
                .into(holder.ivProduct);
    }

    private void bindPrice(ViewHolder holder, Products product) {
        boolean hasSalePrice = product.sale_price != null && product.sale_price > 0 && product.sale_price < product.price;
        if (hasSalePrice) {
            holder.tvPrice.setVisibility(View.VISIBLE);
            holder.tvPrice.setText(formatPrice(product.price));
            holder.tvSalePrice.setText(formatPrice(product.sale_price));
            return;
        }

        holder.tvPrice.setVisibility(View.GONE);
        holder.tvSalePrice.setText(formatPrice(product.price));
    }

    private void applyItemState(ViewHolder holder, Products product) {
        if (product.is_active) {
            holder.cardInventoryItem.setCardBackgroundColor(holder.itemView.getContext().getColor(R.color.white));
            holder.cardInventoryItem.setStrokeColor(holder.itemView.getContext().getColor(android.R.color.transparent));
            holder.tvProductName.setTextColor(holder.itemView.getContext().getColor(R.color.admin_text_main));
            holder.tvDescription.setTextColor(holder.itemView.getContext().getColor(R.color.on_surface_variant));
            holder.tvSKU.setTextColor(0xFFBAC0D6);
            holder.tvStockCount.setTextColor(holder.itemView.getContext().getColor(R.color.admin_text_main));
            holder.tvUnit.setTextColor(holder.itemView.getContext().getColor(R.color.on_surface_variant));
            holder.tvPrice.setTextColor(holder.itemView.getContext().getColor(R.color.on_surface_variant));
            holder.tvSalePrice.setTextColor(holder.itemView.getContext().getColor(R.color.admin_primary));
            holder.ivProduct.setAlpha(1.0f);
            return;
        }

        holder.cardInventoryItem.setCardBackgroundColor(holder.itemView.getContext().getColor(R.color.inventory_inactive_bg));
        holder.cardInventoryItem.setStrokeColor(holder.itemView.getContext().getColor(R.color.inventory_inactive_stroke));
        holder.tvProductName.setTextColor(holder.itemView.getContext().getColor(R.color.inventory_inactive_text));
        holder.tvDescription.setTextColor(holder.itemView.getContext().getColor(R.color.inventory_inactive_subtext));
        holder.tvSKU.setTextColor(holder.itemView.getContext().getColor(R.color.inventory_inactive_subtext));
        holder.tvStockCount.setTextColor(holder.itemView.getContext().getColor(R.color.inventory_inactive_alert));
        holder.tvUnit.setTextColor(holder.itemView.getContext().getColor(R.color.inventory_inactive_alert));
        holder.tvPrice.setTextColor(holder.itemView.getContext().getColor(R.color.inventory_inactive_subtext));
        holder.tvSalePrice.setTextColor(holder.itemView.getContext().getColor(R.color.admin_primary));
        holder.ivProduct.setAlpha(0.58f);
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
        return String.join(" / ", parts);
    }

    private String formatPrice(double price) {
        return String.format(new Locale("vi", "VN"), "%,.0f\u0111", price);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProduct;
        ImageButton btnProductMenu;
        MaterialCardView cardInventoryItem;
        TextView tvProductName, tvDescription, tvSKU, tvStatusLabel, tvStockCount, tvUnit, tvPrice, tvSalePrice;

        ViewHolder(View itemView) {
            super(itemView);
            cardInventoryItem = itemView.findViewById(R.id.cardInventoryItem);
            ivProduct = itemView.findViewById(R.id.ivProduct);
            btnProductMenu = itemView.findViewById(R.id.btnProductMenu);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvSKU = itemView.findViewById(R.id.tvSKU);
            tvStatusLabel = itemView.findViewById(R.id.tvStatusLabel);
            tvStockCount = itemView.findViewById(R.id.tvStockCount);
            tvUnit = itemView.findViewById(R.id.tvUnit);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvSalePrice = itemView.findViewById(R.id.tvSalePrice);
        }
    }
}
