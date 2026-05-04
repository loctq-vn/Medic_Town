package com.example.medictown.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medictown.R;
import com.example.medictown.data.models.Address;
import com.example.medictown.data.repositories.ProfileRepository;
import com.example.medictown.databinding.ActivityAddressDetailBinding;
import com.example.medictown.databinding.ActivityProfileDetailBinding;
import com.example.medictown.ui.product.ProductAdapter;
import com.example.medictown.ui.product.ProductDetailActivity;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddressDetailActivity extends AppCompatActivity {
    private ActivityAddressDetailBinding binding;
    private ProfileRepository repository;
    private AddressAdapter adapter;
    public List<Address> addressList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddressDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        this.repository = new ProfileRepository();
        setupRecyclerView();
        setupbutton();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAddresses();
    }
    private void setupbutton() {
        binding.toolbar.setNavigationOnClickListener(v -> finish());
        binding.btnAdd.setOnClickListener(v -> {
            Intent intent = new Intent(AddressDetailActivity.this, AddressEditActivity.class);
            intent.putExtra("user_id", getIntent().getStringExtra("id"));
            startActivity(intent);
        });
    }
    private void setupRecyclerView() {
        adapter = new AddressAdapter();
        binding.rvAddress.setLayoutManager(new LinearLayoutManager(this));
        binding.rvAddress.setAdapter(adapter);

        adapter.setOnAddressClickListener(new AddressAdapter.OnAddressClickListener() {
            @Override
            public void onEditClick(Address address) {
                Intent intent = new Intent(AddressDetailActivity.this, AddressEditActivity.class);
                // Truyền dữ liệu địa chỉ sang màn hình sửa
                intent.putExtra("id", address.id);
                intent.putExtra("user_id", address.user_id);
                intent.putExtra("recipient_name", address.recipient_name);
                intent.putExtra("phone_number", address.phone_number);
                intent.putExtra("location", address.location);
                intent.putExtra("location_name", address.location_name);
                startActivity(intent);
            }

            @Override
            public void onDeleteClick(Address address) {
                new AlertDialog.Builder(AddressDetailActivity.this)
                        .setTitle("Xác nhận xóa")
                        .setMessage("Bạn có chắc chắn muốn xóa địa chỉ này?")
                        .setPositiveButton("Xóa", (dialog, which) -> {
                            repository.deleteAddress(address.id, new Callback<Void>() {
                                @Override
                                public void onResponse(Call<Void> call, Response<Void> response) {
                                    if (response.isSuccessful()) {
                                        Toast.makeText(AddressDetailActivity.this, "Đã xóa địa chỉ", Toast.LENGTH_SHORT).show();
                                        // Tải lại danh sách sau khi xóa
                                        loadAddresses();
                                    }
                                }

                                @Override
                                public void onFailure(Call<Void> call, Throwable t) {
                                    Toast.makeText(AddressDetailActivity.this, "Lỗi khi xóa", Toast.LENGTH_SHORT).show();
                                }
                            });
                        })
                        .setNegativeButton("Hủy", null)
                        .show();
            }
        });
    }

    private void loadAddresses() {
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("id")) {
            repository.getAddress(intent.getStringExtra("id"), new Callback<List<Address>>() {
                @Override
                public void onResponse(Call<List<Address>> call, Response<List<Address>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        addressList = response.body();
                        adapter.setAddressList(addressList);
                    }
                }

                @Override
                public void onFailure(Call<List<Address>> call, Throwable t) {
                    Toast.makeText(AddressDetailActivity.this, "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}