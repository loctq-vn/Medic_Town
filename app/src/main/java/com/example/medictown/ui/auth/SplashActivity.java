package com.example.medictown.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.animation.AnimationUtils;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;

import com.example.medictown.MainActivity;
import com.example.medictown.R;
import com.example.medictown.data.api.SessionManager;

public class SplashActivity extends AppCompatActivity {

    private static final long MINIMUM_DISPLAY_TIME_MS = 900;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        sessionManager = new SessionManager(this);

        findViewById(R.id.brandContent).startAnimation(
                AnimationUtils.loadAnimation(this, R.anim.pulse_slow)
        );

        handler.postDelayed(this::openDestination, MINIMUM_DISPLAY_TIME_MS);
    }

    private void openDestination() {
        Class<?> destination = sessionManager.isLoggedIn()
                ? MainActivity.class
                : LoginActivity.class;

        Intent intent = new Intent(this, destination);
        intent.addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK |
                        Intent.FLAG_ACTIVITY_CLEAR_TASK
        );

        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }
}