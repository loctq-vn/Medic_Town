package com.example.medictown;

import android.view.View;
import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.example.medictown.data.models.CartItem;
import com.example.medictown.data.api.SessionManager;
import com.example.medictown.data.repositories.ProductRepository;
import com.example.medictown.ui.cart.CartFragment;
import com.example.medictown.ui.history.HistoryFragment;
import com.example.medictown.ui.payment.PaymentFragment;
import com.example.medictown.ui.product.ProductFragment;
import com.example.medictown.ui.profile.ProfileFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;
    private View appBarMain;

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
            // Bỏ padding bottom (set thành 0) vì BottomNavigationView sẽ tự xử lý inset ở dưới
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });


        bottomNav = findViewById(R.id.bottom_navigation);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new ProductFragment())
                    .commit();
        }

        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;

            int itemId = item.getItemId();
            if (itemId == R.id.nav_product) {
                selectedFragment = new ProductFragment();
            } else if (itemId == R.id.nav_cart) {
                selectedFragment = new CartFragment();
            } else if (itemId == R.id.nav_history) {
                selectedFragment = new HistoryFragment();
            } else if (itemId == R.id.nav_profile) {
                selectedFragment = new ProfileFragment();
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .commit();
            }

            return true;
        });

        handleIntent(getIntent());
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

    @Override
    protected void onNewIntent(@NonNull Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (intent != null) {
            if (intent.getBooleanExtra("open_cart", false)) {
                bottomNav.setSelectedItemId(R.id.nav_cart);
            } else if (intent.hasExtra("open_payment")) {
                List<CartItem> items = (List<CartItem>) intent.getSerializableExtra("payment_items");
                if (items != null) {
                    PaymentFragment paymentFragment = PaymentFragment.newInstance(items);
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, paymentFragment)
                            .addToBackStack(null)
                            .commit();
                }
            }
        }
    }
}
