package com.example.medictown.ui.payment;

import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.medictown.R;
import com.example.medictown.data.models.CartItem;
import com.example.medictown.databinding.ItemPaymentProductBinding;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PaymentProductAdapter extends RecyclerView.Adapter<PaymentProductAdapter.ViewHolder> {

    private List<CartItem> itemList = new ArrayList<>();
    private final NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

    public void setItems(List<CartItem> items) {
        this.itemList = items;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemPaymentProductBinding binding = ItemPaymentProductBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CartItem item = itemList.get(position);
        if (item.products == null) return;

        holder.binding.tvProductName.setText(item.products.name);
        holder.binding.tvQuantity.setText("x" + item.quantity);

        double price = (item.products.sale_price != null && item.products.sale_price > 0)
                ? item.products.sale_price
                : item.products.price;

        holder.binding.tvProductPrice.setText(formatter.format(price * item.quantity));

        if (item.products.sale_price != null && item.products.sale_price > 0) {
            holder.binding.tvOldPrice.setText(formatter.format(item.products.price * item.quantity));
            holder.binding.tvOldPrice.setPaintFlags(holder.binding.tvOldPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            holder.binding.tvOldPrice.setVisibility(android.view.View.VISIBLE);
        } else {
            holder.binding.tvOldPrice.setVisibility(android.view.View.GONE);
        }

        String imageUrl = (item.products.images != null && !item.products.images.isEmpty())
                ? item.products.images.get(0)
                : "";

        Glide.with(holder.itemView.getContext())
                .load(imageUrl)
                .placeholder(R.drawable.ic_product)
                .error(R.drawable.ic_product)
                .into(holder.binding.imgProduct);
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final ItemPaymentProductBinding binding;
        public ViewHolder(ItemPaymentProductBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
