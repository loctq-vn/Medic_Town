package com.example.medictown.ui.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.lifecycle.ViewModelProvider;
import com.example.medictown.data.api.SessionManager;
import com.example.medictown.data.models.Orders;
import com.example.medictown.ui.admin.AdminOrdersAdapter;
import com.example.medictown.ui.admin.AdminViewModel;
import com.google.android.material.tabs.TabLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import com.example.medictown.R;

public class AdminOrdersFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_orders, container, false);
    }

    private AdminViewModel viewModel;
    private AdminOrdersAdapter adapter;
    private List<Orders> allOrdersList = new ArrayList<>();
    private String currentShopId;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(AdminViewModel.class);
        currentShopId = new SessionManager(requireContext()).getCurrentShopId();
        adapter = new AdminOrdersAdapter();

        RecyclerView rvOrders = view.findViewById(R.id.rvOrders);
        rvOrders.setLayoutManager(new LinearLayoutManager(getContext()));
        rvOrders.setAdapter(adapter);

        TabLayout tabLayout = view.findViewById(R.id.tabLayout);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                filterOrders(tab.getText().toString());
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });

        adapter.setOnOrderActionListener(new AdminOrdersAdapter.OnOrderActionListener() {
            @Override
            public void onQuickAction(Orders order) {
                if ("shipping".equalsIgnoreCase(order.status)) {
                    onDetails(order);
                    return;
                }

                String nextStatus = "";
                if ("pending".equalsIgnoreCase(order.status)) nextStatus = "confirmed";
                else if ("confirmed".equalsIgnoreCase(order.status)) nextStatus = "shipping";
                
                if (!nextStatus.isEmpty()) {
                    if (currentShopId != null && !currentShopId.isEmpty()) {
                        viewModel.updateOrderStatus(currentShopId, order.id, nextStatus);
                    }
                }
            }

            @Override
            public void onDetails(Orders order) {
                requireActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, AdminOrderDetailFragment.newInstance(order, currentShopId))
                        .addToBackStack(null)
                        .commit();
            }

            @Override
            public void onCancel(Orders order) {
                if (currentShopId != null && !currentShopId.isEmpty()) {
                    viewModel.updateOrderStatus(currentShopId, order.id, "cancelled");
                }
            }
        });

        viewModel.getAllOrders().observe(getViewLifecycleOwner(), orders -> {
            allOrdersList = orders;
            filterOrders(tabLayout.getTabAt(tabLayout.getSelectedTabPosition()).getText().toString());
        });

        if (currentShopId != null && !currentShopId.isEmpty()) {
            viewModel.fetchShopOrders(currentShopId);
        } else {
            viewModel.fetchAllOrders();
        }
    }

    private void filterOrders(String status) {
        List<Orders> filtered;
        if ("Tất cả".equalsIgnoreCase(status) || "All".equalsIgnoreCase(status)) {
            filtered = allOrdersList;
        } else {
            String mappedStatus = mapStatus(status);
            filtered = allOrdersList.stream()
                    .filter(o -> mappedStatus.equalsIgnoreCase(o.status))
                    .collect(Collectors.toList());
        }
        adapter.setOrders(filtered);
        updateOrderCount(filtered.size());
    }

    private String mapStatus(String tabText) {
        switch (tabText) {
            case "Chờ xác nhận": return "pending";
            case "Đã xác nhận": return "confirmed";
            case "Đang giao": return "shipping";
            case "Hoàn thành": return "completed";
            case "Đã hủy": return "cancelled";
            default: return tabText.toLowerCase();
        }
    }

    private void updateOrderCount(int count) {
        View view = getView();
        if (view != null) {
            android.widget.TextView tvOrderCount = view.findViewById(R.id.tvOrderCount);
            if (tvOrderCount != null) {
                tvOrderCount.setText("Tổng cộng " + count + " đơn hàng");
            }
        }
    }
}
