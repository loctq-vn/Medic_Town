package com.example.medictown.ui.cart;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.medictown.R;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private List<CartItemUI> items = new ArrayList<>();
    private final CartListener listener;

    // Cái này giống như bộ đàm, để Adapter gọi cho Fragment khi có người bấm nút
    public interface CartListener {
        void onIncrease(String productId);
        void onDecrease(String productId);
        void onDelete(String productId);
    }

    public CartAdapter(CartListener listener) {
        this.listener = listener;
    }

    // Cập nhật lại danh sách khi có thay đổi (Thêm/Sửa/Xóa)
    public void submitList(List<CartItemUI> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cart_product, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartItemUI itemUI = items.get(position);
        DecimalFormat df = new DecimalFormat("###,###,###"); // Định dạng tiền VNĐ

        // Hiển thị tên và hãng
        holder.tvProductName.setText(itemUI.getProduct().name);
        holder.tvBrand.setText("Hãng: " + itemUI.getProduct().manufacturer);

        // Hiển thị giá và số lượng
        holder.tvPrice.setText(df.format(itemUI.getEffectivePrice()) + "đ");
        holder.tvQuantity.setText(String.format("%02d", itemUI.getQuantity()));

        // Tick checkbox
        holder.cbSelect.setChecked(itemUI.isSelected());

        // Cài đặt sự kiện khi bấm nút (+), (-) và Xóa
        holder.btnPlus.setOnClickListener(v -> listener.onIncrease(itemUI.getProduct().id));
        holder.btnMinus.setOnClickListener(v -> listener.onDecrease(itemUI.getProduct().id));
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(itemUI.getProduct().id));
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    static class CartViewHolder extends RecyclerView.ViewHolder {
        TextView tvProductName, tvBrand, tvPrice, tvQuantity, btnMinus, btnPlus;
        ImageView btnDelete;
        CheckBox cbSelect;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvBrand = itemView.findViewById(R.id.tvBrand);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            btnPlus = itemView.findViewById(R.id.btnPlus);
            btnMinus = itemView.findViewById(R.id.btnMinus);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            cbSelect = itemView.findViewById(R.id.cbSelect);
        }
    }
}