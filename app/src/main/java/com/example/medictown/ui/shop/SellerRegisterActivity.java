package com.example.medictown.ui.shop;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.medictown.MainActivity;
import com.example.medictown.data.api.SessionManager;
import com.example.medictown.data.models.Shop;
import com.example.medictown.data.repositories.ShopRepository;
import com.example.medictown.databinding.ActivitySellerRegisterBinding;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SellerRegisterActivity extends AppCompatActivity {
    private ActivitySellerRegisterBinding binding;
    private ShopRepository repository;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySellerRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        repository = new ShopRepository();
        sessionManager = new SessionManager(this);

        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnCreateShop.setOnClickListener(v -> createShop());
    }

    private void createShop() {
        String name = binding.etShopName.getText().toString().trim();
        String description = binding.etShopDescription.getText().toString().trim();
        String address = binding.etShopAddress.getText().toString().trim();

        if (name.isEmpty()) {
            binding.etShopName.setError("Vui lòng nhập tên gian hàng");
            return;
        }

        binding.progressBar.setVisibility(View.VISIBLE);
        binding.btnCreateShop.setEnabled(false);
        repository.createShop(new Shop(name, description, address), new Callback<Shop>() {
            @Override
            public void onResponse(Call<Shop> call, Response<Shop> response) {
                binding.progressBar.setVisibility(View.GONE);
                binding.btnCreateShop.setEnabled(true);
                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(SellerRegisterActivity.this, "Không thể tạo gian hàng", Toast.LENGTH_SHORT).show();
                    return;
                }
                Shop shop = response.body();
                sessionManager.saveCurrentShop(shop.id, shop.name, shop.logo_url);
                Intent intent = new Intent(SellerRegisterActivity.this, MainActivity.class);
                intent.putExtra("open_seller_dashboard", true);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
            }

            @Override
            public void onFailure(Call<Shop> call, Throwable t) {
                binding.progressBar.setVisibility(View.GONE);
                binding.btnCreateShop.setEnabled(true);
                Toast.makeText(SellerRegisterActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
