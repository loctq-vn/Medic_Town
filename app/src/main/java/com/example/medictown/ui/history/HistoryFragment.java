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
import com.example.medictown.databinding.FragmentHistoryBinding;

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
        adapter.setOnOrderClickListener(order -> {
            OrderDetailFragment detailFragment = OrderDetailFragment.newInstance(order.id);
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, detailFragment)
                    .addToBackStack(null)
                    .commit();
        });
        binding.rvOrderHistory.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvOrderHistory.setAdapter(adapter);
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
