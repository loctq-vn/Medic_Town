package com.example.medictown.ui.shop;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.medictown.MainActivity;
import com.example.medictown.R;
import com.example.medictown.data.api.SessionManager;
import com.example.medictown.data.models.Shop;
import com.example.medictown.data.repositories.ShopRepository;
import com.example.medictown.databinding.FragmentShopProfileBinding;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ShopProfileFragment extends Fragment {
    private FragmentShopProfileBinding binding;
    private ShopRepository repository;
    private SessionManager sessionManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentShopProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        repository = new ShopRepository();
        sessionManager = new SessionManager(requireContext());

        binding.btnManageProducts.setOnClickListener(v -> requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new com.example.medictown.ui.admin.AdminInventoryFragment())
                .addToBackStack(null)
                .commit());

        binding.btnManageOrders.setOnClickListener(v -> requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new com.example.medictown.ui.admin.AdminOrdersFragment())
                .addToBackStack(null)
                .commit());

        binding.btnManageAds.setOnClickListener(v -> requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new AdManagementFragment())
                .addToBackStack(null)
                .commit());

        binding.btnBuyerChannel.setOnClickListener(v -> {
            sessionManager.clearCurrentShop();
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).openBuyerChannel();
            }
        });

        setupSwipeRefresh();
        loadShop();
    }

    private void setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener(this::loadShop);
        binding.swipeRefresh.setColorSchemeResources(R.color.admin_primary);
    }

    private void loadShop() {
        String shopId = sessionManager.getCurrentShopId();
        if (shopId == null || shopId.isEmpty()) {
            if (binding != null) {
                binding.swipeRefresh.setRefreshing(false);
            }
            Toast.makeText(getContext(), "Chưa chọn gian hàng", Toast.LENGTH_SHORT).show();
            return;
        }

        if (binding != null && !binding.swipeRefresh.isRefreshing()) {
            binding.progressBar.setVisibility(View.VISIBLE);
        }
        repository.getShop(shopId, new Callback<Shop>() {
            @Override
            public void onResponse(Call<Shop> call, Response<Shop> response) {
                if (binding == null) return;
                binding.progressBar.setVisibility(View.GONE);
                binding.swipeRefresh.setRefreshing(false);
                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(getContext(), "Không thể tải thông tin gian hàng", Toast.LENGTH_SHORT).show();
                    return;
                }
                bindShop(response.body());
            }

            @Override
            public void onFailure(Call<Shop> call, Throwable t) {
                if (binding == null) return;
                binding.progressBar.setVisibility(View.GONE);
                binding.swipeRefresh.setRefreshing(false);
                Toast.makeText(getContext(), t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void bindShop(Shop shop) {
        binding.tvShopName.setText(shop.name);
        binding.tvShopDescription.setText(shop.description != null && !shop.description.isEmpty() 
            ? shop.description : "Chưa có mô tả");
        binding.tvShopAddress.setText(shop.address != null && !shop.address.isEmpty() 
            ? shop.address : "Chưa cập nhật địa chỉ");
        
        Glide.with(this)
                .load(shop.logo_url)
                .placeholder(R.drawable.ic_profile)
                .circleCrop()
                .into(binding.ivShopLogo);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
