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
import com.example.medictown.ui.admin.AdminInventoryAdapter;
import com.example.medictown.ui.admin.AdminViewModel;
import com.google.android.material.chip.ChipGroup;
import com.example.medictown.R;

public class AdminInventoryFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_inventory, container, false);
    }

    private AdminViewModel viewModel;
    private AdminInventoryAdapter adapter;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        viewModel = new ViewModelProvider(this).get(AdminViewModel.class);
        adapter = new AdminInventoryAdapter();
        
        RecyclerView rvInventory = view.findViewById(R.id.rvInventory);
        rvInventory.setLayoutManager(new LinearLayoutManager(getContext()));
        rvInventory.setAdapter(adapter);

        ChipGroup chipGroup = view.findViewById(R.id.chipGroupFilters);
        chipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            // TODO: Implement local filtering logic
        });

        viewModel.getAllProducts().observe(getViewLifecycleOwner(), products -> {
            adapter.setProducts(products);
        });

        viewModel.fetchAllProducts();
    }
}
