package com.example.medictown.ui.product;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.viewpager2.widget.MarginPageTransformer;
import androidx.viewpager2.widget.ViewPager2;
import com.example.medictown.MainActivity;
import com.example.medictown.R;
import com.example.medictown.data.api.SessionManager;
import com.example.medictown.data.models.Advertisement;
import com.example.medictown.data.models.CartItem;
import com.example.medictown.data.models.Products;
import com.example.medictown.data.repositories.AdvertisementRepository;
import com.example.medictown.databinding.FragmentProductBinding;
import com.example.medictown.ui.cart.CartViewModel;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ProductFragment extends Fragment {
    private FragmentProductBinding binding;
    private ProductViewModel viewModel;
    private CartViewModel cartViewModel;
    private SessionManager sessionManager;
    private ProductAdapter adapter;
    private AdBannerAdapter adBannerAdapter;
    private AdvertisementRepository advertisementRepository;
    private final Set<String> viewedAdvertisementIds = new HashSet<>();
    private final Handler adAutoScrollHandler = new Handler(Looper.getMainLooper());
    private final Runnable adAutoScrollRunnable = new Runnable() {
        @Override
        public void run() {
            if (binding == null || adBannerAdapter == null || adBannerAdapter.getItemCount() <= 1) {
                return;
            }
            int nextItem = (binding.vpAdBanners.getCurrentItem() + 1) % adBannerAdapter.getItemCount();
            binding.vpAdBanners.setCurrentItem(nextItem, true);
            scheduleAdAutoScroll();
        }
    };
    private final ViewPager2.OnPageChangeCallback adPageChangeCallback = new ViewPager2.OnPageChangeCallback() {
        @Override
        public void onPageSelected(int position) {
            updateAdIndicator(position);
            recordAdView(position);
        }
    };

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
        advertisementRepository = new AdvertisementRepository();

        setupRecyclerView();
        setupAdBanner();
        setupSearch();
        setupCategoryNavigation();
        observeViewModel();
        
        // Gọi tải tất cả sản phẩm
        viewModel.loadHomeBannerAds();
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

    private void setupAdBanner() {
        adBannerAdapter = new AdBannerAdapter();
        adBannerAdapter.setOnAdClickListener(this::openAdTarget);
        binding.vpAdBanners.setAdapter(adBannerAdapter);
        binding.vpAdBanners.setClipToPadding(false);
        binding.vpAdBanners.setClipChildren(false);
        binding.vpAdBanners.setOffscreenPageLimit(3);
        binding.vpAdBanners.setPageTransformer(new MarginPageTransformer(dpToPx(8)));
        binding.vpAdBanners.registerOnPageChangeCallback(adPageChangeCallback);
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

    private void openAdTarget(Advertisement advertisement) {
        if (advertisement == null) return;
        if (advertisement.id != null) {
            advertisementRepository.recordClick(advertisement.id);
        }
        if ("none".equals(advertisement.target_type)) {
            return;
        }
        if ("external_url".equals(advertisement.target_type)
                && advertisement.target_url != null) {
            startActivity(new android.content.Intent(
                    android.content.Intent.ACTION_VIEW,
                    android.net.Uri.parse(advertisement.target_url)
            ));
            return;
        }
        if ("shop".equals(advertisement.target_type)) {
            android.content.Intent intent = new android.content.Intent(
                    getContext(),
                    SearchProductActivity.class
            );
            intent.putExtra("mode", "shop");
            intent.putExtra("shop_id", advertisement.target_id);
            intent.putExtra("shop_title", "Sản phẩm của shop");
            startActivity(intent);
            return;
        }
        if (advertisement.product == null) {
            android.widget.Toast.makeText(
                    getContext(),
                    "Sản phẩm quảng cáo chưa sẵn sàng",
                    android.widget.Toast.LENGTH_SHORT
            ).show();
            return;
        }
        android.content.Intent intent = new android.content.Intent(
                getContext(),
                ProductDetailActivity.class
        );
        intent.putExtra("product", advertisement.product);
        startActivity(intent);
    }

    private void recordAdView(int position) {
        if (advertisementRepository == null || adBannerAdapter == null) return;
        Advertisement advertisement = adBannerAdapter.getAdvertisement(position);
        if (advertisement == null || advertisement.id == null) return;
        if (viewedAdvertisementIds.add(advertisement.id)) {
            advertisementRepository.recordView(advertisement.id);
        }
    }

    private void observeViewModel() {
        viewModel.getHomeBannerAds().observe(getViewLifecycleOwner(), this::bindAdBanners);

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

    private void bindAdBanners(List<Advertisement> advertisements) {
        if (binding == null || advertisements == null || advertisements.isEmpty()) {
            stopAdAutoScroll();
            if (binding != null) {
                binding.layoutAdBanner.setVisibility(View.GONE);
            }
            return;
        }

        binding.layoutAdBanner.setVisibility(View.VISIBLE);
        adBannerAdapter.setAdvertisements(advertisements);
        binding.vpAdBanners.setCurrentItem(0, false);
        recordAdView(0);
        buildAdIndicator(advertisements.size());
        updateAdIndicator(0);

        if (advertisements.size() > 1) {
            scheduleAdAutoScroll();
        } else {
            stopAdAutoScroll();
        }
    }

    private void buildAdIndicator(int count) {
        binding.llAdIndicator.removeAllViews();
        binding.llAdIndicator.setVisibility(count > 1 ? View.VISIBLE : View.GONE);
        for (int index = 0; index < count; index++) {
            View dot = new View(requireContext());
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dpToPx(8), dpToPx(8));
            params.setMargins(dpToPx(4), 0, dpToPx(4), 0);
            binding.llAdIndicator.addView(dot, params);
        }
    }

    private void updateAdIndicator(int selectedPosition) {
        if (binding == null) {
            return;
        }
        for (int index = 0; index < binding.llAdIndicator.getChildCount(); index++) {
            binding.llAdIndicator.getChildAt(index).setBackgroundResource(
                    index == selectedPosition ? R.drawable.bg_ad_dot_active : R.drawable.bg_ad_dot_inactive
            );
        }
    }

    private void scheduleAdAutoScroll() {
        stopAdAutoScroll();
        adAutoScrollHandler.postDelayed(adAutoScrollRunnable, 5000);
    }

    private void stopAdAutoScroll() {
        adAutoScrollHandler.removeCallbacks(adAutoScrollRunnable);
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (adBannerAdapter != null && adBannerAdapter.getItemCount() > 1) {
            scheduleAdAutoScroll();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        stopAdAutoScroll();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        stopAdAutoScroll();
        if (binding != null) {
            binding.vpAdBanners.unregisterOnPageChangeCallback(adPageChangeCallback);
        }
        binding = null;
    }
}
