package com.example.medictown.ui.shop;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.medictown.MainActivity;
import com.example.medictown.data.api.SessionManager;
import com.example.medictown.data.models.Shop;
import com.example.medictown.data.repositories.ShopRepository;
import com.example.medictown.databinding.ActivityShopSelectionBinding;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ShopSelectionActivity extends AppCompatActivity {
    private ActivityShopSelectionBinding binding;
    private ShopRepository repository;
    private SessionManager sessionManager;
    private ShopSelectionAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityShopSelectionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        repository = new ShopRepository();
        sessionManager = new SessionManager(this);
        adapter = new ShopSelectionAdapter(this::openShopDashboard);

        binding.rvShops.setLayoutManager(new GridLayoutManager(this, 2));
        binding.rvShops.setAdapter(adapter);
        binding.btnCreateShop.setOnClickListener(v -> startActivity(new Intent(this, SellerRegisterActivity.class)));

        loadShops();
    }

    private void loadShops() {
        binding.progressBar.setVisibility(View.VISIBLE);
        repository.getMyShops(new Callback<List<Shop>>() {
            @Override
            public void onResponse(Call<List<Shop>> call, Response<List<Shop>> response) {
                binding.progressBar.setVisibility(View.GONE);
                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(ShopSelectionActivity.this, "Không thể tải danh sách gian hàng", Toast.LENGTH_SHORT).show();
                    return;
                }
                List<Shop> shops = response.body();
                if (shops.isEmpty()) {
                    startActivity(new Intent(ShopSelectionActivity.this, SellerRegisterActivity.class));
                    finish();
                } else if (shops.size() == 1) {
                    openShopDashboard(shops.get(0));
                } else {
                    adapter.submitList(shops);
                }
            }

            @Override
            public void onFailure(Call<List<Shop>> call, Throwable t) {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(ShopSelectionActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openShopDashboard(Shop shop) {
        sessionManager.saveCurrentShop(shop.id, shop.name, shop.logo_url);
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("open_seller_dashboard", true);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }
}
