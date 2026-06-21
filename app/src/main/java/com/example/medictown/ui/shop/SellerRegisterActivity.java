package com.example.medictown.ui.shop;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
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
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (v instanceof EditText) {
                Rect outRect = new Rect();
                v.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int) event.getRawX(), (int) event.getRawY())) {
                    v.clearFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (imm != null) {
                        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    }
                }
            }
        }
        return super.dispatchTouchEvent(event);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySellerRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        repository = new ShopRepository();
        sessionManager = new SessionManager(this);

        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnCreateShop.setOnClickListener(v -> createShop());
        setupFormFocusClearing();
    }

    private void setupFormFocusClearing() {
        clearTextFocusWhenTouchingNonInput(binding.getRoot());
    }

    private void clearTextFocusWhenTouchingNonInput(View view) {
        if (view == null) return;
        if (!(view instanceof EditText)) {
            view.setOnTouchListener((touchedView, event) -> {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    clearFormFocus();
                }
                return false;
            });
        }

        if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                clearTextFocusWhenTouchingNonInput(viewGroup.getChildAt(i));
            }
        }
    }

    private void clearFormFocus() {
        if (binding == null) return;
        binding.etShopName.clearFocus();
        binding.etShopDescription.clearFocus();
        binding.etShopAddress.clearFocus();

        View focusedView = getCurrentFocus();
        if (focusedView != null) {
            InputMethodManager inputMethodManager =
                    (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(focusedView.getWindowToken(), 0);
        }
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
