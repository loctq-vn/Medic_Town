package com.example.medictown.ui.chat;

import android.content.Intent;
import android.graphics.Color;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.medictown.MainActivity;
import com.example.medictown.R;
import com.example.medictown.data.models.ChatMessageMetadata;
import com.example.medictown.data.models.ChatMessageUi;
import com.example.medictown.data.models.MessageSendState;
import com.example.medictown.data.models.Products;
import com.example.medictown.databinding.ItemChatMessageBinding;
import com.example.medictown.ui.product.ProductDetailActivity;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class ChatMessageAdapter extends RecyclerView.Adapter<ChatMessageAdapter.ViewHolder> {
    public interface RetryListener {
        void onRetry(String clientMessageId);
    }

    private static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("HH:mm");

    private final String currentUserId;
    private final boolean sellerMode;
    private final RetryListener retryListener;
    private final List<ChatMessageUi> items = new ArrayList<>();

    public ChatMessageAdapter(
            String currentUserId,
            boolean sellerMode,
            RetryListener retryListener
    ) {
        this.currentUserId = currentUserId;
        this.sellerMode = sellerMode;
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

            boolean imageMessage = "image".equals(item.message.message_type)
                    && item.message.metadata != null
                    && item.message.metadata.url != null
                    && !item.message.metadata.url.trim().isEmpty();
            if (imageMessage) {
                binding.ivAttachment.setVisibility(View.VISIBLE);
                Glide.with(binding.ivAttachment.getContext())
                        .load(item.message.metadata.url)
                        .placeholder(R.drawable.ic_medicine_placeholder)
                        .error(R.drawable.ic_medicine_placeholder)
                        .into(binding.ivAttachment);
            } else {
                binding.ivAttachment.setVisibility(View.GONE);
                binding.ivAttachment.setImageDrawable(null);
            }

            boolean richMessage = bindRichAttachment(item);
            String content = richMessage
                    ? ""
                    : item.message.content == null ? "" : item.message.content.trim();
            binding.tvContent.setVisibility(content.isEmpty() ? View.GONE : View.VISIBLE);
            binding.tvContent.setText(content);
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

        private boolean bindRichAttachment(ChatMessageUi item) {
            String messageType = item.message.message_type;
            ChatMessageMetadata metadata = item.message.metadata;
            if (!"product".equals(messageType) && !"order".equals(messageType)) {
                binding.cardRichAttachment.setVisibility(View.GONE);
                binding.cardRichAttachment.setOnClickListener(null);
                return false;
            }
            if (metadata == null) {
                binding.cardRichAttachment.setVisibility(View.GONE);
                binding.cardRichAttachment.setOnClickListener(null);
                return false;
            }

            binding.cardRichAttachment.setVisibility(View.VISIBLE);
            if ("product".equals(messageType)) {
                bindProductCard(metadata);
            } else {
                bindOrderCard(metadata);
            }
            return true;
        }

        private void bindProductCard(ChatMessageMetadata metadata) {
            binding.tvCardTitle.setText(
                    metadata.product_name == null || metadata.product_name.trim().isEmpty()
                            ? "Sản phẩm"
                            : metadata.product_name
            );
            binding.tvCardSubtitle.setText(
                    metadata.brand == null || metadata.brand.trim().isEmpty()
                            ? "Sản phẩm đính kèm"
                            : metadata.brand
            );
            binding.tvCardMeta.setText(formatProductPrice(metadata));
            loadCardImage(metadata.product_image, R.drawable.ic_product);
            binding.cardRichAttachment.setOnClickListener(view -> openProduct(metadata));
        }

        private void bindOrderCard(ChatMessageMetadata metadata) {
            String code = metadata.order_code == null || metadata.order_code.trim().isEmpty()
                    ? metadata.order_id
                    : metadata.order_code;
            binding.tvCardTitle.setText(code == null ? "Đơn hàng" : "Đơn hàng #" + code);
            binding.tvCardSubtitle.setText(displayOrderStatus(metadata.status));
            binding.tvCardMeta.setText(formatOrderTotal(metadata));
            loadCardImage(metadata.product_image, R.drawable.ic_history);
            binding.cardRichAttachment.setOnClickListener(view -> openOrder(metadata));
        }

        private void loadCardImage(String imageUrl, int placeholder) {
            Glide.with(binding.ivCardImage.getContext())
                    .load(imageUrl)
                    .placeholder(placeholder)
                    .error(placeholder)
                    .into(binding.ivCardImage);
        }

        private String formatProductPrice(ChatMessageMetadata metadata) {
            Double price = metadata.sale_price != null && metadata.sale_price > 0
                    ? metadata.sale_price
                    : metadata.price;
            if (price == null) {
                return "Xem chi tiết";
            }
            String formatted = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"))
                    .format(price);
            if (metadata.unit == null || metadata.unit.trim().isEmpty()) {
                return formatted;
            }
            return formatted + " / " + metadata.unit.trim();
        }

        private String formatOrderTotal(ChatMessageMetadata metadata) {
            String countText = metadata.items_count == null
                    ? ""
                    : metadata.items_count + " sản phẩm";
            if (metadata.total == null) {
                return countText.isEmpty() ? "Xem chi tiết" : countText;
            }
            String total = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"))
                    .format(metadata.total);
            return countText.isEmpty() ? total : countText + " · " + total;
        }

        private String displayOrderStatus(String status) {
            if (status == null) {
                return "Đơn hàng đính kèm";
            }
            switch (status) {
                case "pending":
                    return "Chờ xác nhận";
                case "confirmed":
                    return "Đã xác nhận";
                case "shipping":
                    return "Đang giao hàng";
                case "completed":
                    return "Đã hoàn thành";
                case "cancelled":
                    return "Đã hủy";
                default:
                    return "Đơn hàng đính kèm";
            }
        }

        private void openProduct(ChatMessageMetadata metadata) {
            if (metadata.product_id == null || metadata.product_id.trim().isEmpty()) {
                return;
            }
            if (sellerMode) {
                Intent intent = new Intent(binding.getRoot().getContext(), MainActivity.class);
                intent.putExtra("open_admin_product_detail", true);
                intent.putExtra("product_id", metadata.product_id);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                binding.getRoot().getContext().startActivity(intent);
                return;
            }
            Products product = new Products();
            product.id = metadata.product_id;
            product.name = metadata.product_name == null ? "Sản phẩm" : metadata.product_name;
            product.brand = metadata.brand;
            product.price = metadata.price == null ? 0 : metadata.price;
            product.sale_price = metadata.sale_price;
            product.unit = metadata.unit;
            product.requires_prescription = Boolean.TRUE.equals(metadata.requires_prescription);
            product.images = metadata.product_image == null || metadata.product_image.trim().isEmpty()
                    ? new ArrayList<>()
                    : Collections.singletonList(metadata.product_image);

            Intent intent = new Intent(binding.getRoot().getContext(), ProductDetailActivity.class);
            intent.putExtra("product", product);
            binding.getRoot().getContext().startActivity(intent);
        }

        private void openOrder(ChatMessageMetadata metadata) {
            if (metadata.order_id == null || metadata.order_id.trim().isEmpty()) {
                return;
            }
            Intent intent = new Intent(binding.getRoot().getContext(), MainActivity.class);
            intent.putExtra(
                    sellerMode ? "open_admin_order_detail" : "open_order_detail",
                    true
            );
            intent.putExtra("order_id", metadata.order_id);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            binding.getRoot().getContext().startActivity(intent);
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
