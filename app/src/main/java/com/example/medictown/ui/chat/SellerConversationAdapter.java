package com.example.medictown.ui.chat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.medictown.R;
import com.example.medictown.data.models.ChatMessage;
import com.example.medictown.data.models.SellerConversationItem;
import com.example.medictown.databinding.ItemSellerConversationBinding;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class SellerConversationAdapter extends
        RecyclerView.Adapter<SellerConversationAdapter.ViewHolder> {

    public interface ConversationClickListener {
        void onConversationClick(SellerConversationItem item);
    }

    private static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM");

    private final ConversationClickListener listener;
    private final List<SellerConversationItem> items = new ArrayList<>();

    public SellerConversationAdapter(ConversationClickListener listener) {
        this.listener = listener;
    }

    public void submitList(List<SellerConversationItem> conversations) {
        items.clear();
        if (conversations != null) {
            items.addAll(conversations);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemSellerConversationBinding binding =
                ItemSellerConversationBinding.inflate(
                        LayoutInflater.from(parent.getContext()),
                        parent,
                        false
                );
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemSellerConversationBinding binding;

        ViewHolder(ItemSellerConversationBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(SellerConversationItem item) {
            String customerName = item.customer != null
                    && item.customer.name != null
                    && !item.customer.name.trim().isEmpty()
                    ? item.customer.name
                    : "Khách hàng";
            binding.tvCustomerName.setText(customerName);
            binding.tvLastMessage.setText(lastMessageText(item.last_message));
            binding.tvTime.setText(formatTime(
                    item.last_message != null
                            ? item.last_message.created_at
                            : item.conversation != null
                                    ? item.conversation.last_message_at
                                    : null
            ));

            if (item.unread_count > 0) {
                binding.tvUnreadBadge.setVisibility(View.VISIBLE);
                binding.tvUnreadBadge.setText(
                        item.unread_count > 99 ? "99+" : String.valueOf(item.unread_count)
                );
            } else {
                binding.tvUnreadBadge.setVisibility(View.GONE);
            }

            if (item.customer != null
                    && item.customer.avatar_url != null
                    && !item.customer.avatar_url.trim().isEmpty()) {
                binding.imgAvatar.setPadding(0, 0, 0, 0);
                Glide.with(binding.imgAvatar)
                        .load(item.customer.avatar_url)
                        .placeholder(R.drawable.ic_profile)
                        .error(R.drawable.ic_profile)
                        .into(binding.imgAvatar);
            } else {
                int padding = (int) (10 * binding.imgAvatar.getResources()
                        .getDisplayMetrics().density);
                binding.imgAvatar.setPadding(padding, padding, padding, padding);
                binding.imgAvatar.setImageResource(R.drawable.ic_profile);
            }

            binding.getRoot().setOnClickListener(view -> {
                if (listener != null) {
                    listener.onConversationClick(item);
                }
            });
        }

        private String lastMessageText(ChatMessage message) {
            if (message == null || message.content == null) {
                return "Chưa có tin nhắn";
            }
            String prefix = "seller".equals(message.sender_type) ? "Bạn: " : "";
            return prefix + message.content;
        }

        private String formatTime(String value) {
            if (value == null || value.trim().isEmpty()) {
                return "";
            }
            try {
                Instant instant = Instant.parse(value);
                LocalDate messageDate = instant.atZone(ZoneId.systemDefault()).toLocalDate();
                if (LocalDate.now().equals(messageDate)) {
                    return TIME_FORMAT.format(instant.atZone(ZoneId.systemDefault()));
                }
                return DATE_FORMAT.format(messageDate);
            } catch (Exception ignored) {
                return "";
            }
        }
    }
}
