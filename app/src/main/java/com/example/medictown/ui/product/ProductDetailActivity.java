package com.example.medictown.ui.product;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.bumptech.glide.Glide;
import com.example.medictown.R;
import com.example.medictown.data.models.Products;
import com.example.medictown.databinding.ActivityProductDetailBinding;
import java.text.NumberFormat;
import java.util.Locale;

public class ProductDetailActivity extends AppCompatActivity {
    private ActivityProductDetailBinding binding;
    private Products product;

    // Thêm ViewModel của tôi vào đây
    private ProductDetailViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProductDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Khởi tạo ViewModel
        viewModel = new ViewModelProvider(this).get(ProductDetailViewModel.class);

        // Nhận dữ liệu product từ Intent
        product = (Products) getIntent().getSerializableExtra("product");

        if (product != null) {
            displayProductDetails();
        }

        setupButtons();

        // Lắng nghe kết quả từ ViewModel
        observeViewModel();
    }

    // GIỮ NGUYÊN 100% CODE GIAO DIỆN CỦA BRO Ở ĐÂY
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

        // CẬP NHẬT LOGIC NÚT ADD TO CART
        binding.btnAddToCartDetail.setOnClickListener(v -> {
            if (product != null && product.id != null) {
                // Gọi ViewModel để đẩy lên Supabase
                viewModel.addProductToCart(product.id);
            }
        });

        binding.btnBuyNowDetail.setOnClickListener(v -> {
            // Logic mua ngay (tạm thời để trống hoặc làm sau)
        });
    }

    // THÊM HÀM LẮNG NGHE KẾT QUẢ TỪ DB BÁO VỀ
    private void observeViewModel() {
        viewModel.getIsAddToCartSuccessful().observe(this, isSuccess -> {
            if (isSuccess) {
                Toast.makeText(this, "Đã thêm vào giỏ hàng thành công!", Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getErrorMessage().observe(this, error -> {
            if (error != null) {
                Toast.makeText(this, "Lỗi: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }
}