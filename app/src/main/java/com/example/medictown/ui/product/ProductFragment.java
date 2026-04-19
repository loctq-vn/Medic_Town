package com.example.medictown.ui.product;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import com.example.medictown.databinding.FragmentProductBinding;

public class ProductFragment extends Fragment {
    private FragmentProductBinding binding;
    private ProductViewModel viewModel;
    private ProductAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentProductBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(ProductViewModel.class);

        setupRecyclerView();
        observeViewModel();
    }

    private void setupRecyclerView() {
        adapter = new ProductAdapter();
        binding.rvProducts.setLayoutManager(new GridLayoutManager(getContext(), 2));
        binding.rvProducts.setAdapter(adapter);
    }

    private void observeViewModel() {
        viewModel.getFeaturedProducts().observe(getViewLifecycleOwner(), products -> {
            if (products != null) {
                adapter.setProductList(products);
            }
        });

        // Optional: show loading state
        // viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
        //     binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        // });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
