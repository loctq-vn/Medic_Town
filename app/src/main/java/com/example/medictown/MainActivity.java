package com.example.medictown;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.net.Uri;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import androidx.appcompat.content.res.AppCompatResources;
import com.example.medictown.data.api.SessionManager;
import com.example.medictown.data.api.SupabaseApi;
import com.example.medictown.data.api.RetrofitClient;
import com.example.medictown.data.models.CartItem;
import com.example.medictown.data.models.Orders;
import com.example.medictown.data.models.Products;
import com.example.medictown.ui.admin.AdminDashboardFragment;

import com.example.medictown.ui.admin.AdminOrderDetailFragment;

import com.example.medictown.ui.cart.CartFragment;
import com.example.medictown.ui.chat.ChatActivity;
import com.example.medictown.ui.chat.SellerConversationFragment;
import com.example.medictown.ui.history.HistoryFragment;
import com.example.medictown.ui.history.OrderDetailFragment;
import com.example.medictown.ui.notifications.NotificationCenterActivity;
import com.example.medictown.ui.payment.PaymentFragment;
import com.example.medictown.ui.product.ProductFragment;
import com.example.medictown.ui.profile.ProfileFragment;
import com.example.medictown.ui.shop.SellerProductFormFragment;
import com.example.medictown.ui.shop.ShopProfileFragment;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.example.medictown.notifications.NotificationTokenManager;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.view.LayoutInflater;
import android.view.Gravity;
import android.widget.ImageButton;
import android.widget.PopupWindow;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medictown.data.models.AppNotification;
import com.example.medictown.ui.notifications.NotificationAdapter;

import java.util.ArrayList;
import java.util.Collections;

