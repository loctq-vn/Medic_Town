package com.example.medictown.ui.cart;

import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.medictown.R;
import com.example.medictown.data.models.CartItem;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private List<CartItem> cartItems = new ArrayList<>();
    private Map<String, String> subcategoryNames = new HashMap<>();
    private OnCartItemInteractionListener listener;

    public interface OnCartItemInteractionListener {
        void onIncreaseQuantity(CartItem item);
        void onDecreaseQuantity(CartItem item);
        void onDeleteItem(CartItem item);
        void onToggleSelection(CartItem item);
    }

    public void setOnCartItemInteractionListener(OnCartItemInteractionListener listener) {
        this.listener = listener;
    }

    public void setCartItems(List<CartItem> items) {
        this.cartItems = items;
        notifyDataSetChanged();
    }

    public void setSubcategoryNames(Map<String, String> subcategoryNames) {
        this.subcategoryNames = subcategoryNames != null ? subcategoryNames : new HashMap<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cart_product, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartItem item = cartItems.get(position);
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

        if (item.products != null) {
            holder.tvProductName.setText(item.products.name);
            String categoryName = item.products.subcategory_id != null
                    ? subcategoryNames.get(item.products.subcategory_id)
                    : null;
            if (categoryName != null && !categoryName.trim().isEmpty()) {
                holder.layoutTags.setVisibility(View.VISIBLE);
                holder.tvCategoryTag.setText(categoryName);
            } else {
                holder.layoutTags.setVisibility(View.GONE);
            }
            holder.tvBrand.setText(buildBrandAndUnitText(item.products.brand, item.products.unit));

            double unitPrice = (item.products.sale_price != null && item.products.sale_price > 0) 
                ? item.products.sale_price 
                : item.products.price;
            
            holder.tvPrice.setText(formatter.format(unitPrice * item.quantity));
            
            if (item.products.sale_price != null && item.products.sale_price > 0 && item.products.sale_price < item.products.price) {
                holder.tvOldPrice.setVisibility(View.VISIBLE);
                holder.tvOldPrice.setText(formatter.format(item.products.price * item.quantity));
                holder.tvOldPrice.setPaintFlags(holder.tvOldPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            } else {
                holder.tvOldPrice.setVisibility(View.GONE);
            }

            if (item.products.images != null && !item.products.images.isEmpty()) {
                Glide.with(holder.itemView.getContext())
                        .load(item.products.images.get(0))
                        .placeholder(R.drawable.ic_launcher_background)
                        .into(holder.imgProduct);
            }
        } else {
            holder.layoutTags.setVisibility(View.GONE);
        }

        holder.tvQuantity.setText(String.format(Locale.getDefault(), "%02d", item.quantity));
        
        holder.cbSelect.setOnCheckedChangeListener(null);
        holder.cbSelect.setChecked(item.isSelected);
        holder.cbSelect.setOnCheckedChangeListener((buttonView, isChecked) -> {
            item.isSelected = isChecked;
            if (listener != null) listener.onToggleSelection(item);
        });

        holder.btnPlus.setOnClickListener(v -> {
            if (listener != null) listener.onIncreaseQuantity(item);
        });

        holder.btnMinus.setOnClickListener(v -> {
            if (listener != null && item.quantity > 1) {
                listener.onDecreaseQuantity(item);
            }
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) listener.onDeleteItem(item);
        });
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    private String buildBrandAndUnitText(String brand, String unit) {
        StringBuilder text = new StringBuilder();
        if (brand != null && !brand.trim().isEmpty()) {
            text.append("Hãng: ").append(brand.trim());
        }
        if (unit != null && !unit.trim().isEmpty()) {
            if (text.length() > 0) {
                text.append(" - ");
            }
            text.append("Đơn vị: ").append(unit.trim());
        }
        return text.length() > 0 ? text.toString() : "Đơn vị chưa cập nhật";
    }

    static class CartViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProduct, btnDelete;
        LinearLayout layoutTags;
        TextView tvProductName, tvBrand, tvPrice, tvOldPrice, tvQuantity, tvCategoryTag, btnMinus, btnPlus;
        CheckBox cbSelect;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.imgProduct);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvBrand = itemView.findViewById(R.id.tvBrand);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvOldPrice = itemView.findViewById(R.id.tvOldPrice);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            tvCategoryTag = itemView.findViewById(R.id.tvCategoryTag);
            layoutTags = itemView.findViewById(R.id.layoutTags);
            btnMinus = itemView.findViewById(R.id.btnMinus);
            btnPlus = itemView.findViewById(R.id.btnPlus);
            cbSelect = itemView.findViewById(R.id.cbSelect);
        }
    }
}
