package com.example.medictown.ui.product;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.medictown.MainActivity;
import com.example.medictown.R;
import com.example.medictown.data.api.SessionManager;
import com.example.medictown.data.models.Products;
import com.example.medictown.databinding.ActivityProductDetailBinding;
import com.example.medictown.ui.cart.CartViewModel;

import java.text.NumberFormat;
import java.util.Locale;

public class ProductDetailActivity extends AppCompatActivity {
    private ActivityProductDetailBinding binding;
    private Products product;
    private CartViewModel cartViewModel;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProductDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sessionManager = new SessionManager(this);
        cartViewModel = new ViewModelProvider(this).get(CartViewModel.class);

        // Nhận dữ liệu product từ Intent
        product = (Products) getIntent().getSerializableExtra("product");

        if (product != null) {
            displayProductDetails();
        }

        setupButtons();
        observeViewModel();
    }

    private void observeViewModel() {
        cartViewModel.addToCartStatus.observe(this, status -> {
            Toast.makeText(this, status, Toast.LENGTH_SHORT).show();
        });
    }

    private void displayProductDetails() {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        binding.tvProductNameDetail.setText(product.name);
        binding.tvBrandDetail.setText(product.brand);
        binding.tvDescriptionDetail.setText(product.description);
        binding.tvIndicationsDetail.setText(product.indications != null ? product.indications : "Chưa có thông tin");
        binding.tvUsageDetail.setText(product.usage != null ? product.usage : "Chưa có thông tin");
        binding.tvContraindicationsDetail.setText(product.contraindications != null ? product.contraindications : "Chưa có thông tin");
        
        if (product.sale_price != null && product.sale_price > 0 && product.sale_price < product.price) {
            binding.tvOldPriceDetail.setVisibility(View.VISIBLE);
            binding.tvOldPriceDetail.setText(formatter.format(product.price));
            binding.tvPriceDetail.setText(formatter.format(product.sale_price));
            binding.btnBuyNowDetail.setText("Mua ngay - " + formatter.format(product.sale_price));
            
            // Tính phần trăm giảm giá
            double discountPercent = ((product.price - product.sale_price) / product.price) * 100;
            binding.tvDiscountDetail.setVisibility(View.VISIBLE);
            binding.tvDiscountDetail.setText("-" + Math.round(discountPercent) + "%");
        } else {
            binding.tvOldPriceDetail.setVisibility(View.GONE);
            binding.tvDiscountDetail.setVisibility(View.GONE);
            binding.tvPriceDetail.setText(formatter.format(product.price));
            binding.btnBuyNowDetail.setText("Mua ngay - " + formatter.format(product.price));
        }

        // Load image
        if (product.images != null && !product.images.isEmpty()) {
            Glide.with(this)
                    .load(product.images.get(0))
                    .placeholder(R.drawable.ic_launcher_background)
                    .into(binding.imgProductDetail);
        }
    }

    private void setupButtons() {
        binding.toolbar.setNavigationOnClickListener(v -> {
            finish();
        });
        
        binding.btnAddToCartDetail.setOnClickListener(v -> {
            if (sessionManager.isLoggedIn()) {
                String userId = sessionManager.getUserId();
                String token = sessionManager.getToken();
                cartViewModel.addToCart(userId, product.id, 1, token);
            } else {
                Toast.makeText(this, "Vui lòng đăng nhập để thêm vào giỏ hàng", Toast.LENGTH_SHORT).show();
            }
        });

        binding.btnCart.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("open_cart", true);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        });

        binding.btnBuyNowDetail.setOnClickListener(v -> {
            // Logic mua ngay
        });
    }
}
