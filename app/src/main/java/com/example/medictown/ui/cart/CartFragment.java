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
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CartFragment extends Fragment {

    private CartViewModel mViewModel;
    private SessionManager sessionManager;
    private CartAdapter adapter;

    private RecyclerView rvCartItems;
    private TextView tvTotalAmount, tvSubtotalLabel, tvSubtotalValue, tvClearAll;
    private Button btnCheckout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_cart, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sessionManager = new SessionManager(requireContext());
        mViewModel = new ViewModelProvider(this).get(CartViewModel.class);

        initViews(view);
        setupRecyclerView();
        observeViewModel();

        loadCartData();
    }

    private void initViews(View view) {
        rvCartItems = view.findViewById(R.id.rvCartItems);
        tvTotalAmount = view.findViewById(R.id.tvTotalAmount);
        tvSubtotalLabel = view.findViewById(R.id.tvSubtotalLabel);
        tvSubtotalValue = view.findViewById(R.id.tvSubtotalValue);
        tvClearAll = view.findViewById(R.id.tvClearAll);
        btnCheckout = view.findViewById(R.id.btnCheckout);

        btnCheckout.setOnClickListener(v -> {
            List<CartItem> allItems = mViewModel.cartItems.getValue();
            List<CartItem> selectedItems = new ArrayList<>();
            if (allItems != null) {
                for (CartItem item : allItems) {
                    if (item.isSelected) {
                        selectedItems.add(item);
                    }
                }
            }

            if (selectedItems.isEmpty()) {
                Toast.makeText(getContext(), "Vui lòng chọn sản phẩm để thanh toán", Toast.LENGTH_SHORT).show();
                return;
            }

            if (getActivity() != null) {
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, com.example.medictown.ui.payment.PaymentFragment.newInstance(selectedItems))
                        .addToBackStack(null)
                        .commit();
            }
        });

        tvClearAll.setOnClickListener(v -> {
            if (sessionManager.isLoggedIn()) {
                mViewModel.clearCart(sessionManager.getUserId(), sessionManager.getToken());
            }
        });
    }

    private void setupRecyclerView() {
        adapter = new CartAdapter();
        rvCartItems.setLayoutManager(new LinearLayoutManager(getContext()));
        rvCartItems.setAdapter(adapter);

        // Xử lý các sự kiện tương tác trong giỏ hàng
        adapter.setOnCartItemInteractionListener(new CartAdapter.OnCartItemInteractionListener() {
            @Override
            public void onIncreaseQuantity(CartItem item) {
                mViewModel.updateQuantity(item.id, item.quantity + 1, sessionManager.getToken(), sessionManager.getUserId());
            }

            @Override
            public void onDecreaseQuantity(CartItem item) {
                if (item.quantity > 1) {
                    mViewModel.updateQuantity(item.id, item.quantity - 1, sessionManager.getToken(), sessionManager.getUserId());
                }
            }

            @Override
            public void onDeleteItem(CartItem item) {
                mViewModel.removeFromCart(item.id, sessionManager.getToken(), sessionManager.getUserId());
            }

            @Override
            public void onToggleSelection(CartItem item) {
                updateTotals(mViewModel.cartItems.getValue());
            }
        });
    }

    private void observeViewModel() {
        // Theo dõi danh sách giỏ hàng
        mViewModel.cartItems.observe(getViewLifecycleOwner(), items -> {
            if (items != null) {
                adapter.setCartItems(items);
                updateTotals(items);
            } else {
                adapter.setCartItems(new ArrayList<>());
                updateTotals(new ArrayList<>());
            }
        });
    }

    private void loadCartData() {
        if (sessionManager.isLoggedIn()) {
            mViewModel.fetchCartItems(sessionManager.getUserId(), sessionManager.getToken());
        } else {
            Toast.makeText(getContext(), "Vui lòng đăng nhập để xem giỏ hàng", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateTotals(List<CartItem> items) {
        double total = 0;
        int count = 0;
        if (items != null) {
            for (CartItem item : items) {
                if (item.isSelected && item.products != null) {
                    double price = (item.products.sale_price != null && item.products.sale_price > 0)
                            ? item.products.sale_price : item.products.price;
                    total += price * item.quantity;
                    count++;
                }
            }
        }

        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        String formattedTotal = formatter.format(total);
        
        tvTotalAmount.setText(formattedTotal);
        tvSubtotalValue.setText(formattedTotal);
        tvSubtotalLabel.setText("Tạm tính (" + count + " sản phẩm)");
    }
}
