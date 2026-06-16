package com.example.medictown.ui.profile;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;


import com.bumptech.glide.Glide;

import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.example.medictown.R;

import com.example.medictown.data.models.Users;
import com.example.medictown.data.repositories.ProfileRepository;
import com.example.medictown.databinding.ActivityProfileDetailBinding;

import org.json.JSONObject;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
public class ProfileDetailActivity extends AppCompatActivity {
    private ActivityProfileDetailBinding binding;
    private ProfileRepository repository;
    private ActivityResultLauncher<String> imagePickerLauncher;
    public Uri imageUri;
    public String exportedurl;

    public Users user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityProfileDetailBinding.inflate(
                getLayoutInflater()
        );
        setContentView(binding.getRoot());

        repository = new ProfileRepository();

        setupImagePicker();
        setupButtons();
        loadCurrentUser();
    }

    private void setupImagePicker() {
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                selectedUri -> {
                    if (selectedUri == null) return;

                    imageUri = selectedUri;

                    Glide.with(this)
                            .load(selectedUri)
                            .placeholder(R.drawable.ic_profile)
                            .error(R.drawable.ic_profile)
                            .transform(new CenterCrop(), new RoundedCorners(24))
                            .into(binding.ivAvatar);
                }
        );

        binding.ivAvatar.setOnClickListener(v ->
                imagePickerLauncher.launch("image/*")
        );
    }

    private void uploadAvatarThenSave() {
        repository.uploadToSupabase(
                this,
                imageUri,
                new okhttp3.Callback() {
                    @Override
                    public void onFailure(
                            @NonNull okhttp3.Call call,
                            @NonNull IOException exception
                    ) {
                        runOnUiThread(() -> {
                            setSaving(false);
                            Toast.makeText(
                                    ProfileDetailActivity.this,
                                    exception.getMessage(),
                                    Toast.LENGTH_LONG
                            ).show();
                        });
                    }

                    @Override
                    public void onResponse(
                            @NonNull okhttp3.Call call,
                            @NonNull okhttp3.Response response
                    ) {
                        try (response) {
                            String body = response.body() == null
                                    ? ""
                                    : response.body().string();

                            if (!response.isSuccessful()) {
                                runOnUiThread(() -> {
                                    setSaving(false);
                                    Toast.makeText(
                                            ProfileDetailActivity.this,
                                            "Tải ảnh thất bại: HTTP " + response.code(),
                                            Toast.LENGTH_LONG
                                    ).show();
                                });
                                return;
                            }

                            JSONObject jsonObject = new JSONObject(body);
                            exportedurl = jsonObject.getString("url");
                            user.avatar_url = exportedurl;

                            runOnUiThread(this::saveUser);
                        } catch (Exception exception) {
                            runOnUiThread(() -> {
                                setSaving(false);
                                Toast.makeText(
                                        ProfileDetailActivity.this,
                                        "Phản hồi tải ảnh không hợp lệ",
                                        Toast.LENGTH_LONG
                                ).show();
                            });
                        }
                    }

                    private void saveUser() {
                        ProfileDetailActivity.this.saveUser();
                    }
                }
        );
    }
    private void setupButtons(){
        binding.toolbar.setNavigationOnClickListener(v -> {
            finish();
        });

        binding.btnSave.setOnClickListener(v -> {
            if (user == null) {
                Toast.makeText(
                        ProfileDetailActivity.this,
                        "Hồ sơ chưa được tải",
                        Toast.LENGTH_SHORT
                ).show();
                return;
            }

            String name = binding.etFullName
                    .getText()
                    .toString()
                    .trim();

            String phone = binding.etPhone
                    .getText()
                    .toString()
                    .trim();

            if (name.isEmpty()) {
                binding.etFullName.setError("Vui lòng nhập họ tên");
                return;
            }

            user.name = name;
            user.phone = phone;

            setSaving(true);

            if (imageUri == null) {
                saveUser();
                return;
            }

            uploadAvatarThenSave();
        });
    }

    private void saveUser() {
        repository.setUser(user, new Callback<Void>() {
            @Override
            public void onResponse(
                    Call<Void> call,
                    Response<Void> response
            ) {
                if (response.isSuccessful()) {
                    Toast.makeText(
                            ProfileDetailActivity.this,
                            "Đã cập nhật hồ sơ",
                            Toast.LENGTH_SHORT
                    ).show();
                    finish();
                    return;
                }

                setSaving(false);
                Toast.makeText(
                        ProfileDetailActivity.this,
                        "Cập nhật thất bại: HTTP " + response.code(),
                        Toast.LENGTH_LONG
                ).show();
            }

            @Override
            public void onFailure(
                    Call<Void> call,
                    Throwable throwable
            ) {
                setSaving(false);
                Toast.makeText(
                        ProfileDetailActivity.this,
                        "Lỗi kết nối: " + throwable.getMessage(),
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }

    private void setSaving(boolean saving) {
        binding.btnSave.setEnabled(!saving);
        binding.ivAvatar.setEnabled(!saving);
        binding.btnSave.setText(
                saving ? "Đang lưu..." : "Lưu thay đổi"
        );
    }

    private void loadCurrentUser() {
        binding.btnSave.setEnabled(false);

        repository.getCurrentUser(new Callback<Users>() {
            @Override
            public void onResponse(
                    Call<Users> call,
                    Response<Users> response
            ) {
                if (!response.isSuccessful() || response.body() == null) {
                    binding.btnSave.setEnabled(false);

                    Toast.makeText(
                            ProfileDetailActivity.this,
                            "Không thể tải hồ sơ: HTTP " + response.code(),
                            Toast.LENGTH_LONG
                    ).show();
                    return;
                }

                user = response.body();

                binding.etFullName.setText(user.name);
                binding.etPhone.setText(user.phone);

                if (user.avatar_url != null
                        && !user.avatar_url.trim().isEmpty()) {
                    exportedurl = user.avatar_url;

                    Glide.with(ProfileDetailActivity.this)
                            .load(user.avatar_url)
                            .placeholder(R.drawable.ic_profile)
                            .error(R.drawable.ic_profile)
                            .transform(
                                    new CenterCrop(),
                                    new RoundedCorners(24)
                            )
                            .into(binding.ivAvatar);
                } else {
                    binding.ivAvatar.setImageResource(
                            R.drawable.ic_profile
                    );
                }

                binding.btnSave.setEnabled(true);
            }

            @Override
            public void onFailure(
                    Call<Users> call,
                    Throwable throwable
            ) {
                binding.btnSave.setEnabled(false);

                Toast.makeText(
                        ProfileDetailActivity.this,
                        "Lỗi kết nối: " + throwable.getMessage(),
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }
}
