package com.example.medictown.ui.chat;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.medictown.data.api.RetrofitClient;
import com.example.medictown.data.api.SessionManager;
import com.example.medictown.data.models.SellerConversationItem;
import com.example.medictown.databinding.FragmentSellerConversationsBinding;

import java.util.Collections;
import java.util.List;

public class SellerConversationFragment extends Fragment {
    private FragmentSellerConversationsBinding binding;
    private SellerChatViewModel viewModel;
    private SellerConversationAdapter adapter;
    private SessionManager sessionManager;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        binding = FragmentSellerConversationsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState
    ) {
        super.onViewCreated(view, savedInstanceState);
        sessionManager = new SessionManager(requireContext());
        viewModel = new ViewModelProvider(this).get(SellerChatViewModel.class);
        adapter = new SellerConversationAdapter(this::openConversation);

        binding.rvConversations.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvConversations.setAdapter(adapter);
        binding.btnRetry.setOnClickListener(retryView -> viewModel.loadConversations());

        observeViewModel();
        viewModel.initialize(sessionManager.getToken());
    }

    @Override
    public void onStart() {
        super.onStart();
        if (viewModel != null) {
            viewModel.connectRealtime();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (viewModel != null) {
            viewModel.loadConversations();
        }
    }

    @Override
    public void onStop() {
        if (viewModel != null) {
            viewModel.disconnectRealtime();
        }
        super.onStop();
    }

    private void observeViewModel() {
        viewModel.getConversations().observe(getViewLifecycleOwner(), conversations -> {
            List<SellerConversationItem> values = conversations == null
                    ? Collections.emptyList()
                    : conversations;
            adapter.submitList(values);
            binding.emptyState.setVisibility(values.isEmpty() ? View.VISIBLE : View.GONE);
            binding.rvConversations.setVisibility(values.isEmpty() ? View.GONE : View.VISIBLE);
        });

        viewModel.getLoading().observe(getViewLifecycleOwner(), isLoading ->
                binding.progressLoading.setVisibility(
                        Boolean.TRUE.equals(isLoading) ? View.VISIBLE : View.GONE
                )
        );

        viewModel.getConnected().observe(getViewLifecycleOwner(), isConnected -> {
            boolean connected = Boolean.TRUE.equals(isConnected);
            binding.tvConnectionStatus.setText(
                    connected ? "Đang trực tuyến" : "Đang kết nối..."
            );
            binding.tvConnectionStatus.setTextColor(
                    requireContext().getColor(
                            connected
                                    ? android.R.color.holo_green_dark
                                    : com.example.medictown.R.color.admin_text_secondary
                    )
            );
        });

        viewModel.getError().observe(getViewLifecycleOwner(), error -> {
            boolean hasError = error != null && !error.trim().isEmpty();
            binding.errorState.setVisibility(hasError ? View.VISIBLE : View.GONE);
            if (hasError) {
                binding.tvError.setText(error);
                binding.emptyState.setVisibility(View.GONE);
            }
        });
    }

    private void openConversation(SellerConversationItem item) {
        if (item == null || item.conversation == null) {
            return;
        }
        Intent intent = new Intent(requireContext(), ChatActivity.class);
        intent.putExtra(ChatActivity.EXTRA_SELLER_MODE, true);
        intent.putExtra(
                ChatActivity.EXTRA_CONVERSATION_JSON,
                RetrofitClient.getGson().toJson(item.conversation)
        );
        intent.putExtra(
                ChatActivity.EXTRA_CUSTOMER_NAME,
                item.customer != null ? item.customer.name : "Khách hàng"
        );
        startActivity(intent);
    }

    @Override
    public void onDestroyView() {
        binding = null;
        super.onDestroyView();
    }
}
