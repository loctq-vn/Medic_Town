package com.example.medictown.ui.history;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.medictown.R;
import com.example.medictown.data.api.SessionManager;
import com.example.medictown.data.models.CartItem;
import com.example.medictown.data.models.OrderItem;
import com.example.medictown.data.models.Orders;
import com.example.medictown.data.models.Products;
import com.example.medictown.databinding.FragmentHistoryBinding;
import com.example.medictown.ui.payment.PaymentFragment;

import java.util.ArrayList;
import java.util.Collections;

public class HistoryFragment extends Fragment {
    private HistoryViewModel viewModel;
    private FragmentHistoryBinding binding;
    private OrderHistoryAdapter adapter;
    private SessionManager sessionManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHistoryBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Use requireActivity() to share ViewModel with OrderDetailFragment
        viewModel = new ViewModelProvider(requireActivity()).get(HistoryViewModel.class);
        sessionManager = new SessionManager(requireContext());
        
        setupRecyclerView();
        setupFilterButtons();
        observeViewModel();
        
        if (sessionManager.isLoggedIn()) {
            viewModel.fetchOrders(sessionManager.getUserId());
        } else {
            Toast.makeText(getContext(), "Vui lòng đăng nhập để xem lịch sử", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupFilterButtons() {
        binding.btnFilterAll.setOnClickListener(v -> viewModel.setFilter("all"));
        binding.btnFilterPending.setOnClickListener(v -> viewModel.setFilter("pending"));
        binding.btnFilterShipping.setOnClickListener(v -> viewModel.setFilter("shipping"));
        binding.btnFilterCompleted.setOnClickListener(v -> viewModel.setFilter("completed"));
        binding.btnFilterCancelled.setOnClickListener(v -> viewModel.setFilter("cancelled"));
    }

    private void setupRecyclerView() {
        adapter = new OrderHistoryAdapter();
        adapter.setOnOrderClickListener(new OrderHistoryAdapter.OnOrderClickListener() {
            @Override
            public void onDetailClick(Orders order) {
                OrderDetailFragment detailFragment = OrderDetailFragment.newInstance(order.id);
                requireActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, detailFragment)
                        .addToBackStack(null)
                        .commit();
            }

            @Override
            public void onReorderClick(Orders order) {
                openPaymentForReorder(order);
            }
        });
        binding.rvOrderHistory.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvOrderHistory.setAdapter(adapter);
    }

    private void openPaymentForReorder(Orders order) {
        if (order.order_items == null || order.order_items.isEmpty()) {
            Toast.makeText(getContext(), "Đơn hàng không có sản phẩm để mua lại", Toast.LENGTH_SHORT).show();
            return;
        }

        ArrayList<CartItem> paymentItems = new ArrayList<>();
        for (OrderItem orderItem : order.order_items) {
            if (orderItem.product_id == null || orderItem.product_id.trim().isEmpty()) {
                continue;
            }

            CartItem cartItem = new CartItem();
            cartItem.product_id = orderItem.product_id;
            cartItem.quantity = Math.max(orderItem.quantity, 1);
            cartItem.products = buildProductFromOrderItem(orderItem);
            paymentItems.add(cartItem);
        }

        if (paymentItems.isEmpty()) {
            Toast.makeText(getContext(), "Không thể mua lại đơn hàng này", Toast.LENGTH_SHORT).show();
            return;
        }

        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, PaymentFragment.newInstance(
                        paymentItems,
                        order.getPaymentMethod(),
                        order.note,
                        order.shipping_address
                ))
                .addToBackStack(null)
                .commit();
    }

    private Products buildProductFromOrderItem(OrderItem orderItem) {
        Products product = new Products();
        product.id = orderItem.product_id;
        product.name = orderItem.product_name != null ? orderItem.product_name : "Sản phẩm";
        product.price = orderItem.price;
        product.sale_price = null;
        product.unit = null;
        product.stock = Integer.MAX_VALUE;
        product.images = orderItem.product_image != null && !orderItem.product_image.trim().isEmpty()
                ? Collections.singletonList(orderItem.product_image)
                : new ArrayList<>();
        return product;
    }

    private void observeViewModel() {
        viewModel.orders.observe(getViewLifecycleOwner(), orders -> {
            adapter.setOrders(orders);
        });

        viewModel.currentFilter.observe(getViewLifecycleOwner(), this::updateFilterButtonsUI);

        viewModel.isLoading.observe(getViewLifecycleOwner(), isLoading -> {
            // Có thể thêm ProgressBar nếu cần
        });

        viewModel.errorMessage.observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateFilterButtonsUI(String activeFilter) {
        int activeBg = getResources().getColor(R.color.primary);
        int inactiveBg = getResources().getColor(R.color.surface_container_high);
        int activeText = getResources().getColor(R.color.white);
        int inactiveText = getResources().getColor(R.color.on_surface_variant);

        resetButton(binding.btnFilterAll, "all".equals(activeFilter), activeBg, inactiveBg, activeText, inactiveText);
        resetButton(binding.btnFilterPending, "pending".equals(activeFilter), activeBg, inactiveBg, activeText, inactiveText);
        resetButton(binding.btnFilterShipping, "shipping".equals(activeFilter), activeBg, inactiveBg, activeText, inactiveText);
        resetButton(binding.btnFilterCompleted, "completed".equals(activeFilter), activeBg, inactiveBg, activeText, inactiveText);
        resetButton(binding.btnFilterCancelled, "cancelled".equals(activeFilter), activeBg, inactiveBg, activeText, inactiveText);
    }

    private void resetButton(com.google.android.material.button.MaterialButton button, boolean isActive, int activeBg, int inactiveBg, int activeText, int inactiveText) {
        button.setBackgroundTintList(android.content.res.ColorStateList.valueOf(isActive ? activeBg : inactiveBg));
        button.setTextColor(isActive ? activeText : inactiveText);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
