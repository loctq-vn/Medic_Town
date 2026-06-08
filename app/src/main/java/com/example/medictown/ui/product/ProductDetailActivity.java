package com.example.medictown.ui.product;

import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.medictown.MainActivity;
import com.example.medictown.R;
import com.example.medictown.data.api.SessionManager;
import com.example.medictown.data.models.CartItem;
import com.example.medictown.data.models.Products;
import com.example.medictown.databinding.ActivityProductDetailBinding;
import com.example.medictown.ui.cart.CartViewModel;
import com.example.medictown.ui.chat.ChatActivity;
import com.example.medictown.data.models.Reviews;
import com.example.medictown.data.repositories.ReviewRepository;
import androidx.recyclerview.widget.LinearLayoutManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ProductDetailActivity extends AppCompatActivity {
    private ActivityProductDetailBinding binding;
    private Products product;
    private CartViewModel cartViewModel;
    private SessionManager sessionManager;
    private ReviewRepository reviewRepository;
    private ProductReviewAdapter reviewAdapter;
    private ProductImageAdapter productImageAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProductDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sessionManager = new SessionManager(this);
        cartViewModel = new ViewModelProvider(this).get(CartViewModel.class);
        reviewRepository = new ReviewRepository();

        // Nhận dữ liệu product từ Intent
        product = (Products) getIntent().getSerializableExtra("product");

        if (product != null) {
            displayProductDetails();
            setupReviewRecyclerView();
            fetchReviews();
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
        binding.tvUsesDetail.setText(displayText(product.uses));
        binding.tvUsageDetail.setText(displayText(product.usage));
        binding.tvSideEffectsDetail.setText(displayText(product.side_effects));
        binding.tvPrecautionsDetail.setText(displayText(product.precautions));
        binding.tvStorageDetail.setText(displayText(product.storage));
        
        if (product.sale_price != null && product.sale_price > 0 && product.sale_price < product.price) {
            binding.tvOldPriceDetail.setVisibility(View.VISIBLE);
            binding.tvOldPriceDetail.setText(formatter.format(product.price));
            binding.tvPriceDetail.setText(formatPriceWithUnit(formatter, product.sale_price));
            binding.btnBuyNowDetail.setText("Mua ngay - " + formatter.format(product.sale_price));
            
            // Tính phần trăm giảm giá
            double discountPercent = ((product.price - product.sale_price) / product.price) * 100;
            binding.tvDiscountDetail.setVisibility(View.VISIBLE);
            binding.tvDiscountDetail.setText("-" + Math.round(discountPercent) + "%");
        } else {
            binding.tvOldPriceDetail.setVisibility(View.GONE);
            binding.tvDiscountDetail.setVisibility(View.GONE);
            binding.tvPriceDetail.setText(formatPriceWithUnit(formatter, product.price));
            binding.btnBuyNowDetail.setText("Mua ngay - " + formatter.format(product.price));
        }

        setupProductImages();
    }

    private String displayText(String value) {
        return value == null || value.trim().isEmpty() ? "Chưa có thông tin" : value;
    }

    private String formatPriceWithUnit(NumberFormat formatter, double price) {
        String formattedPrice = formatter.format(price);
        if (product == null || product.unit == null || product.unit.trim().isEmpty()) {
            return formattedPrice;
        }
        return formattedPrice + " / " + product.unit.trim();
    }

    private void setupProductImages() {
        if (product.images == null || product.images.isEmpty()) {
            binding.imgProductDetail.setImageResource(R.drawable.ic_product);
            binding.rvProductImages.setVisibility(View.GONE);
            return;
        }

        loadProductImage(product.images.get(0));

        if (product.images.size() <= 1) {
            binding.rvProductImages.setVisibility(View.GONE);
            return;
        }

        binding.rvProductImages.setVisibility(View.VISIBLE);
        binding.rvProductImages.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        );
        productImageAdapter = new ProductImageAdapter(this::loadProductImage);
        binding.rvProductImages.setAdapter(productImageAdapter);
        productImageAdapter.setImages(product.images);
    }

    private void loadProductImage(String imageUrl) {
        Glide.with(this)
                .load(imageUrl)
                .placeholder(R.drawable.ic_product)
                .error(R.drawable.ic_product)
                .into(binding.imgProductDetail);
    }

    private void setupReviewRecyclerView() {
        reviewAdapter = new ProductReviewAdapter();
        binding.rvReviews.setLayoutManager(new LinearLayoutManager(this));
        binding.rvReviews.setAdapter(reviewAdapter);
    }

    private void fetchReviews() {
        reviewRepository.getProductReviews(product.id, new Callback<List<Reviews>>() {
            @Override
            public void onResponse(Call<List<Reviews>> call, Response<List<Reviews>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Reviews> reviews = response.body();
                    updateReviewSummary(reviews);
                    reviewAdapter.setReviews(reviews);
                }
            }

            @Override
            public void onFailure(Call<List<Reviews>> call, Throwable t) {
                // Ignore error for now
            }
        });
    }

    private void updateReviewSummary(List<Reviews> reviews) {
        int total = reviews.size();
        binding.tvReviewTitle.setText("Đánh giá sản phẩm (" + total + " đánh giá)");
        
        if (total == 0) return;

        int sum = 0;
        int[] starCounts = new int[6]; // 1-5

        for (Reviews r : reviews) {
            sum += r.rating;
            if (r.rating >= 1 && r.rating <= 5) {
                starCounts[r.rating]++;
            }
        }

        float avg = (float) sum / total;
        binding.tvAvgRating.setText(String.format(Locale.getDefault(), "%.1f", avg));

        // Update progress indicators (using camelCase as generated by ViewBinding)
        binding.progress5Star.setProgress((int) ((float) starCounts[5] / total * 100));
        binding.progress4Star.setProgress((int) ((float) starCounts[4] / total * 100));
        binding.progress3Star.setProgress((int) ((float) starCounts[3] / total * 100));
        binding.progress2Star.setProgress((int) ((float) starCounts[2] / total * 100));
        binding.progress1Star.setProgress((int) ((float) starCounts[1] / total * 100));

        binding.tvCount5Star.setText(String.valueOf(starCounts[5]));
        binding.tvCount4Star.setText(String.valueOf(starCounts[4]));
        binding.tvCount3Star.setText(String.valueOf(starCounts[3]));
        binding.tvCount2Star.setText(String.valueOf(starCounts[2]));
        binding.tvCount1Star.setText(String.valueOf(starCounts[1]));
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

        binding.btnChatDetail.setOnClickListener(v -> {
            if (sessionManager.isLoggedIn()) {
                Intent intent = new Intent(this, ChatActivity.class);
                intent.putExtra(ChatActivity.EXTRA_PRODUCT_ATTACHMENT, product);
                startActivity(intent);
            } else {
                Toast.makeText(
                        this,
                        "Vui lòng đăng nhập để chat với shop",
                        Toast.LENGTH_SHORT
                ).show();
            }
        });

        binding.btnBuyNowDetail.setOnClickListener(v -> {
            if (sessionManager.isLoggedIn()) {
                showBuyNowBottomSheet();
            } else {
                Toast.makeText(this, "Vui lòng đăng nhập để mua hàng", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showBuyNowBottomSheet() {
        ProductBuyNowBottomSheet.show(this, getLayoutInflater(), product, new ProductBuyNowBottomSheet.OnProductPurchaseActionListener() {
            @Override
            public void onAddToCart(Products product, int quantity) {
                cartViewModel.addToCart(sessionManager.getUserId(), product.id, quantity, sessionManager.getToken());
            }

            @Override
            public void onBuyNow(Products product, int quantity) {
                openPayment(product, quantity);
            }
        });
    }

    private void openPayment(Products product, int quantity) {
        CartItem buyNowItem = new CartItem();
        buyNowItem.product_id = product.id;
        buyNowItem.quantity = quantity;
        buyNowItem.products = product;

        ArrayList<CartItem> paymentItems = new ArrayList<>();
        paymentItems.add(buyNowItem);

        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("open_payment", true);
        intent.putExtra("payment_items", paymentItems);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }
}
