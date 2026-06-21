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
import com.example.medictown.data.repositories.RecommendationRepository;
import com.example.medictown.data.repositories.ReviewRepository;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ProductDetailActivity extends AppCompatActivity {
    private ActivityProductDetailBinding binding;
    private Products product;
    private CartViewModel cartViewModel;
    private SessionManager sessionManager;
    private ReviewRepository reviewRepository;
    private RecommendationRepository recommendationRepository;
    private ProductReviewAdapter reviewAdapter;
    private ProductImageAdapter productImageAdapter;
    private ProductAdapter relatedProductAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        postponeEnterTransition();
        binding = ActivityProductDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sessionManager = new SessionManager(this);
        cartViewModel = new ViewModelProvider(this).get(CartViewModel.class);
        reviewRepository = new ReviewRepository();
        recommendationRepository = new RecommendationRepository();

        // Nhận dữ liệu product từ Intent
        product = (Products) getIntent().getSerializableExtra("product");

        if (product != null) {
            displayProductDetails();
            setupReviewRecyclerView();
            setupRelatedProductsRecyclerView();
            fetchReviews();
            fetchRelatedProducts();
            recordProductEvent("view", buildMetadata("product_detail", 0));
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

        // Badges visibility
        binding.tvPrescriptionBadge.setVisibility(product.requires_prescription ? View.VISIBLE : View.GONE);
        binding.tvBestSellerBadge.setVisibility(product.is_best_seller ? View.VISIBLE : View.GONE);
        
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
                .listener(new com.bumptech.glide.request.RequestListener<android.graphics.drawable.Drawable>() {
                    @Override
                    public boolean onLoadFailed(@androidx.annotation.Nullable com.bumptech.glide.load.engine.GlideException e, Object model, com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable> target, boolean isFirstResource) {
                        startPostponedEnterTransition();
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(android.graphics.drawable.Drawable resource, Object model, com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable> target, com.bumptech.glide.load.DataSource dataSource, boolean isFirstResource) {
                        startPostponedEnterTransition();
                        return false;
                    }
                })
                .into(binding.imgProductDetail);
    }

    private void setupReviewRecyclerView() {
        reviewAdapter = new ProductReviewAdapter();
        binding.rvReviews.setLayoutManager(new LinearLayoutManager(this));
        binding.rvReviews.setAdapter(reviewAdapter);
    }

    private void setupRelatedProductsRecyclerView() {
        relatedProductAdapter = new ProductAdapter();
        relatedProductAdapter.setOnProductClickListener(new ProductAdapter.OnProductClickListener() {
            @Override
            public void onProductClick(Products relatedProduct, android.widget.ImageView productImage) {
                recordProductEvent(relatedProduct, "click", buildMetadata("related_products", 0));
                Intent intent = new Intent(ProductDetailActivity.this, ProductDetailActivity.class);
                intent.putExtra("product", relatedProduct);

                androidx.core.app.ActivityOptionsCompat options = androidx.core.app.ActivityOptionsCompat.makeSceneTransitionAnimation(
                        ProductDetailActivity.this,
                        productImage,
                        androidx.core.view.ViewCompat.getTransitionName(productImage)
                );
                startActivity(intent, options.toBundle());
            }

            @Override
            public void onBuyNowClick(Products relatedProduct) {
                showRelatedProductBottomSheet(relatedProduct);
            }
        });
        binding.rvRelatedProducts.setLayoutManager(new GridLayoutManager(this, 2));
        binding.rvRelatedProducts.setAdapter(relatedProductAdapter);
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

    private void fetchRelatedProducts() {
        if (product == null || product.id == null || product.id.trim().isEmpty()) {
            binding.cardRelatedProducts.setVisibility(View.GONE);
            return;
        }
        recommendationRepository.getRelatedProducts(product.id, 10, new Callback<List<Products>>() {
            @Override
            public void onResponse(Call<List<Products>> call, Response<List<Products>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    binding.cardRelatedProducts.setVisibility(View.VISIBLE);
                    relatedProductAdapter.setProductList(response.body());
                } else {
                    binding.cardRelatedProducts.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(Call<List<Products>> call, Throwable t) {
                binding.cardRelatedProducts.setVisibility(View.GONE);
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
            supportFinishAfterTransition();
        });
        
        binding.btnAddToCartDetail.setOnClickListener(v -> {
            if (sessionManager.isLoggedIn()) {
                String userId = sessionManager.getUserId();
                String token = sessionManager.getToken();
                cartViewModel.addToCart(userId, product.id, 1, token);
                recordProductEvent("add_to_cart", buildMetadata("product_detail", 1));
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
                recordProductEvent("add_to_cart", buildMetadata("buy_now_sheet", quantity));
            }

            @Override
            public void onBuyNow(Products product, int quantity) {
                recordProductEvent("buy", buildMetadata("buy_now_sheet", quantity));
                openPayment(product, quantity);
            }
        });
    }

    private void showRelatedProductBottomSheet(Products relatedProduct) {
        ProductBuyNowBottomSheet.show(this, getLayoutInflater(), relatedProduct, new ProductBuyNowBottomSheet.OnProductPurchaseActionListener() {
            @Override
            public void onAddToCart(Products product, int quantity) {
                if (!sessionManager.isLoggedIn()) {
                    Toast.makeText(ProductDetailActivity.this, "Vui lòng đăng nhập để thêm vào giỏ hàng", Toast.LENGTH_SHORT).show();
                    return;
                }
                cartViewModel.addToCart(sessionManager.getUserId(), product.id, quantity, sessionManager.getToken());
                recordProductEvent(product, "add_to_cart", buildMetadata("related_products_sheet", quantity));
            }

            @Override
            public void onBuyNow(Products product, int quantity) {
                if (!sessionManager.isLoggedIn()) {
                    Toast.makeText(ProductDetailActivity.this, "Vui lòng đăng nhập để mua hàng", Toast.LENGTH_SHORT).show();
                    return;
                }
                recordProductEvent(product, "buy", buildMetadata("related_products_sheet", quantity));
                openPayment(product, quantity);
            }
        });
    }

    private void recordProductEvent(String eventType, Map<String, Object> metadata) {
        recordProductEvent(product, eventType, metadata);
    }

    private void recordProductEvent(Products eventProduct, String eventType, Map<String, Object> metadata) {
        if (eventProduct == null || eventProduct.id == null || eventProduct.id.trim().isEmpty()
                || recommendationRepository == null || sessionManager == null || !sessionManager.isLoggedIn()) {
            return;
        }
        recommendationRepository.recordEvent(eventProduct.id, eventType, metadata);
    }

    private Map<String, Object> buildMetadata(String source, int quantity) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("source", source);
        if (quantity > 0) {
            metadata.put("quantity", quantity);
        }
        return metadata;
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
