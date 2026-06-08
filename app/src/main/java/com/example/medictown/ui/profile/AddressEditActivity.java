package com.example.medictown.ui.profile;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

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
    private Double selectedLatitude;
    private Double selectedLongitude;
    private String selectedProviderPlaceId;
    private String selectedRawAddress;
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
        binding.btnChooseOnMap.setOnClickListener(v -> {
            Intent pickerIntent = new Intent(AddressEditActivity.this, AddressMapPickerActivity.class);

            if (selectedLatitude != null && selectedLongitude != null) {
                pickerIntent.putExtra("latitude", selectedLatitude);
                pickerIntent.putExtra("longitude", selectedLongitude);
            }

            String currentLocation = binding.location.getText() != null
                    ? binding.location.getText().toString()
                    : "";

            pickerIntent.putExtra("location", currentLocation);

            mapPickerLauncher.launch(pickerIntent);
        });
        binding.btnSave.setOnClickListener(v -> {
            Intent intent = getIntent();
            Address address = new Address();
            address.id = intent.getStringExtra("id");
            address.user_id = intent.getStringExtra("user_id");
            address.location_name = binding.locationName.getText().toString();
            address.recipient_name = binding.recipientName.getText().toString();
            address.phone_number = binding.phoneNumber.getText().toString();
            address.location = binding.location.getText().toString();

            address.latitude = selectedLatitude;
            address.longitude = selectedLongitude;

            if (selectedLatitude != null && selectedLongitude != null) {
                address.map_provider = "osm";
            }

            address.provider_place_id = selectedProviderPlaceId;
            address.raw_address = selectedRawAddress;
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

        binding.btnChooseOnMap.setOnClickListener(v -> {
            Intent intent = new Intent(AddressEditActivity.this, AddressMapPickerActivity.class);

            if (selectedLatitude != null && selectedLongitude != null) {
                intent.putExtra("latitude", selectedLatitude);
                intent.putExtra("longitude", selectedLongitude);
            }

            mapPickerLauncher.launch(intent);
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
        if (intent.hasExtra("latitude")) {
            selectedLatitude = intent.getDoubleExtra("latitude", 0);
        }

        if (intent.hasExtra("longitude")) {
            selectedLongitude = intent.getDoubleExtra("longitude", 0);
        }

        selectedProviderPlaceId = intent.getStringExtra("provider_place_id");
        selectedRawAddress = intent.getStringExtra("raw_address");
    }

    private final ActivityResultLauncher<Intent> mapPickerLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() != Activity.RESULT_OK || result.getData() == null) {
                            return;
                        }

                        Intent data = result.getData();

                        String selectedLocation = data.getStringExtra("location");
                        if (selectedLocation != null && !selectedLocation.trim().isEmpty()) {
                            binding.location.setText(selectedLocation);
                        }

                        if (data.hasExtra("latitude")) {
                            selectedLatitude = data.getDoubleExtra("latitude", 0);
                        }

                        if (data.hasExtra("longitude")) {
                            selectedLongitude = data.getDoubleExtra("longitude", 0);
                        }

                        selectedProviderPlaceId = data.getStringExtra("provider_place_id");
                        selectedRawAddress = data.getStringExtra("raw_address");
                    }
            );
}