package com.example.medictown.ui.product;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.medictown.R;
import com.example.medictown.data.models.Reviews;
import com.example.medictown.databinding.ItemProductReviewBinding;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ProductReviewAdapter extends RecyclerView.Adapter<ProductReviewAdapter.ViewHolder> {
    private List<Reviews> reviewsList = new ArrayList<>();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    public void setReviews(List<Reviews> reviews) {
        this.reviewsList = reviews;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemProductReviewBinding binding = ItemProductReviewBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(reviewsList.get(position));
    }

    @Override
    public int getItemCount() {
        return reviewsList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemProductReviewBinding binding;

        public ViewHolder(ItemProductReviewBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Reviews review) {
            if (review.users != null) {
                binding.tvUserName.setText(review.users.name != null ? review.users.name : "Người dùng");
                if (review.users.avatar_url != null) {
                    Glide.with(binding.ivAvatar.getContext())
                            .load(review.users.avatar_url)
                            .placeholder(R.drawable.ic_profile)
                            .into(binding.ivAvatar);
                }
            } else {
                binding.tvUserName.setText("Người dùng");
            }

            binding.tvRating.setText(String.format(Locale.getDefault(), "%.1f", (float) review.rating));
            binding.tvComment.setText(review.comment);
            
            if (review.created_at != null) {
                binding.tvTime.setText(dateFormat.format(review.created_at));
            }
        }
    }
}
