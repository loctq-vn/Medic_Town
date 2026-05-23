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

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(AdminViewModel.class);
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
                String nextStatus = "";
                if ("pending".equalsIgnoreCase(order.status)) nextStatus = "confirmed";
                else if ("confirmed".equalsIgnoreCase(order.status)) nextStatus = "shipping";
                
                if (!nextStatus.isEmpty()) {
                    viewModel.updateOrderStatus(order.id, nextStatus);
                }
            }

            @Override
            public void onDetails(Orders order) {
                // Navigate to existing order detail if applicable or admin specific detail
            }
        });

        viewModel.getAllOrders().observe(getViewLifecycleOwner(), orders -> {
            allOrdersList = orders;
            filterOrders(tabLayout.getTabAt(tabLayout.getSelectedTabPosition()).getText().toString());
        });

        viewModel.fetchAllOrders();
    }

    private void filterOrders(String status) {
        if ("All".equalsIgnoreCase(status)) {
            adapter.setOrders(allOrdersList);
        } else {
            List<Orders> filtered = allOrdersList.stream()
                    .filter(o -> status.equalsIgnoreCase(o.status))
                    .collect(Collectors.toList());
            adapter.setOrders(filtered);
        }
    }
}
