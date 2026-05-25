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

import androidx.recyclerview.widget.RecyclerView;

import com.example.medictown.R;
import com.example.medictown.data.api.SessionManager;
import com.example.medictown.data.models.Orders;
import com.example.medictown.data.models.Products;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class AdminDashboardFragment extends Fragment {

    private AdminViewModel viewModel;
    private TextView tvDashboardTitle, tvTotalRevenue, tvTotalOrders, tvLowStock, tvNewCustomers;
    private RecyclerView rvRecentOrders;
    private RecentOrdersAdapter recentOrdersAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(AdminViewModel.class);

        tvDashboardTitle = view.findViewById(R.id.tvDashboardTitle);
        tvTotalRevenue = view.findViewById(R.id.tvTotalRevenue);
        tvTotalOrders = view.findViewById(R.id.tvTotalOrders);
        tvLowStock = view.findViewById(R.id.tvLowStock);
        tvNewCustomers = view.findViewById(R.id.tvNewCustomers);
        rvRecentOrders = view.findViewById(R.id.rvRecentOrders);

        recentOrdersAdapter = new RecentOrdersAdapter();
        rvRecentOrders.setAdapter(recentOrdersAdapter);

        SessionManager sessionManager = new SessionManager(requireContext());
        String currentShopName = sessionManager.getCurrentShopName();
        if (currentShopName != null && !currentShopName.isEmpty()) {
            tvDashboardTitle.setText(currentShopName);
        }

        MaterialCardView btnManageInventory = view.findViewById(R.id.btnManageInventory);
        MaterialCardView btnManageOrders = view.findViewById(R.id.btnManageOrders);
        MaterialButton btnViewAllOrders = view.findViewById(R.id.btnViewAllOrders);
        MaterialButton btnQuickAddProduct = view.findViewById(R.id.btnQuickAddProduct);

        viewModel.getAllOrders().observe(getViewLifecycleOwner(), this::updateOrderStats);
        viewModel.getAllProducts().observe(getViewLifecycleOwner(), this::updateProductStats);

        String currentShopId = sessionManager.getCurrentShopId();
        if (currentShopId != null && !currentShopId.isEmpty()) {
            viewModel.fetchShopOrders(currentShopId);
            viewModel.fetchShopProducts(currentShopId);
        } else {
            viewModel.fetchAllOrders();
            viewModel.fetchAllProducts();
        }

        btnManageInventory.setOnClickListener(v -> navigateTo(new AdminInventoryFragment()));
        btnManageOrders.setOnClickListener(v -> navigateTo(new AdminOrdersFragment()));
        btnViewAllOrders.setOnClickListener(v -> navigateTo(new AdminOrdersFragment()));
        
        btnQuickAddProduct.setOnClickListener(v -> {
            // Implementation for quick add product
        });
    }

    private void navigateTo(Fragment fragment) {
        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }

    private void updateOrderStats(List<Orders> orders) {
        if (orders == null) return;
        
        // Update recent orders list (sort by date if available, assuming list is already sorted or just taking latest)
        List<Orders> sortedOrders = orders;
        Collections.reverse(sortedOrders); // Assuming newest at bottom, reverse to get newest at top
        recentOrdersAdapter.setOrders(sortedOrders);

        double revenue = orders.stream()
                .filter(o -> "completed".equalsIgnoreCase(o.status))
                .mapToDouble(o -> o.total_amount)
                .sum();
        tvTotalRevenue.setText(String.format(Locale.getDefault(), "%,.0fđ", revenue));
        tvTotalOrders.setText(String.valueOf(orders.size()));
    }

    private void updateProductStats(List<Products> products) {
        if (products == null) return;
        long lowStock = products.stream()
                .filter(p -> p.stock < 10)
                .count();
        tvLowStock.setText(String.valueOf(lowStock));

        // Update Inventory Overview Section (Mocking for now with first 2 products)
        View view = getView();
        if (view != null && products.size() >= 2) {
            // This is a bit brittle as it relies on the specific layout structure
            // but for a quick refactor it works.
            updateInventoryItem(view, 0, products.get(0));
            updateInventoryItem(view, 1, products.get(1));
        }
    }

    private void updateInventoryItem(View parent, int index, Products product) {
        // Find the GridLayout
        android.widget.GridLayout grid = parent.findViewById(R.id.inventory_grid);
        if (grid != null && index < grid.getChildCount()) {
            View card = grid.getChildAt(index);
            TextView tvName = card.findViewById(R.id.tvInventoryProductName);
            TextView tvCount = card.findViewById(R.id.tvInventoryStockCount);
            View indicator = card.findViewById(R.id.inventory_indicator);

            if (tvName != null) tvName.setText(product.name.toUpperCase());
            if (tvCount != null) tvCount.setText(product.stock + " đv");
            if (indicator != null) {
                indicator.getBackground().setTint(product.stock < 10 ? 0xFFA33500 : 0xFF003D9B);
            }
        }
    }
}
