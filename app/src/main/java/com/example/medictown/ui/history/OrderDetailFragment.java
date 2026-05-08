package com.example.medictown.ui.history;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.medictown.R;
import com.example.medictown.data.api.SessionManager;
import com.example.medictown.databinding.FragmentOrderDetailBinding;
import com.example.medictown.ui.cart.CartAdapter; // Reusing CartAdapter for product list if suitable
import java.text.SimpleDateFormat;
import java.util.Date;
import java.text.NumberFormat;
import java.util.Locale;

public class OrderDetailFragment extends Fragment {
    private FragmentOrderDetailBinding binding;
    private HistoryViewModel viewModel;
    private static final String ARG_ORDER_ID = "order_id";

    public static OrderDetailFragment newInstance(String orderId) {
        OrderDetailFragment fragment = new OrderDetailFragment();
        Bundle args = new Bundle();
        args.putString(ARG_ORDER_ID, orderId);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentOrderDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Ensure we get the ViewModel from the Activity scope to access the same data as HistoryFragment
        viewModel = new ViewModelProvider(requireActivity()).get(HistoryViewModel.class);

        String orderId = getArguments() != null ? getArguments().getString(ARG_ORDER_ID) : null;

        binding.btnBack.setOnClickListener(v -> getParentFragmentManager().popBackStack());

        if (orderId != null) {
            setupRecyclerView();
            observeOrderDetails(orderId);
            
            // If the orders list is empty, fetch them
            if (viewModel.orders.getValue() == null || viewModel.orders.getValue().isEmpty()) {
                SessionManager sessionManager = new SessionManager(requireContext());
                if (sessionManager.isLoggedIn()) {
                    viewModel.fetchOrders(sessionManager.getUserId());
                }
            }
        }
    }

    private void setupRecyclerView() {
        binding.rvOrderItems.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    private void observeOrderDetails(String orderId) {
        // Use allOrders to find the specific order regardless of the current status filter
        viewModel.allOrders.observe(getViewLifecycleOwner(), orders -> {
            if (orders == null) return;
            for (com.example.medictown.data.models.Orders o : orders) {
                if (o.id.equals(orderId)) {
                    bindOrderData(o);
                    return;
                }
            }
        });
    }

    private void bindOrderData(com.example.medictown.data.models.Orders order) {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        
        String displayId = order.id.length() > 8 ? order.id.substring(0, 8) : order.id;
        binding.tvOrderId.setText(getString(R.string.order_id_format, displayId));
        
        // Update Order Date
        if (order.created_at != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            binding.tvOrderDate.setText(sdf.format(order.created_at));
        }

        // Update Progress Bar based on status
        updateProgress(order.status);

        binding.tvTotalAmount.setText(formatter.format(order.total_amount));
        binding.tvSubtotal.setText(formatter.format(order.total_amount));
        
        binding.tvRecipientName.setText(order.shipping_name != null ? order.shipping_name : "N/A");
        binding.tvRecipientPhone.setText(order.shipping_phone != null ? order.shipping_phone : "N/A");
        binding.tvShippingAddress.setText(order.shipping_address != null ? order.shipping_address : "N/A");
        binding.tvPaymentMethod.setText(order.payment_method != null ? order.payment_method.toUpperCase() : "COD");

        // Setup product list
        OrderDetailProductAdapter adapter = new OrderDetailProductAdapter(order.order_items);
        binding.rvOrderItems.setAdapter(adapter);
    }

    private void updateProgress(String status) {
        int progress = 0;
        String statusText = "Chờ xác nhận";
        
        if (status == null) status = "pending";
        
        switch (status) {
            case "pending":
                progress = 20;
                statusText = "Chờ xác nhận";
                break;
            case "confirmed":
                progress = 50;
                statusText = "Đã xác nhận";
                break;
            case "shipping":
                progress = 80;
                statusText = "Đang giao hàng";
                break;
            case "completed":
                progress = 100;
                statusText = "Giao hàng thành công";
                break;
            case "cancelled":
                progress = 0;
                statusText = "Đã hủy";
                binding.orderProgressIndicator.setIndicatorColor(getResources().getColor(R.color.error, null));
                break;
        }
        
        binding.orderProgressIndicator.setProgress(progress);
        binding.tvProgressStatus.setText(statusText);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
