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
import com.example.medictown.databinding.ItemProductFeaturedBinding;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FeaturedProductAdapter extends RecyclerView.Adapter<FeaturedProductAdapter.FeaturedProductViewHolder> {
    private List<Products> productList = new ArrayList<>();
    private ProductAdapter.OnProductClickListener listener;

    public void setOnProductClickListener(ProductAdapter.OnProductClickListener listener) {
        this.listener = listener;
    }

    public void setProductList(List<Products> productList) {
        this.productList = productList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public FeaturedProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemProductFeaturedBinding binding = ItemProductFeaturedBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new FeaturedProductViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull FeaturedProductViewHolder holder, int position) {
        holder.bind(productList.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    static class FeaturedProductViewHolder extends RecyclerView.ViewHolder {
        private final ItemProductFeaturedBinding binding;

        public FeaturedProductViewHolder(ItemProductFeaturedBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Products product, ProductAdapter.OnProductClickListener listener) {
            binding.tvProductName.setText(product.name);

            NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
            
            if (product.sale_price != null && product.sale_price > 0 && product.sale_price < product.price) {
                binding.tvPrice.setText(formatter.format(product.sale_price));
                
                binding.tvDiscount.setVisibility(View.VISIBLE);
                double discountPercent = ((product.price - product.sale_price) / product.price) * 100;
                binding.tvDiscount.setText("-" + Math.round(discountPercent) + "%");
            } else {
                binding.tvPrice.setText(formatter.format(product.price));
                binding.tvDiscount.setVisibility(View.GONE);
            }

            // Hiển thị rating thực tế từ database
            if (product.total_reviews > 0) {
                binding.tvRating.setText(String.format(Locale.getDefault(), "★ %.1f", product.average_rating));
                binding.tvSold.setText(String.format(Locale.getDefault(), "Đã bán %d", product.total_reviews * 5 + 10)); // Giả lập số đã bán dựa trên review
            } else {
                binding.tvRating.setText("★ 5.0");
                binding.tvSold.setText("Đã bán 0");
            }

            if (product.images != null && !product.images.isEmpty()) {
                Glide.with(binding.imgProduct.getContext())
                        .load(product.images.get(0))
                        .placeholder(R.drawable.ic_product)
                        .error(R.drawable.ic_product)
                        .into(binding.imgProduct);
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onProductClick(product, binding.imgProduct);
                }
            });
        }

    }
}
