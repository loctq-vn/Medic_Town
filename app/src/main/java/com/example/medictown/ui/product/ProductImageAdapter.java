package com.example.medictown.ui.product;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.medictown.R;
import com.example.medictown.databinding.ItemProductImageThumbnailBinding;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ProductImageAdapter extends RecyclerView.Adapter<ProductImageAdapter.ViewHolder> {
    private final List<String> images = new ArrayList<>();
    private final OnImageClickListener listener;
    private OnImageRemoveListener removeListener;
    private int selectedPosition = 0;
    private boolean showRemoveButton = false;

    public interface OnImageClickListener {
        void onImageClick(String imageUrl);
    }

    public interface OnImageRemoveListener {
        void onImageRemove(int position, String imageUrl);
    }

    public ProductImageAdapter(OnImageClickListener listener) {
        this.listener = listener;
    }

    public void setShowRemoveButton(boolean showRemoveButton) {
        this.showRemoveButton = showRemoveButton;
        notifyDataSetChanged();
    }

    public void setOnImageRemoveListener(OnImageRemoveListener removeListener) {
        this.removeListener = removeListener;
    }

    public void setImages(List<String> imageUrls) {
        images.clear();
        if (imageUrls != null) {
            images.addAll(imageUrls);
        }
        selectedPosition = 0;
        notifyDataSetChanged();
    }

    public void removeImageAt(int position) {
        if (position < 0 || position >= images.size()) {
            return;
        }
        images.remove(position);
        if (images.isEmpty()) {
            selectedPosition = 0;
        } else if (selectedPosition >= images.size()) {
            selectedPosition = images.size() - 1;
        } else if (position < selectedPosition) {
            selectedPosition--;
        }
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, images.size() - position);
    }

    public void moveImage(int fromPosition, int toPosition) {
        if (
                fromPosition < 0
                        || toPosition < 0
                        || fromPosition >= images.size()
                        || toPosition >= images.size()
                        || fromPosition == toPosition
        ) {
            return;
        }

        Collections.swap(images, fromPosition, toPosition);
        if (selectedPosition == fromPosition) {
            selectedPosition = toPosition;
        } else if (selectedPosition == toPosition) {
            selectedPosition = fromPosition;
        }
        notifyItemMoved(fromPosition, toPosition);
        notifyItemChanged(fromPosition);
        notifyItemChanged(toPosition);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemProductImageThumbnailBinding binding = ItemProductImageThumbnailBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(images.get(position), position == selectedPosition);
    }

    @Override
    public int getItemCount() {
        return images.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemProductImageThumbnailBinding binding;

        ViewHolder(ItemProductImageThumbnailBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(String imageUrl, boolean selected) {
            binding.thumbnailFrame.setBackgroundResource(
                    selected
                            ? R.drawable.bg_product_thumbnail_selected
                            : R.drawable.bg_product_thumbnail_unselected
            );
            binding.btnRemoveImage.setVisibility(showRemoveButton ? android.view.View.VISIBLE : android.view.View.GONE);

            Glide.with(binding.imgThumbnail.getContext())
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_product)
                    .error(R.drawable.ic_product)
                    .into(binding.imgThumbnail);

            binding.getRoot().setOnClickListener(v -> {
                int position = getBindingAdapterPosition();
                if (position == RecyclerView.NO_POSITION) {
                    return;
                }

                int previousPosition = selectedPosition;
                selectedPosition = position;
                notifyItemChanged(previousPosition);
                notifyItemChanged(selectedPosition);

                if (listener != null) {
                    listener.onImageClick(images.get(position));
                }
            });

            binding.btnRemoveImage.setOnClickListener(v -> {
                int position = getBindingAdapterPosition();
                if (position == RecyclerView.NO_POSITION || removeListener == null) {
                    return;
                }
                removeListener.onImageRemove(position, images.get(position));
            });
        }
    }
}
