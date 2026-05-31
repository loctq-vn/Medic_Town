package com.example.medictown.ui.product;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import com.example.medictown.MainActivity;
import com.example.medictown.data.api.SessionManager;
import com.example.medictown.data.models.CartItem;
import com.example.medictown.data.models.Products;
import com.example.medictown.databinding.FragmentProductBinding;
import com.example.medictown.ui.cart.CartViewModel;

import java.util.ArrayList;

public class ProductFragment extends Fragment {
    private FragmentProductBinding binding;
    private ProductViewModel viewModel;
    private CartViewModel cartViewModel;
    private SessionManager sessionManager;
    private ProductAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentProductBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(ProductViewModel.class);
        cartViewModel = new ViewModelProvider(this).get(CartViewModel.class);
        sessionManager = new SessionManager(requireContext());

        setupRecyclerView();
        setupSearch();
        setupCategoryNavigation();
        observeViewModel();
        
        // Gọi tải tất cả sản phẩm
        viewModel.loadAllProducts();
    }

    private void setupSearch() {
        binding.cvSearch.setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(getContext(), SearchProductActivity.class);
            intent.putExtra("mode", "search");
            startActivity(intent);
        });

        binding.tvSeeAll.setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(getContext(), SearchProductActivity.class);
            intent.putExtra("mode", "see_all");
            startActivity(intent);
        });
    }

    private void setupCategoryNavigation() {
        binding.cvCategoryMedicine.setOnClickListener(v -> openCategory("medicine", "Thuốc"));
        binding.cvCategorySupplement.setOnClickListener(v -> openCategory("supplement", "Thực phẩm chức năng"));
        binding.cvCategoryCosmetic.setOnClickListener(v -> openCategory("cosmetic", "Dược mỹ phẩm"));
        binding.cvCategoryDevice.setOnClickListener(v -> openCategory("device", "Thiết bị y tế"));
        binding.cvCategoryPersonalCare.setOnClickListener(v -> openCategory("personal_care", "Chăm sóc cá nhân"));
    }

    private void openCategory(String categoryKey, String categoryTitle) {
        android.content.Intent intent = new android.content.Intent(getContext(), SearchProductActivity.class);
        intent.putExtra("mode", "category");
        intent.putExtra("category_key", categoryKey);
        intent.putExtra("category_title", categoryTitle);
        startActivity(intent);
    }

    private void setupRecyclerView() {
        adapter = new ProductAdapter();
        adapter.setOnProductClickListener(new ProductAdapter.OnProductClickListener() {
            @Override
            public void onProductClick(Products product) {
                android.content.Intent intent = new android.content.Intent(getContext(), ProductDetailActivity.class);
                intent.putExtra("product", product);
                startActivity(intent);
            }

            @Override
            public void onBuyNowClick(Products product) {
                showBuyNowBottomSheet(product);
            }
        });
        binding.rvProducts.setLayoutManager(new GridLayoutManager(getContext(), 2));
        binding.rvProducts.setAdapter(adapter);
    }

    private void showBuyNowBottomSheet(Products product) {
        ProductBuyNowBottomSheet.show(requireContext(), getLayoutInflater(), product, new ProductBuyNowBottomSheet.OnProductPurchaseActionListener() {
            @Override
            public void onAddToCart(Products product, int quantity) {
                if (!sessionManager.isLoggedIn()) {
                    android.widget.Toast.makeText(getContext(), "Vui lòng đăng nhập để thêm vào giỏ hàng", android.widget.Toast.LENGTH_SHORT).show();
                    return;
                }
                cartViewModel.addToCart(sessionManager.getUserId(), product.id, quantity, sessionManager.getToken());
            }

            @Override
            public void onBuyNow(Products product, int quantity) {
                if (!sessionManager.isLoggedIn()) {
                    android.widget.Toast.makeText(getContext(), "Vui lòng đăng nhập để mua hàng", android.widget.Toast.LENGTH_SHORT).show();
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

        android.content.Intent intent = new android.content.Intent(requireContext(), MainActivity.class);
        intent.putExtra("open_payment", true);
        intent.putExtra("payment_items", paymentItems);
        intent.addFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP | android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
    }

    private void observeViewModel() {
        // Quan sát cả featured và all products để hiển thị
        viewModel.getFeaturedProducts().observe(getViewLifecycleOwner(), products -> {
            if (products != null && !products.isEmpty()) {
                adapter.setProductList(products);
            }
        });

        viewModel.getAllProducts().observe(getViewLifecycleOwner(), products -> {
            if (products != null && !products.isEmpty()) {
                adapter.setProductList(products);
            }
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                android.widget.Toast.makeText(getContext(), error, android.widget.Toast.LENGTH_LONG).show();
            }
        });

        cartViewModel.addToCartStatus.observe(getViewLifecycleOwner(), status -> {
            if (status != null) {
                android.widget.Toast.makeText(getContext(), status, android.widget.Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
