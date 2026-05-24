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
import com.example.medictown.databinding.LayoutBuyNowBottomSheetBinding;
import com.example.medictown.ui.cart.CartViewModel;
import com.example.medictown.data.models.Reviews;
import com.example.medictown.data.repositories.ReviewRepository;
import com.google.android.material.bottomsheet.BottomSheetDialog;
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
    private int buyNowQuantity = 1;

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

        binding.btnBuyNowDetail.setOnClickListener(v -> {
            if (sessionManager.isLoggedIn()) {
                showBuyNowBottomSheet();
            } else {
                Toast.makeText(this, "Vui lòng đăng nhập để mua hàng", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showBuyNowBottomSheet() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this, R.style.BottomSheetDialogTheme);
        LayoutBuyNowBottomSheetBinding sheetBinding = LayoutBuyNowBottomSheetBinding.inflate(getLayoutInflater());
        bottomSheetDialog.setContentView(sheetBinding.getRoot());

        buyNowQuantity = 1;
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

        // Set Product Info
        if (product.images != null && !product.images.isEmpty()) {
            Glide.with(this).load(product.images.get(0)).into(sheetBinding.imgProduct);
        }

        double currentPrice = (product.sale_price != null && product.sale_price > 0) ? product.sale_price : product.price;
        sheetBinding.tvPrice.setText(formatter.format(currentPrice));
        
        if (product.sale_price != null && product.sale_price > 0) {
            sheetBinding.tvOldPrice.setVisibility(View.VISIBLE);
            sheetBinding.tvOldPrice.setText(formatter.format(product.price));
            sheetBinding.tvOldPrice.setPaintFlags(sheetBinding.tvOldPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            sheetBinding.tvOldPrice.setVisibility(View.GONE);
        }

        sheetBinding.tvStock.setText("Kho: " + product.stock);

        // Quantity Logic
        sheetBinding.btnPlus.setOnClickListener(v -> {
            if (buyNowQuantity < product.stock) {
                buyNowQuantity++;
                sheetBinding.tvQuantity.setText(String.valueOf(buyNowQuantity));
            }
        });

        sheetBinding.btnMinus.setOnClickListener(v -> {
            if (buyNowQuantity > 1) {
                buyNowQuantity--;
                sheetBinding.tvQuantity.setText(String.valueOf(buyNowQuantity));
            }
        });

        // Buy Now Logic
        sheetBinding.btnBuyNow.setOnClickListener(v -> {
            CartItem buyNowItem = new CartItem();
            buyNowItem.product_id = product.id;
            buyNowItem.quantity = buyNowQuantity;
            buyNowItem.products = product;

            ArrayList<CartItem> paymentItems = new ArrayList<>();
            paymentItems.add(buyNowItem);

            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("open_payment", true);
            intent.putExtra("payment_items", paymentItems);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            
            bottomSheetDialog.dismiss();
            finish();
        });

        bottomSheetDialog.show();
    }
}
