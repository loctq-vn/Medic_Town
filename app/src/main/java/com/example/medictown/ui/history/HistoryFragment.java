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
        
        viewModel = new ViewModelProvider(this).get(HistoryViewModel.class);
        sessionManager = new SessionManager(requireContext());
        
        setupRecyclerView();
        observeViewModel();
        
        if (sessionManager.isLoggedIn()) {
            viewModel.fetchOrders(sessionManager.getUserId());
        } else {
            Toast.makeText(getContext(), "Vui lòng đăng nhập để xem lịch sử", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupRecyclerView() {
        adapter = new OrderHistoryAdapter();
        binding.rvOrderHistory.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvOrderHistory.setAdapter(adapter);
    }

    private void observeViewModel() {
        viewModel.orders.observe(getViewLifecycleOwner(), orders -> {
            adapter.setOrders(orders);
        });

        viewModel.isLoading.observe(getViewLifecycleOwner(), isLoading -> {
            // Có thể thêm ProgressBar nếu cần
        });

        viewModel.errorMessage.observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
