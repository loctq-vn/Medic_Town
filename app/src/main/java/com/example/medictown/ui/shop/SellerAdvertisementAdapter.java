package com.example.medictown.ui.shop;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.medictown.R;
import com.example.medictown.data.models.Advertisement;
import com.example.medictown.databinding.ItemSellerAdvertisementBinding;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SellerAdvertisementAdapter
        extends RecyclerView.Adapter<SellerAdvertisementAdapter.ViewHolder> {

    public interface OnAdvertisementActionListener {
        void onToggle(Advertisement item, boolean enabled);
        void onDetails(Advertisement item);
        void onEdit(Advertisement item);
        void onDelete(Advertisement item);
    }

    private final List<Advertisement> items = new ArrayList<>();
    private final OnAdvertisementActionListener listener;

    public SellerAdvertisementAdapter(OnAdvertisementActionListener listener) {
        this.listener = listener;
    }

    public void setItems(List<Advertisement> values) {
        items.clear();
        if (values != null) items.addAll(values);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(ItemSellerAdvertisementBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        ));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(items.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemSellerAdvertisementBinding binding;

        ViewHolder(ItemSellerAdvertisementBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Advertisement item, OnAdvertisementActionListener listener) {
            binding.tvAdTitle.setText(item.title);
            binding.tvAdDescription.setText(item.description == null ? "" : item.description);
            binding.tvAdPosition.setText(positionLabel(item.position));
            binding.tvAdDates.setText(dateRange(item));
            binding.tvAdViews.setText(formatCount(item.view_count) + " lượt xem");
            binding.tvAdClicks.setText(formatCount(item.click_count) + " lượt nhấn");

            boolean hasBudget = item.budget_amount != null && item.budget_amount > 0;
            int performance = hasBudget
                    ? (int) Math.min(100, Math.round(item.spent_amount * 100 / item.budget_amount))
                    : 0;
            binding.adPerformanceGroup.setVisibility(hasBudget ? View.VISIBLE : View.GONE);
            binding.tvAdBudget.setText("Ngân sách: " + money(item.spent_amount)
                    + " / " + money(item.budget_amount == null ? 0 : item.budget_amount));
            binding.tvAdPerformance.setText(performance + "%");
            binding.progressAdPerformance.setProgress(performance);

            Glide.with(binding.ivAdBanner)
                    .load(item.image_url)
                    .centerCrop()
                    .placeholder(R.drawable.bg_ad_banner_placeholder)
                    .error(R.drawable.bg_ad_banner_placeholder)
                    .into(binding.ivAdBanner);

            boolean expired = "expired".equals(item.status);
            binding.switchAdActive.setOnCheckedChangeListener(null);
            binding.switchAdActive.setEnabled(!expired);
            binding.switchAdActive.setChecked(item.is_active && "active".equals(item.status));
            binding.switchAdActive.setOnCheckedChangeListener((button, checked) -> {
                if (listener != null) listener.onToggle(item, checked);
            });

            bindStatus(item.status);
            binding.getRoot().setAlpha(
                    "paused".equals(item.status) || expired ? 0.82f : 1f
            );
            binding.btnAdDetails.setText(expired ? "Xem lại" : "Xem chi tiết");
            binding.btnAdDetails.setOnClickListener(v -> {
                if (listener != null) listener.onDetails(item);
            });
            binding.btnEditAd.setVisibility(expired ? View.GONE : View.VISIBLE);
            binding.btnEditAd.setOnClickListener(v -> {
                if (listener != null) listener.onEdit(item);
            });
            binding.btnDeleteAd.setOnClickListener(v -> {
                if (listener != null) listener.onDelete(item);
            });
        }

        private void bindStatus(String status) {
            int background;
            int textColor;
            String label;
            if ("draft".equals(status)) {
                background = R.drawable.bg_ad_status_off;
                textColor = R.color.ad_text_secondary;
                label = "●  Bản nháp";
            } else if ("paused".equals(status)) {
                background = R.drawable.bg_ad_status_off;
                textColor = R.color.ad_text_secondary;
                label = "●  Tạm tắt";
            } else if ("expired".equals(status)) {
                background = R.drawable.bg_ad_status_expired;
                textColor = R.color.ad_danger;
                label = "●  Hết hạn";
            } else {
                background = R.drawable.bg_ad_status_running;
                textColor = R.color.ad_text;
                label = "●  Đang chạy";
            }
            binding.tvAdStatus.setBackgroundResource(background);
            binding.tvAdStatus.setTextColor(
                    ContextCompat.getColor(binding.getRoot().getContext(), textColor)
            );
            binding.tvAdStatus.setText(label);

            int activeColor = ContextCompat.getColor(
                    binding.getRoot().getContext(),
                    R.color.ad_primary
            );
            int disabledColor = ContextCompat.getColor(
                    binding.getRoot().getContext(),
                    R.color.ad_disabled
            );
            binding.switchAdActive.setTrackTintList(new ColorStateList(
                    new int[][]{
                            new int[]{android.R.attr.state_checked},
                            new int[]{}
                    },
                    new int[]{activeColor, disabledColor}
            ));
        }

        private static String dateRange(Advertisement item) {
            SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            String start = item.start_date == null ? "Không giới hạn" : format.format(item.start_date);
            String end = item.end_date == null ? "Không giới hạn" : format.format(item.end_date);
            return start + " - " + end;
        }

        private static String formatCount(int value) {
            if (value >= 1_000_000) return String.format(Locale.US, "%.1fM", value / 1_000_000d);
            if (value >= 1_000) return String.format(Locale.US, "%.1fk", value / 1_000d);
            return String.valueOf(value);
        }

        private static String money(double value) {
            return NumberFormat.getNumberInstance(new Locale("vi", "VN")).format(value) + "đ";
        }

        private static String positionLabel(String position) {
            if ("product_list".equals(position)) return "Banner sản phẩm";
            if ("popup".equals(position)) return "Popup";
            if ("checkout_banner".equals(position)) return "Thanh toán";
            return "Trang chủ";
        }
    }
}
