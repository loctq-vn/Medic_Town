package com.example.medictown.ui.product;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.medictown.data.models.Products;
import com.example.medictown.databinding.ItemProductBinding;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {
    private List<Products> productList = new ArrayList<>();

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
        holder.bind(product);
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

        public void bind(Products product) {
            binding.tvProductName.setText(product.name);
            binding.tvBrand.setText(product.brand);
            binding.tvPackaging.setText(product.usage); // Using usage as packaging info for now

            NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
            binding.tvPrice.setText(formatter.format(product.price));

            if (product.requires_prescription) {
                binding.tvBadge.setVisibility(View.VISIBLE);
                binding.tvBadge.setText("RX required");
            } else if (product.is_best_seller) {
                binding.tvBadge.setVisibility(View.VISIBLE);
                binding.tvBadge.setText("BEST SELLER");
            } else {
                binding.tvBadge.setVisibility(View.GONE);
            }

            // Note: In a real app, use Glide or Picasso to load image from product.images.get(0)
            // binding.imgProduct.setImageResource(...)
        }
    }
}
