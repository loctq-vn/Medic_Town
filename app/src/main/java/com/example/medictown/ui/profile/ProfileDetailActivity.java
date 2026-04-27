package com.example.medictown.ui.profile;

import static com.example.medictown.data.api.SupabaseConfig.BASE_URL;
import static com.example.medictown.data.api.SupabaseConfig.STORAGE_URL;
import static com.example.medictown.data.api.SupabaseConfig.SUPABASE_URL;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;


import com.bumptech.glide.Glide;

import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.example.medictown.R;

import com.example.medictown.data.api.SupabaseConfig;
import com.example.medictown.data.models.Users;
import com.example.medictown.data.repositories.ProfileRepository;
import com.example.medictown.databinding.ActivityProfileDetailBinding;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import org.json.JSONObject;
public class ProfileDetailActivity extends AppCompatActivity {
    private ActivityProfileDetailBinding binding;
    private ProfileRepository repository;
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    public Uri imageUri;
    public String exportedurl;
    private TextView tvUrl;

    public Users user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        imageUri = result.getData().getData();
                        Glide.with(this)
                                .load(imageUri)
                                .transform(new CenterCrop(), new RoundedCorners(24))
                                .into(binding.ivAvatar);
                    }
                }
        );

        this.repository = new ProfileRepository();

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("id")) {
            repository.getUser(intent.getStringExtra("id"), new Callback<Users>() {
                @Override
                public void onResponse(Call<Users> call, Response<Users> response) {
                    if (response.isSuccessful() && response.body() != null){
                        user = response.body();
                        binding.etFullName.setText(user.name);
                        binding.etPhone.setText(user.phone);

                        if (user.avatar_url != null && !user.avatar_url.isEmpty()) {
                            Glide.with(ProfileDetailActivity.this)
                                    .load(user.avatar_url)
                                    .placeholder(R.drawable.ic_profile)
                                    .transform(new CenterCrop(), new RoundedCorners(24))
                                    .into(binding.ivAvatar);
                            exportedurl = user.avatar_url;
                        }
                    }
                }

                @Override
                public void onFailure(Call<Users> call, Throwable throwable) {

                }
            });
        }
        setupbutton();
    }

    private void setupbutton(){
        binding.toolbar.setNavigationOnClickListener(v -> {
            finish();
        });

        binding.btnSave.setOnClickListener(v -> {
            if (exportedurl == user.avatar_url){
                user.name = binding.etFullName.getText().toString();
                user.phone = binding.etPhone.getText().toString();
                saveUser();
                return;
            }
            repository.uploadToSupabase(this,imageUri,new okhttp3.Callback(){
                @Override
                public void onFailure(@NonNull okhttp3.Call call, @NonNull IOException e) {
                    user.name = binding.etFullName.getText().toString();
                    user.phone = binding.etPhone.getText().toString();
                    saveUser();
                }

                @Override
                public void onResponse(@NonNull okhttp3.Call call, @NonNull okhttp3.Response response) throws IOException {
                    String responseBody = response.body().string();
                    try {
                        JSONObject jsonObject = new JSONObject(responseBody);
                        String key = jsonObject.getString("Key");
                        exportedurl = STORAGE_URL + "object/public/" + key;
                        user.name = binding.etFullName.getText().toString();
                        user.phone = binding.etPhone.getText().toString();
                        user.avatar_url = exportedurl;
                        saveUser();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        });
    }

    private void saveUser() {
        repository.setUser(user, new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()){
                    finish();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable throwable) {
                finish();
            }
        });
    }
    public void openGallery(View view) {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePickerLauncher.launch(intent);
    }
}