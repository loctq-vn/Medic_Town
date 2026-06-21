package com.example.medictown.ui.auth;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.medictown.MainActivity;
import com.example.medictown.R;
import com.example.medictown.data.api.RetrofitClient;
import com.example.medictown.data.api.SessionManager;
import com.example.medictown.data.api.SupabaseApi;
import com.example.medictown.data.models.AuthRequest;
import com.example.medictown.data.models.AuthResponse;
import com.example.medictown.data.models.GoogleAuthRequest;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.example.medictown.notifications.NotificationTokenManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private LinearLayout btnGoogle;
    private TextView tvRegister, tvForgotPassword;
    private SessionManager sessionManager;
    private GoogleSignInClient mGoogleSignInClient;
    private ActivityResultLauncher<Intent> googleSignInLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        sessionManager = new SessionManager(this);


        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnGoogle = findViewById(R.id.btnGoogle);
        tvRegister = findViewById(R.id.tvRegister);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);

        mGoogleSignInClient = GoogleSignInConfig.createClient(this);

        // Initialize ActivityResultLauncher
        googleSignInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    Intent data = result.getData();
                    if (data != null) {
                        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                        handleSignInResult(task);
                    } else if (result.getResultCode() != RESULT_OK) {
                        Toast.makeText(this, "Không thể mở đăng nhập Google. Vui lòng kiểm tra cấu hình Google.", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        btnLogin.setOnClickListener(v -> loginUser());

        btnGoogle.setOnClickListener(v -> {
            mGoogleSignInClient.signOut().addOnCompleteListener(this, task -> {
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                googleSignInLauncher.launch(signInIntent);
            });
        });

        tvRegister.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });

        tvForgotPassword.setOnClickListener(v -> {
            Toast.makeText(this, "Chức năng này đang được phát triển", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (v instanceof EditText) {
                Rect outRect = new Rect();
                v.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int) event.getRawX(), (int) event.getRawY())) {
                    v.clearFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        }
        return super.dispatchTouchEvent(event);
    }

    private void loginUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        SupabaseApi authService = RetrofitClient.getAuthService();
        AuthRequest loginRequest = new AuthRequest(email, password);

        authService.login(loginRequest).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    AuthResponse authResponse = response.body();
                    sessionManager.saveSession(
                            authResponse.getAccessToken(),
                            authResponse.getUser().getId(),
                            authResponse.getUser().getEmail()

                    );

// Must run after saveSession because the endpoint requires Bearer authentication.
                    NotificationTokenManager.registerCurrentToken(LoginActivity.this);

                    Toast.makeText(
                            LoginActivity.this,
                            "Đăng nhập thành công!",
                            Toast.LENGTH_SHORT
                    ).show();

                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    finish();
                } else {
                    Toast.makeText(LoginActivity.this, "Đăng nhập thất bại. Vui lòng kiểm tra lại!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                Toast.makeText(LoginActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            String idToken = account.getIdToken();
            if (idToken != null) {
                loginWithSupabaseGoogle(idToken);
            } else {
                Toast.makeText(this, "Không lấy được Google ID token. Vui lòng kiểm tra Web Client ID.", Toast.LENGTH_SHORT).show();
            }
        } catch (ApiException e) {
            Toast.makeText(this, "Google Sign-In failed: " + e.getStatusCode(), Toast.LENGTH_SHORT).show();
        }
    }

    private void loginWithSupabaseGoogle(String idToken) {
        SupabaseApi authService = RetrofitClient.getAuthService();
        GoogleAuthRequest request = new GoogleAuthRequest(idToken);

        authService.loginWithGoogle(request).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    AuthResponse authResponse = response.body();
                    sessionManager.saveSession(
                            authResponse.getAccessToken(),
                            authResponse.getUser().getId(),
                            authResponse.getUser().getEmail()
                    );

                    NotificationTokenManager.registerCurrentToken(LoginActivity.this);

                    Toast.makeText(
                            LoginActivity.this,
                            "Đăng nhập Google thành công!",
                            Toast.LENGTH_SHORT
                    ).show();

                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    finish();
                } else {
                    Toast.makeText(LoginActivity.this, "Xác thực với Supabase thất bại!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                Toast.makeText(LoginActivity.this, "Lỗi kết nối Supabase: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
