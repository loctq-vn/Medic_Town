package com.example.medictown.ui.cart;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medictown.R;
import com.example.medictown.data.api.SessionManager;
import com.example.medictown.data.models.CartItem;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class CartFragment extends Fragment {

    private CartViewModel mViewModel;
    private CartAdapter adapter;
    private SessionManager sessionManager;

    private RecyclerView rvCartItems;
    private TextView tvTotalAmount, tvSubtotalLabel, tvSubtotalValue, tvShippingFee, tvVoucherDiscount, tvClearAll;
    private Button btnCheckout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_cart, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvCartItems = view.findViewById(R.id.rvCartItems);
        tvTotalAmount = view.findViewById(R.id.tvTotalAmount);
        tvSubtotalLabel = view.findViewById(R.id.tvSubtotalLabel);
        tvSubtotalValue = view.findViewById(R.id.tvSubtotalValue);
        tvShippingFee = view.findViewById(R.id.tvShippingFee);
        tvVoucherDiscount = view.findViewById(R.id.tvVoucherDiscount);
        tvClearAll = view.findViewById(R.id.tvClearAll);
        btnCheckout = view.findViewById(R.id.btnCheckout);

        sessionManager = new SessionManager(requireContext());
        mViewModel = new ViewModelProvider(this).get(CartViewModel.class);

        setupRecyclerView();
        observeViewModel();

        if (sessionManager.isLoggedIn()) {
            mViewModel.fetchCartItems(sessionManager.getUserId(), sessionManager.getToken());
        } else {
            Toast.makeText(getContext(), "Vui lòng đăng nhập để xem giỏ hàng", Toast.LENGTH_SHORT).show();
        }

        tvClearAll.setOnClickListener(v -> {
            if (sessionManager.isLoggedIn()) {
                mViewModel.clearCart(sessionManager.getUserId(), sessionManager.getToken());
            }
        });

        btnCheckout.setOnClickListener(v -> {
            // Logic thanh toán
        });
    }

    private void setupRecyclerView() {
        adapter = new CartAdapter();
        rvCartItems.setLayoutManager(new LinearLayoutManager(getContext()));
        rvCartItems.setAdapter(adapter);

        adapter.setOnCartItemInteractionListener(new CartAdapter.OnCartItemInteractionListener() {
            @Override
            public void onIncreaseQuantity(CartItem item) {
                mViewModel.updateQuantity(item.id, item.quantity + 1, sessionManager.getToken(), sessionManager.getUserId());
            }

            @Override
            public void onDecreaseQuantity(CartItem item) {
                mViewModel.updateQuantity(item.id, item.quantity - 1, sessionManager.getToken(), sessionManager.getUserId());
            }

            @Override
            public void onDeleteItem(CartItem item) {
                mViewModel.removeFromCart(item.id, sessionManager.getToken(), sessionManager.getUserId());
            }

            @Override
            public void onToggleSelection(CartItem item) {
                if (mViewModel.cartItems.getValue() != null) {
                    calculateTotal(mViewModel.cartItems.getValue());
                }
            }
        });
    }

    private void observeViewModel() {
        mViewModel.cartItems.observe(getViewLifecycleOwner(), items -> {
            if (items != null) {
                adapter.setCartItems(items);
                calculateTotal(items);
            }
        });
    }

    private void calculateTotal(List<CartItem> items) {
        double subtotal = 0;
        int totalQuantity = 0;
        for (CartItem item : items) {
            if (item.isSelected && item.products != null) {
                double price = (item.products.sale_price != null && item.products.sale_price > 0) 
                    ? item.products.sale_price 
                    : item.products.price;
                subtotal += price * item.quantity;
                totalQuantity += item.quantity;
            }
        }
        
        double shippingFee = 0; // Hiện tại miễn phí
        double voucherDiscount = 0; // Hiện tại chưa có mã giảm giá
        double total = subtotal + shippingFee - voucherDiscount;

        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        
        tvSubtotalLabel.setText("Tạm tính (" + totalQuantity + " sản phẩm)");
        tvSubtotalValue.setText(formatter.format(subtotal));
        
        if (shippingFee == 0) {
            tvShippingFee.setText("Miễn phí");
        } else {
            tvShippingFee.setText(formatter.format(shippingFee));
        }
        
        tvVoucherDiscount.setText("-" + formatter.format(voucherDiscount));
        tvTotalAmount.setText(formatter.format(total));
    }
}
