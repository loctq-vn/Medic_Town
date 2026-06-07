package com.example.medictown.ui.chat;

import android.graphics.Color;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medictown.R;
import com.example.medictown.data.models.ChatMessageUi;
import com.example.medictown.data.models.MessageSendState;
import com.example.medictown.databinding.ItemChatMessageBinding;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ChatMessageAdapter extends RecyclerView.Adapter<ChatMessageAdapter.ViewHolder> {
    public interface RetryListener {
        void onRetry(String clientMessageId);
    }

    private static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("HH:mm");

    private final String currentUserId;
    private final RetryListener retryListener;
    private final List<ChatMessageUi> items = new ArrayList<>();

    public ChatMessageAdapter(String currentUserId, RetryListener retryListener) {
        this.currentUserId = currentUserId;
        this.retryListener = retryListener;
    }

    public void submitList(List<ChatMessageUi> messages) {
        items.clear();
        if (messages != null) {
            items.addAll(messages);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemChatMessageBinding binding = ItemChatMessageBinding.inflate(
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
        private final ItemChatMessageBinding binding;

        ViewHolder(ItemChatMessageBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(ChatMessageUi item) {
            if (item == null || item.message == null) {
                return;
            }

            boolean mine = currentUserId != null
                    && currentUserId.equals(item.message.sender_id);
            LinearLayout.LayoutParams params =
                    (LinearLayout.LayoutParams) binding.messageContainer.getLayoutParams();
            params.gravity = mine ? Gravity.END : Gravity.START;
            binding.messageContainer.setLayoutParams(params);
            binding.messageContainer.setBackgroundResource(
                    mine ? R.drawable.bg_chat_customer : R.drawable.bg_chat_seller
            );

            binding.tvContent.setText(item.message.content);
            binding.tvContent.setTextColor(
                    mine ? Color.WHITE : binding.getRoot().getContext()
                            .getColor(R.color.on_surface)
            );
            binding.tvMeta.setText(buildMeta(item));
            binding.tvMeta.setTextColor(
                    mine ? 0xFFDCE8FF : binding.getRoot().getContext()
                            .getColor(R.color.text_gray)
            );

            boolean failed = item.sendState == MessageSendState.FAILED;
            binding.messageContainer.setOnClickListener(failed ? view -> {
                if (retryListener != null
                        && item.message.client_message_id != null) {
                    retryListener.onRetry(item.message.client_message_id);
                }
            } : null);
            binding.messageContainer.setClickable(failed);
        }

        private String buildMeta(ChatMessageUi item) {
            String time = formatTime(item.message.created_at);
            if (item.sendState == MessageSendState.SENDING) {
                return time + " · Đang gửi...";
            }
            if (item.sendState == MessageSendState.FAILED) {
                return time + " · Gửi thất bại, chạm để thử lại";
            }
            return time;
        }

        private String formatTime(String value) {
            if (value == null || value.trim().isEmpty()) {
                return "";
            }
            try {
                return TIME_FORMAT.format(
                        Instant.parse(value).atZone(ZoneId.systemDefault())
                );
            } catch (Exception ignored) {
                return "";
            }
        }
    }
}
