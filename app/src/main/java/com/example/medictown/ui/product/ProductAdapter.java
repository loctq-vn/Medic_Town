package com.example.medictown.ui.product;

import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.medictown.R;
import com.example.medictown.data.models.Products;
import com.example.medictown.databinding.ItemProductBinding;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {
    private List<Products> productList = new ArrayList<>();

    private OnProductClickListener listener;

    public interface OnProductClickListener {
        void onProductClick(Products product);
    }

    public void setOnProductClickListener(OnProductClickListener listener) {
        this.listener = listener;
    }

    public void setProductList(List<Products> productList) {
        this.productList = productList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemProductBinding binding = ItemProductBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ProductViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Products product = productList.get(position);
        holder.bind(product, listener);
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    static class ProductViewHolder extends RecyclerView.ViewHolder {
        private final ItemProductBinding binding;

        public ProductViewHolder(ItemProductBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Products product, OnProductClickListener listener) {
            binding.tvProductName.setText(product.name);
            binding.tvBrand.setText(product.brand);
            binding.tvPackaging.setText(product.usage);

            NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
            
            if (product.sale_price != null && product.sale_price > 0 && product.sale_price < product.price) {
                binding.tvPrice.setText(formatter.format(product.sale_price));
                binding.tvOldPrice.setVisibility(View.VISIBLE);
                binding.tvOldPrice.setText(formatter.format(product.price));
                binding.tvOldPrice.setPaintFlags(binding.tvOldPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                
                binding.tvDiscount.setVisibility(View.VISIBLE);
                double discountPercent = ((product.price - product.sale_price) / product.price) * 100;
                binding.tvDiscount.setText("-" + Math.round(discountPercent) + "%");
            } else {
                binding.tvPrice.setText(formatter.format(product.price));
                binding.tvOldPrice.setVisibility(View.GONE);
                binding.tvDiscount.setVisibility(View.GONE);
            }

            if (product.requires_prescription) {
                binding.tvBadge.setVisibility(View.VISIBLE);
                binding.tvBadge.setText("RX required");
            } else if (product.is_best_seller) {
                binding.tvBadge.setVisibility(View.VISIBLE);
                binding.tvBadge.setText("BEST SELLER");
            } else {
                binding.tvBadge.setVisibility(View.GONE);
            }

            // Hiển thị hình ảnh sử dụng Glide
            if (product.images != null && !product.images.isEmpty()) {
                String imageUrl = product.images.get(0);
                Glide.with(binding.imgProduct.getContext())
                        .load(imageUrl)
                        .placeholder(R.drawable.ic_launcher_background) // Ảnh tạm trong khi chờ tải
                        .error(R.drawable.ic_launcher_foreground)       // Ảnh khi lỗi
                        .into(binding.imgProduct);
            } else {
                binding.imgProduct.setImageResource(R.drawable.ic_launcher_background);
            }

            // Thiết lập sự kiện click cho nút "Xem chi tiết"
            binding.btnAddToCart.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onProductClick(product);
                }
            });

            // Thiết lập sự kiện click cho toàn bộ item (tùy chọn)
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onProductClick(product);
                }
            });
        }
    }
}
