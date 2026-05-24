package com.example.medictown.ui.shop;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.medictown.R;
import com.example.medictown.data.models.Shop;
import com.example.medictown.databinding.ItemShopSelectionBinding;

import java.util.ArrayList;
import java.util.List;

public class ShopSelectionAdapter extends RecyclerView.Adapter<ShopSelectionAdapter.ViewHolder> {
    public interface OnShopClickListener {
        void onShopClick(Shop shop);
    }

    private final List<Shop> shops = new ArrayList<>();
    private final OnShopClickListener listener;

    public ShopSelectionAdapter(OnShopClickListener listener) {
        this.listener = listener;
    }

    public void submitList(List<Shop> items) {
        shops.clear();
        if (items != null) {
            shops.addAll(items);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemShopSelectionBinding binding = ItemShopSelectionBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(shops.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return shops.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemShopSelectionBinding binding;

        ViewHolder(ItemShopSelectionBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Shop shop, OnShopClickListener listener) {
            binding.tvShopName.setText(shop.name);
            binding.tvShopAddress.setText(shop.address != null && !shop.address.isEmpty() ? shop.address : "Chưa có địa chỉ");
            Glide.with(binding.ivShopLogo)
                    .load(shop.logo_url)
                    .placeholder(R.drawable.ic_profile)
                    .circleCrop()
                    .into(binding.ivShopLogo);
            binding.getRoot().setOnClickListener(v -> listener.onShopClick(shop));
        }
    }
}
