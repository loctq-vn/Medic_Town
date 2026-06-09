package com.example.medictown.ui.product;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.medictown.R;
import com.example.medictown.data.models.Advertisement;
import com.example.medictown.databinding.ItemAdBannerBinding;

import java.util.ArrayList;
import java.util.List;

public class AdBannerAdapter extends RecyclerView.Adapter<AdBannerAdapter.ViewHolder> {
    public interface OnAdClickListener {
        void onAdClick(Advertisement advertisement);
    }

    private final List<Advertisement> advertisements = new ArrayList<>();
    private OnAdClickListener listener;

    public void setAdvertisements(List<Advertisement> items) {
        advertisements.clear();
        if (items != null) {
            advertisements.addAll(items);
        }
        notifyDataSetChanged();
    }

    public void setOnAdClickListener(OnAdClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemAdBannerBinding binding = ItemAdBannerBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(advertisements.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return advertisements.size();
    }

    public Advertisement getAdvertisement(int position) {
        if (position < 0 || position >= advertisements.size()) return null;
        return advertisements.get(position);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemAdBannerBinding binding;

        ViewHolder(ItemAdBannerBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Advertisement advertisement, OnAdClickListener listener) {
            Glide.with(binding.imgAdBanner)
                    .load(advertisement.image_url)
                    .placeholder(R.drawable.bg_ad_banner_placeholder)
                    .error(R.drawable.bg_ad_banner_placeholder)
                    .centerCrop()
                    .into(binding.imgAdBanner);

            binding.getRoot().setOnClickListener(v -> {
                if (listener != null) {
                    listener.onAdClick(advertisement);
                }
            });
        }
    }
}
