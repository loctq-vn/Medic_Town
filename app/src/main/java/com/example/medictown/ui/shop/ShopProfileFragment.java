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
    private Shop currentShop;

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

        binding.btnManageProducts.setOnClickListener(v ->
                requireActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new com.example.medictown.ui.admin.AdminInventoryFragment())
                        .addToBackStack(null)
                        .commit()
        );
        binding.btnManageOrders.setOnClickListener(v ->
                requireActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new com.example.medictown.ui.admin.AdminOrdersFragment())
                        .addToBackStack(null)
                        .commit()
        );
        binding.btnManageAds.setOnClickListener(v ->
                requireActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new AdManagementFragment())
                        .addToBackStack(null)
                        .commit()
        );
        binding.btnSaveShop.setOnClickListener(v -> saveShop());
        binding.btnBuyerChannel.setOnClickListener(v -> {
            sessionManager.clearCurrentShop();
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).openBuyerChannel();
            }
        });

        loadShop();
    }

    private void loadShop() {
        String shopId = sessionManager.getCurrentShopId();
        if (shopId == null || shopId.isEmpty()) {
            Toast.makeText(getContext(), "Chưa chọn gian hàng", Toast.LENGTH_SHORT).show();
            return;
        }

        binding.progressBar.setVisibility(View.VISIBLE);
        repository.getShop(shopId, new Callback<Shop>() {
            @Override
            public void onResponse(Call<Shop> call, Response<Shop> response) {
                if (binding == null) return;
                binding.progressBar.setVisibility(View.GONE);
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
                Toast.makeText(getContext(), t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void bindShop(Shop shop) {
        currentShop = shop;
        binding.etShopName.setText(shop.name);
        binding.etShopDescription.setText(shop.description);
        binding.etShopAddress.setText(shop.address);
        binding.etShopLogo.setText(shop.logo_url);
        Glide.with(this)
                .load(shop.logo_url)
                .placeholder(R.drawable.ic_profile)
                .circleCrop()
                .into(binding.ivShopLogo);
    }

    private void saveShop() {
        if (currentShop == null) return;

        Shop update = new Shop();
        update.name = binding.etShopName.getText().toString().trim();
        update.description = binding.etShopDescription.getText().toString().trim();
        update.address = binding.etShopAddress.getText().toString().trim();
        update.logo_url = binding.etShopLogo.getText().toString().trim();

        if (update.name.isEmpty()) {
            binding.etShopName.setError("Vui lòng nhập tên gian hàng");
            return;
        }

        binding.progressBar.setVisibility(View.VISIBLE);
        binding.btnSaveShop.setEnabled(false);
        repository.updateShop(currentShop.id, update, new Callback<Shop>() {
            @Override
            public void onResponse(Call<Shop> call, Response<Shop> response) {
                if (binding == null) return;
                binding.progressBar.setVisibility(View.GONE);
                binding.btnSaveShop.setEnabled(true);
                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(getContext(), "Không thể cập nhật gian hàng", Toast.LENGTH_SHORT).show();
                    return;
                }
                Shop shop = response.body();
                sessionManager.saveCurrentShop(shop.id, shop.name, shop.logo_url);
                bindShop(shop);
                Toast.makeText(getContext(), "Đã lưu thông tin gian hàng", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Call<Shop> call, Throwable t) {
                if (binding == null) return;
                binding.progressBar.setVisibility(View.GONE);
                binding.btnSaveShop.setEnabled(true);
                Toast.makeText(getContext(), t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
