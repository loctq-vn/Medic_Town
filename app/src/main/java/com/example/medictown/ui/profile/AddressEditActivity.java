package com.example.medictown.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.medictown.R;
import com.example.medictown.data.models.Address;
import com.example.medictown.data.repositories.ProfileRepository;
import com.example.medictown.databinding.ActivityAddressDetailBinding;
import com.example.medictown.databinding.ActivityAddressEditBinding;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddressEditActivity extends AppCompatActivity {
    private ActivityAddressEditBinding binding;
    private ProfileRepository repository;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddressEditBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        this.repository = new ProfileRepository();

        setupbutton();
        loadAddresses();
    }
    private void setupbutton() {
        binding.toolbar.setNavigationOnClickListener(v -> finish());
        binding.btnSave.setOnClickListener(v -> {
            Intent intent = getIntent();
            Address address = new Address();
            address.id = intent.getStringExtra("id");
            address.user_id = intent.getStringExtra("user_id");
            address.location_name = binding.locationName.getText().toString();
            address.recipient_name = binding.recipientName.getText().toString();
            address.phone_number = binding.phoneNumber.getText().toString();
            address.location = binding.location.getText().toString();
            if (intent.hasExtra("id")){
                repository.setAddress(address, new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        Toast.makeText(AddressEditActivity.this,"Đã lưu địa chỉ", Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable throwable) {
                        Toast.makeText(AddressEditActivity.this,"Lỗi khi lưu địa chỉ", Toast.LENGTH_SHORT).show();
                    }
                });
            }
            else{
                repository.addAddress(address, new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        Toast.makeText(AddressEditActivity.this,"Đã thêm địa chỉ", Toast.LENGTH_SHORT).show();
                        finish();
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable throwable) {
                        Toast.makeText(AddressEditActivity.this,"Lỗi thêm mới địa chỉ", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
    private void loadAddresses() {
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("id")) {
            binding.locationName.setText(intent.getStringExtra("location_name"));
            binding.recipientName.setText(intent.getStringExtra("recipient_name"));
            binding.phoneNumber.setText(intent.getStringExtra("phone_number"));
            binding.location.setText(intent.getStringExtra("location"));
        }

    }
}