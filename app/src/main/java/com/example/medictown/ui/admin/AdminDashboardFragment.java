package com.example.medictown.ui.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.medictown.R;
import com.example.medictown.data.models.Orders;
import com.example.medictown.data.models.Products;
import com.google.android.material.card.MaterialCardView;

import java.util.List;
import java.util.Locale;

public class AdminDashboardFragment extends Fragment {

    private AdminViewModel viewModel;
    private TextView tvTotalRevenue, tvTotalOrders, tvLowStock, tvNewCustomers;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(AdminViewModel.class);

        tvTotalRevenue = view.findViewById(R.id.tvTotalRevenue);
        tvTotalOrders = view.findViewById(R.id.tvTotalOrders);
        tvLowStock = view.findViewById(R.id.tvLowStock);
        tvNewCustomers = view.findViewById(R.id.tvNewCustomers);

        MaterialCardView btnManageInventory = view.findViewById(R.id.btnManageInventory);
        MaterialCardView btnManageOrders = view.findViewById(R.id.btnManageOrders);

        viewModel.getAllOrders().observe(getViewLifecycleOwner(), this::updateOrderStats);
        viewModel.getAllProducts().observe(getViewLifecycleOwner(), this::updateProductStats);

        viewModel.fetchAllOrders();
        viewModel.fetchAllProducts();

        btnManageInventory.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new AdminInventoryFragment())
                    .addToBackStack(null)
                    .commit();
        });

        btnManageOrders.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new AdminOrdersFragment())
                    .addToBackStack(null)
                    .commit();
        });
    }

    private void updateOrderStats(List<Orders> orders) {
        if (orders == null) return;
        double revenue = orders.stream()
                .filter(o -> "completed".equalsIgnoreCase(o.status))
                .mapToDouble(o -> o.total_amount)
                .sum();
        tvTotalRevenue.setText(String.format(Locale.getDefault(), "$%.0f", revenue));
        tvTotalOrders.setText(String.valueOf(orders.size()));
    }

    private void updateProductStats(List<Products> products) {
        if (products == null) return;
        long lowStock = products.stream()
                .filter(p -> p.stock < 10)
                .count();
        tvLowStock.setText(String.valueOf(lowStock));
    }
}