import android.view.ViewGroup;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;
    private BottomAppBar bottomAppBar;
    private FloatingActionButton fabCenter;
    private View appBarMain;
    private ImageView appLogo;
    private TextView appTitle;
    private boolean sellerMode = false;

    private ImageButton notificationButton;
    private TextView notificationBadge;
    private final List<AppNotification> notifications = new ArrayList<>();

    private final Handler iconHandler = new Handler(Looper.getMainLooper());
    private int currentIconIndex = 0;
    private final int[] fabIcons = {
            R.drawable.contact_support,
            R.drawable.clinical_notes,
            R.drawable.health_cross_icon
    };
    private final Runnable iconAnimationRunnable = new Runnable() {
        @Override
        public void run() {
            if (fabCenter == null || fabCenter.getVisibility() != View.VISIBLE) {
                iconHandler.postDelayed(this, 3000);
                return;
            }

            ObjectAnimator fadeOut = ObjectAnimator.ofInt(fabCenter, "imageAlpha", 255, 0);
            fadeOut.setDuration(600);
            fadeOut.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    currentIconIndex = (currentIconIndex + 1) % fabIcons.length;
                    fabCenter.setImageResource(fabIcons[currentIconIndex]);

                    ObjectAnimator fadeIn = ObjectAnimator.ofInt(fabCenter, "imageAlpha", 0, 255);
                    fadeIn.setDuration(600);
                    fadeIn.start();
                }
            });
            fadeOut.start();

            iconHandler.postDelayed(this, 4000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SessionManager sessionManager = new SessionManager(this);

        if (sessionManager.isLoggedIn()) {
            NotificationTokenManager.registerCurrentToken(this);
        }

        appBarMain = findViewById(R.id.app_bar_main);
        appLogo = findViewById(R.id.app_logo);
        appTitle = findViewById(R.id.app_title);

        notificationButton = findViewById(R.id.btn_notifications);
        notificationBadge = findViewById(R.id.tv_notification_badge);

        loadNotifications();

        notificationButton.setOnClickListener(v -> showShortNotificationPopup());
        updateNotificationBadge();

        bottomNav = findViewById(R.id.bottom_navigation);
        bottomAppBar = findViewById(R.id.bottomAppBar);
        fabCenter = findViewById(R.id.fab_center);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new ProductFragment())
                    .commit();
        }

        setupBottomNavigation();
        handleIntent(getIntent());

        findViewById(R.id.fab_center).setOnClickListener(view -> {
            startActivity(new Intent(this, ChatActivity.class));
        });

        startIconAnimation();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (notificationButton != null) {
            loadNotifications();
        }
    }

    private void startIconAnimation() {
        iconHandler.removeCallbacks(iconAnimationRunnable);
        iconHandler.postDelayed(iconAnimationRunnable, 2000);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        iconHandler.removeCallbacks(iconAnimationRunnable);
    }

    private void setupBottomNavigation() {
        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();

            if (sellerMode) {
                if (itemId == R.id.nav_seller_messages) {
                    selectedFragment = new SellerConversationFragment();
                } else if (itemId == R.id.nav_seller_revenue) {
                    selectedFragment = new AdminDashboardFragment();
                } else if (itemId == R.id.nav_seller_profile) {
                    selectedFragment = new ShopProfileFragment();
                }
            } else {
                if (itemId == R.id.nav_home) {
                    selectedFragment = new ProductFragment();
                } else if (itemId == R.id.nav_history) {
                    selectedFragment = new HistoryFragment();
                } else if (itemId == R.id.nav_cart) {
                    selectedFragment = new CartFragment();
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
        if (bottomNav != null) bottomNav.setVisibility(visibility);
        if (bottomAppBar != null) bottomAppBar.setVisibility(visibility);
        if (fabCenter != null) fabCenter.setVisibility(visibility);
        if (appBarMain != null) appBarMain.setVisibility(visibility);
    }

    public void openSellerChannel() {
        sellerMode = true;
        getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        bottomNav.getMenu().clear();
        bottomNav.inflateMenu(R.menu.seller_bottom_nav_menu);

        // Đổi sang thanh dài bình thường (phẳng) cho Admin
        if (fabCenter != null) fabCenter.hide();

        // Đổi màu thanh navigation sang màu admin (xanh lá)
        bottomNav.setItemBackgroundResource(R.drawable.seller_nav_indicator_background);
        bottomNav.setItemIconTintList(AppCompatResources.getColorStateList(this, R.color.seller_nav_item_color_state));
        bottomNav.setItemTextColor(AppCompatResources.getColorStateList(this, R.color.seller_nav_item_color_state));
        bottomNav.setItemRippleColor(ColorStateList.valueOf(getResources().getColor(R.color.ripple_admin_light, getTheme())));

        // Đổi màu logo và tiêu đề sang màu admin
        if (appLogo != null) {
            appLogo.setImageTintList(AppCompatResources.getColorStateList(this, R.color.admin_primary));
        }
        if (appTitle != null) {
            appTitle.setTextColor(getResources().getColor(R.color.admin_primary, getTheme()));
        }
        if (notificationButton != null) {
            notificationButton.setImageTintList(AppCompatResources.getColorStateList(this, R.color.admin_primary));
        }

        setupBottomNavigation();
        bottomNav.setSelectedItemId(R.id.nav_seller_revenue);
    }

    public void openBuyerChannel() {
        sellerMode = false;
        getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        bottomNav.getMenu().clear();
        bottomNav.inflateMenu(R.menu.bottom_nav_menu);

        // Hiển thị lại vết lõm và nút FAB cho User
        if (fabCenter != null) fabCenter.show();

        // Đổi màu thanh navigation về màu mặc định (xanh dương)
        bottomNav.setItemBackgroundResource(R.drawable.nav_indicator_background);
        bottomNav.setItemIconTintList(AppCompatResources.getColorStateList(this, R.color.nav_item_colors));
        bottomNav.setItemTextColor(AppCompatResources.getColorStateList(this, R.color.nav_item_colors));
        bottomNav.setItemRippleColor(ColorStateList.valueOf(getResources().getColor(R.color.ripple_primary_light, getTheme())));

        // Đổi màu logo và tiêu đề về màu mặc định
        if (appLogo != null) {
            appLogo.setImageTintList(AppCompatResources.getColorStateList(this, R.color.main_blue));
        }
        if (appTitle != null) {
            appTitle.setTextColor(getResources().getColor(R.color.main_blue, getTheme()));
        }
        if (notificationButton != null) {
            notificationButton.setImageTintList(AppCompatResources.getColorStateList(this, R.color.main_blue));
        }

        setupBottomNavigation();
        bottomNav.setSelectedItemId(R.id.nav_home);
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
            } else if (intent.getBooleanExtra("open_order_detail", false)) {
                if (sellerMode) {
                    openBuyerChannel();
                }
                bottomNav.setSelectedItemId(R.id.nav_history);
                String orderId = intent.getStringExtra("order_id");
                if (orderId != null && !orderId.trim().isEmpty()) {
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, OrderDetailFragment.newInstance(orderId))
                            .addToBackStack(null)
                            .commit();
                }
            } else if (intent.getBooleanExtra("open_admin_product_detail", false)) {
                openAdminProductFromIntent(intent);
            } else if (intent.getBooleanExtra("open_admin_order_detail", false)) {
                openAdminOrderFromIntent(intent);
            } else if (intent.getBooleanExtra("open_seller_dashboard", false)) {
                openSellerChannel();
            }
        }
    }

    private void openAdminProductFromIntent(Intent intent) {
        String productId = intent.getStringExtra("product_id");
        if (productId == null || productId.trim().isEmpty()) {
            Toast.makeText(this, "Không tìm thấy sản phẩm", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!sellerMode) {
            openSellerChannel();
        }
        bottomNav.setSelectedItemId(R.id.nav_seller_profile); // Chuyển sang tab Gian hàng (hoặc giữ nguyên tab hiện tại)


        SessionManager sessionManager = new SessionManager(this);
        String shopId = sessionManager.getCurrentShopId();
        if (shopId == null || shopId.trim().isEmpty()) {
            Toast.makeText(this, "Chưa chọn gian hàng", Toast.LENGTH_SHORT).show();
            return;
        }

        SupabaseApi apiService = RetrofitClient.getApiService();
        apiService.getShopProducts(shopId).enqueue(new Callback<List<Products>>() {
            @Override
            public void onResponse(Call<List<Products>> call, Response<List<Products>> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(MainActivity.this, "Không tải được sản phẩm", Toast.LENGTH_SHORT).show();
                    return;
                }
                for (Products product : response.body()) {
                    if (product != null && productId.equals(product.id)) {
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.fragment_container, SellerProductFormFragment.newInstance(product))
                                .addToBackStack(null)
                                .commit();
                        return;
                    }
                }
                Toast.makeText(MainActivity.this, "Không tìm thấy sản phẩm trong gian hàng", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Call<List<Products>> call, Throwable throwable) {
                Toast.makeText(MainActivity.this, "Lỗi kết nối khi tải sản phẩm", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openAdminOrderFromIntent(Intent intent) {
        String orderId = intent.getStringExtra("order_id");
        if (orderId == null || orderId.trim().isEmpty()) {
            Toast.makeText(this, "Không tìm thấy đơn hàng", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!sellerMode) {
            openSellerChannel();
        }
        bottomNav.setSelectedItemId(R.id.nav_seller_profile); // Chuyển sang tab Gian hàng


        SessionManager sessionManager = new SessionManager(this);
        String shopId = sessionManager.getCurrentShopId();
        if (shopId == null || shopId.trim().isEmpty()) {
            Toast.makeText(this, "Chưa chọn gian hàng", Toast.LENGTH_SHORT).show();
            return;
        }

        SupabaseApi apiService = RetrofitClient.getApiService();
        apiService.getShopOrders(shopId).enqueue(new Callback<List<Orders>>() {
            @Override
            public void onResponse(Call<List<Orders>> call, Response<List<Orders>> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(MainActivity.this, "Không tải được đơn hàng", Toast.LENGTH_SHORT).show();
                    return;
                }
                for (Orders order : response.body()) {
                    if (order != null && orderId.equals(order.id)) {
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.fragment_container, AdminOrderDetailFragment.newInstance(order, shopId))
                                .addToBackStack(null)
                                .commit();
                        return;
                    }
                }
                Toast.makeText(MainActivity.this, "Không tìm thấy đơn hàng trong gian hàng", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Call<List<Orders>> call, Throwable throwable) {
                Toast.makeText(MainActivity.this, "Lỗi kết nối khi tải đơn hàng", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadNotifications() {
        RetrofitClient.getApiService().getNotifications(10).enqueue(new Callback<List<AppNotification>>() {
            @Override
            public void onResponse(Call<List<AppNotification>> call, Response<List<AppNotification>> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    return;
                }

                notifications.clear();
                notifications.addAll(response.body());
                updateNotificationBadge();
            }

            @Override
            public void onFailure(Call<List<AppNotification>> call, Throwable throwable) {
                Toast.makeText(MainActivity.this, "Cannot load notifications", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateNotificationBadge() {
        int unreadCount = 0;
        for (AppNotification item : notifications) {
            if (!item.isRead) unreadCount++;
        }

        if (unreadCount > 0) {
            notificationBadge.setVisibility(View.VISIBLE);
            notificationBadge.setText(String.valueOf(unreadCount));
        } else {
            notificationBadge.setVisibility(View.GONE);
        }
    }

    private void showShortNotificationPopup() {
        loadNotifications();
        View popupView = LayoutInflater.from(this)
                .inflate(R.layout.popup_notifications, null, false);

        RecyclerView recyclerView = popupView.findViewById(R.id.rv_short_notifications);
        TextView moreButton = popupView.findViewById(R.id.tv_more_notifications);
        TextView markAllRead = popupView.findViewById(R.id.tv_mark_all_read);

        List<AppNotification> shortList = notifications.size() > 5
                ? notifications.subList(0, 5)
                : notifications;

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        PopupWindow popupWindow = new PopupWindow(
                popupView,
                (int) (getResources().getDisplayMetrics().widthPixels * 0.9), // 90% màn hình
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true
        );

        recyclerView.setAdapter(new NotificationAdapter(shortList, notification -> {
            popupWindow.dismiss();
            handleNotificationClick(notification);
        }));

        popupWindow.setOutsideTouchable(true);
        popupWindow.setElevation(16f); // Bo góc và đổ bóng sâu hơn

        markAllRead.setOnClickListener(v -> {
            markAllNotificationsRead();
            popupWindow.dismiss();
        });

        moreButton.setOnClickListener(v -> {
            popupWindow.dismiss();
            startActivity(new Intent(this, NotificationCenterActivity.class));
        });

        // Animation mờ nền nhẹ
        View rootView = getWindow().getDecorView().getRootView();
        float originalAlpha = 1.0f;
        
        popupWindow.setOnDismissListener(() -> {
            // Restore alpha
        });

        popupWindow.showAsDropDown(notificationButton, 0, 10, Gravity.END);
    }

    private void markAllNotificationsRead() {
        for (AppNotification n : notifications) {
            n.isRead = true;
        }
        updateNotificationBadge();
        
        RetrofitClient.getApiService().markAllNotificationsRead().enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    loadNotifications(); // Reload to sync
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                // Silent fail
            }
        });
    }

    private void handleNotificationClick(AppNotification notification) {
        if (notification == null) {
            return;
        }

        markNotificationRead(notification);

        if (notification.orderId == null || notification.orderId.trim().isEmpty()) {
            Toast.makeText(this, "This notification is not linked to an order", Toast.LENGTH_SHORT).show();
            return;
        }

        if (sellerMode) {
            openBuyerChannel();
        }

        bottomNav.setSelectedItemId(R.id.nav_history);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, OrderDetailFragment.newInstance(notification.orderId))
                .addToBackStack(null)
                .commit();
    }

    private void markNotificationRead(AppNotification notification) {
        if (notification == null || notification.id == null || notification.id.trim().isEmpty()) {
            return;
        }

        boolean wasUnread = !notification.isRead;

        if (wasUnread) {
            notification.isRead = true;
            updateNotificationBadge();
        }

        RetrofitClient.getApiService().markNotificationRead(notification.id).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                // Local UI is already updated.
            }

            @Override
            public void onFailure(Call<Void> call, Throwable throwable) {
                if (wasUnread) {
                    notification.isRead = false;
                    updateNotificationBadge();
                }

                Toast.makeText(MainActivity.this, "Cannot mark notification as read", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
