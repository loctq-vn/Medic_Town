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
import com.example.medictown.ui.admin.AdminInventoryAdapter;
import com.example.medictown.ui.admin.AdminViewModel;
import com.example.medictown.ui.shop.SellerProductFormFragment;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.example.medictown.R;

public class AdminInventoryFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_inventory, container, false);
    }

    private AdminViewModel viewModel;
    private AdminInventoryAdapter adapter;
    private String currentShopId;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        viewModel = new ViewModelProvider(this).get(AdminViewModel.class);
        adapter = new AdminInventoryAdapter();
        adapter.setOnProductActionListener(product -> {
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, SellerProductFormFragment.newInstance(product))
                    .addToBackStack(null)
                    .commit();
        });
        
        RecyclerView rvInventory = view.findViewById(R.id.rvInventory);
        rvInventory.setLayoutManager(new LinearLayoutManager(getContext()));
        rvInventory.setAdapter(adapter);

        ChipGroup chipGroup = view.findViewById(R.id.chipGroupFilters);
        chipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            // TODO: Implement local filtering logic
        });

        ExtendedFloatingActionButton fabAddProduct = view.findViewById(R.id.fabAddProduct);
        fabAddProduct.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new SellerProductFormFragment())
                    .addToBackStack(null)
                    .commit();
        });

        viewModel.getAllProducts().observe(getViewLifecycleOwner(), products -> {
            adapter.setProducts(products);
        });

        SessionManager sessionManager = new SessionManager(requireContext());
        currentShopId = sessionManager.getCurrentShopId();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (viewModel != null) {
            loadProducts();
        }
    }

    private void loadProducts() {
        if (currentShopId != null && !currentShopId.isEmpty()) {
            viewModel.fetchShopProducts(currentShopId);
        } else {
            viewModel.fetchAllProducts();
        }
    }
}
