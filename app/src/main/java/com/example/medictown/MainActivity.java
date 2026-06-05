package com.example.medictown;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import androidx.appcompat.content.res.AppCompatResources;
import com.example.medictown.data.api.SessionManager;
import com.example.medictown.data.models.CartItem;
import com.example.medictown.ui.admin.AdminDashboardFragment;
import com.example.medictown.ui.admin.AdminInventoryFragment;
import com.example.medictown.ui.admin.AdminOrdersFragment;
import com.example.medictown.ui.cart.CartFragment;
import com.example.medictown.ui.history.HistoryFragment;
import com.example.medictown.ui.payment.PaymentFragment;
import com.example.medictown.ui.product.ProductFragment;
import com.example.medictown.ui.profile.ProfileFragment;
import com.example.medictown.ui.shop.ShopProfileFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;
    private View appBarMain;
    private boolean sellerMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        new SessionManager(this);

        appBarMain = findViewById(R.id.app_bar_main);
        bottomNav = findViewById(R.id.bottom_navigation);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new ProductFragment())
                    .commit();
        }

        setupBottomNavigation();
        handleIntent(getIntent());
    }

    private void setupBottomNavigation() {
        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();

            if (sellerMode) {
                if (itemId == R.id.nav_seller_inventory) {
                    selectedFragment = new AdminInventoryFragment();
                } else if (itemId == R.id.nav_seller_orders) {
                    selectedFragment = new AdminOrdersFragment();
                } else if (itemId == R.id.nav_seller_revenue) {
                    selectedFragment = new AdminDashboardFragment();
                } else if (itemId == R.id.nav_seller_profile) {
                    selectedFragment = new ShopProfileFragment();
                }
            } else {
                if (itemId == R.id.nav_product) {
                    selectedFragment = new ProductFragment();
                } else if (itemId == R.id.nav_cart) {
                    selectedFragment = new CartFragment();
                } else if (itemId == R.id.nav_history) {
                    selectedFragment = new HistoryFragment();
                } else if (itemId == R.id.nav_profile) {
                    selectedFragment = new ProfileFragment();
                }
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .commit();
            }
            return true;
        });
    }

    public void setNavBarsVisibility(boolean show) {
        int visibility = show ? View.VISIBLE : View.GONE;
        if (bottomNav != null) {
            bottomNav.setVisibility(visibility);
        }
        if (appBarMain != null) {
            appBarMain.setVisibility(visibility);
        }
    }

    public void openSellerChannel() {
        sellerMode = true;
        getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        bottomNav.getMenu().clear();
        bottomNav.inflateMenu(R.menu.seller_bottom_nav_menu);

        // Đổi màu thanh navigation sang màu admin (xanh lá)
        bottomNav.setItemBackgroundResource(R.drawable.seller_nav_indicator_background);
        bottomNav.setItemIconTintList(AppCompatResources.getColorStateList(this, R.color.seller_nav_item_color_state));
        bottomNav.setItemTextColor(AppCompatResources.getColorStateList(this, R.color.seller_nav_item_color_state));

        setupBottomNavigation();
        bottomNav.setSelectedItemId(R.id.nav_seller_inventory);
    }

    public void openBuyerChannel() {
        sellerMode = false;
        getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        bottomNav.getMenu().clear();
        bottomNav.inflateMenu(R.menu.bottom_nav_menu);

        // Đổi màu thanh navigation về màu mặc định (xanh dương)
        bottomNav.setItemBackgroundResource(R.drawable.nav_indicator_background);
        bottomNav.setItemIconTintList(AppCompatResources.getColorStateList(this, R.color.nav_item_color_state));
        bottomNav.setItemTextColor(AppCompatResources.getColorStateList(this, R.color.nav_item_color_state));

        setupBottomNavigation();
        bottomNav.setSelectedItemId(R.id.nav_product);
    }

    @Override
    protected void onNewIntent(@NonNull Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (intent != null) {
            Uri data = intent.getData();
            if (data != null && "medictown".equals(data.getScheme()) && "payment".equals(data.getHost())) {
                if (sellerMode) {
                    openBuyerChannel();
                }
                bottomNav.setSelectedItemId(R.id.nav_history);
            } else if (intent.getBooleanExtra("open_cart", false)) {
                if (sellerMode) {
                    openBuyerChannel();
                }
                bottomNav.setSelectedItemId(R.id.nav_cart);
            } else if (intent.hasExtra("open_payment")) {
                if (sellerMode) {
                    openBuyerChannel();
                }
                List<CartItem> items = (List<CartItem>) intent.getSerializableExtra("payment_items");
                if (items != null) {
                    PaymentFragment paymentFragment = PaymentFragment.newInstance(items);
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, paymentFragment)
                            .addToBackStack(null)
                            .commit();
                }
            } else if (intent.getBooleanExtra("open_seller_dashboard", false)) {
                openSellerChannel();
            }
        }
    }
}
