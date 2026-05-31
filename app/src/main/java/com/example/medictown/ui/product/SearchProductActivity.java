package com.example.medictown.ui.product;

import android.content.Intent;
import android.os.Bundle;
import android.view.inputmethod.EditorInfo;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import com.example.medictown.MainActivity;
import com.example.medictown.data.api.SessionManager;
import com.example.medictown.data.models.CartItem;
import com.example.medictown.data.models.Products;
import com.example.medictown.databinding.ActivitySearchProductBinding;
import com.example.medictown.ui.cart.CartViewModel;

import java.util.ArrayList;

public class SearchProductActivity extends AppCompatActivity {
    private ActivitySearchProductBinding binding;
    private ProductAdapter adapter;
    private ProductViewModel viewModel;
    private CartViewModel cartViewModel;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        androidx.activity.EdgeToEdge.enable(this);
        binding = ActivitySearchProductBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(binding.searchRoot, (v, insets) -> {
            androidx.core.graphics.Insets systemBars = insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        viewModel = new ViewModelProvider(this).get(ProductViewModel.class);
        cartViewModel = new ViewModelProvider(this).get(CartViewModel.class);
        sessionManager = new SessionManager(this);
        setupUI();
        observeData();
        
        handleIntent();
    }

    private void handleIntent() {
        String mode = getIntent().getStringExtra("mode");
        if ("category".equals(mode)) {
            String categoryKey = getIntent().getStringExtra("category_key");
            String categoryTitle = getIntent().getStringExtra("category_title");
            if (categoryTitle != null && !categoryTitle.isEmpty()) {
                binding.etSearch.setHint(categoryTitle);
            }
            viewModel.loadProductsByCategoryKey(categoryKey);
            binding.etSearch.clearFocus();
            hideKeyboard();
        } else if ("see_all".equals(mode)) {
            // Chế độ Xem tất cả: Hiện SP, không focus, ẩn bàn phím
            viewModel.loadAllProducts();
            binding.etSearch.clearFocus();
            hideKeyboard();
        } else {
            // Chế độ Tìm kiếm (mặc định): Không hiện SP, focus vào thanh tìm kiếm
            binding.etSearch.requestFocus();
            binding.etSearch.postDelayed(this::showKeyboard, 200);
        }
    }

    private void setupUI() {
        adapter = new ProductAdapter();
        adapter.setOnProductClickListener(new ProductAdapter.OnProductClickListener() {
            @Override
            public void onProductClick(Products product) {
                Intent intent = new Intent(SearchProductActivity.this, ProductDetailActivity.class);
                intent.putExtra("product", product);
                startActivity(intent);
            }

            @Override
            public void onBuyNowClick(Products product) {
                showBuyNowBottomSheet(product);
            }
        });
        
        binding.rvSearchResults.setLayoutManager(new GridLayoutManager(this, 2));
        binding.rvSearchResults.setAdapter(adapter);
        
        binding.btnBack.setOnClickListener(v -> finish());

        // Hiện bàn phím khi người dùng bấm trực tiếp vào thanh tìm kiếm
        binding.etSearch.setOnClickListener(v -> showKeyboard());

        // 3. Hiện bàn phím khi ô nhập liệu nhận focus
        binding.etSearch.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                showKeyboard();
            }
        });

        // 4. Xử lý nút Tìm kiếm (Enter/Accept) trên bàn phím
        binding.etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                String query = binding.etSearch.getText().toString().trim();
                if (!query.isEmpty()) {
                    hideKeyboard(); // Ẩn bàn phím sau khi nhấn tìm kiếm
                    viewModel.searchProducts(query);
                }
                return true;
            }
            return false;
        });

        // Hết focus khi chạm vào vùng trống hoặc danh sách
        binding.searchRoot.setOnClickListener(v -> {
            binding.etSearch.clearFocus();
            hideKeyboard();
        });

        binding.rvSearchResults.setOnTouchListener((v, event) -> {
            if (binding.etSearch.isFocused()) {
                binding.etSearch.clearFocus();
                hideKeyboard();
            }
            return false;
        });

        binding.rvSearchResults.addOnScrollListener(new androidx.recyclerview.widget.RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@androidx.annotation.NonNull androidx.recyclerview.widget.RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == androidx.recyclerview.widget.RecyclerView.SCROLL_STATE_DRAGGING) {
                    binding.etSearch.clearFocus();
                    hideKeyboard();
                }
            }
        });
    }

    private void showBuyNowBottomSheet(Products product) {
        ProductBuyNowBottomSheet.show(this, getLayoutInflater(), product, new ProductBuyNowBottomSheet.OnProductPurchaseActionListener() {
            @Override
            public void onAddToCart(Products product, int quantity) {
                if (!sessionManager.isLoggedIn()) {
                    android.widget.Toast.makeText(SearchProductActivity.this, "Vui lòng đăng nhập để thêm vào giỏ hàng", android.widget.Toast.LENGTH_SHORT).show();
                    return;
                }
                cartViewModel.addToCart(sessionManager.getUserId(), product.id, quantity, sessionManager.getToken());
            }

            @Override
            public void onBuyNow(Products product, int quantity) {
                if (!sessionManager.isLoggedIn()) {
                    android.widget.Toast.makeText(SearchProductActivity.this, "Vui lòng đăng nhập để mua hàng", android.widget.Toast.LENGTH_SHORT).show();
                    return;
                }
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

    private void showKeyboard() {
        android.view.inputmethod.InputMethodManager imm = (android.view.inputmethod.InputMethodManager) getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.showSoftInput(binding.etSearch, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT);
        }
    }

    private void hideKeyboard() {
        android.view.inputmethod.InputMethodManager imm = (android.view.inputmethod.InputMethodManager) getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(binding.etSearch.getWindowToken(), 0);
        }
    }

    private void observeData() {
        viewModel.getAllProducts().observe(this, products -> {
            if (products != null) {
                adapter.setProductList(products);
            }
        });

        viewModel.getErrorMessage().observe(this, error -> {
            if (error != null) {
                android.widget.Toast.makeText(this, error, android.widget.Toast.LENGTH_SHORT).show();
            }
        });

        cartViewModel.addToCartStatus.observe(this, status -> {
            if (status != null) {
                android.widget.Toast.makeText(this, status, android.widget.Toast.LENGTH_SHORT).show();
            }
        });
    }
}
