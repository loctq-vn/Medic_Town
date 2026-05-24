package com.example.medictown.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.example.medictown.MainActivity;
import com.example.medictown.R;
import com.example.medictown.data.api.SessionManager;
import com.example.medictown.data.models.Shop;
import com.example.medictown.data.models.Users;
import com.example.medictown.data.repositories.ShopRepository;
import com.example.medictown.databinding.FragmentProfileBinding;
import com.example.medictown.ui.admin.AdminDashboardFragment;
import com.example.medictown.ui.auth.LoginActivity;
import com.example.medictown.ui.shop.SellerRegisterActivity;
import com.example.medictown.ui.shop.ShopSelectionActivity;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private ProfileViewModel mViewModel;
    private ShopRepository shopRepository;
    private SessionManager sessionManager;
    private Users user;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Khởi tạo binding
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        mViewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
        shopRepository = new ShopRepository();
        sessionManager = new SessionManager(requireContext());

        setupClickListeners();
        observeViewModel();
    }

    @Override
    public void onResume() {
        super.onResume();
        mViewModel.fetchUserProfile(sessionManager.getUserId());
    }
    private void setupClickListeners() {
        binding.itemProfile.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), ProfileDetailActivity.class);
            intent.putExtra("id", user.id);
            startActivity(intent);
        });

        binding.itemAddresses.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), AddressDetailActivity.class);
            intent.putExtra("id", user.id);
            startActivity(intent);
        });

        binding.itemOffers.setOnClickListener(v -> {

        });

        binding.itemSupport.setOnClickListener(v -> {

        });

        binding.itemSeller.setOnClickListener(v -> openSellerChannel());

        binding.itemAdmin.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new AdminDashboardFragment())
                    .addToBackStack(null)
                    .commit();
        });

        binding.btnLogout.setOnClickListener(v -> {
            SessionManager sessionManager = new SessionManager(getContext());
            sessionManager.clearSession();
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            if (getActivity() != null) {
                getActivity().finish();
            }
        });
    }

    private void openSellerChannel() {
        binding.itemSeller.setEnabled(false);
        shopRepository.getMyShops(new Callback<List<Shop>>() {
            @Override
            public void onResponse(Call<List<Shop>> call, Response<List<Shop>> response) {
                if (binding == null) return;
                binding.itemSeller.setEnabled(true);
                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(getContext(), "Không thể tải gian hàng", Toast.LENGTH_SHORT).show();
                    return;
                }
                List<Shop> shops = response.body();
                if (shops.isEmpty()) {
                    startActivity(new Intent(getContext(), SellerRegisterActivity.class));
                } else if (shops.size() == 1) {
                    Shop shop = shops.get(0);
                    sessionManager.saveCurrentShop(shop.id, shop.name, shop.logo_url);
                    if (getActivity() instanceof MainActivity) {
                        ((MainActivity) getActivity()).openSellerChannel();
                    }
                } else {
                    startActivity(new Intent(getContext(), ShopSelectionActivity.class));
                }
            }

            @Override
            public void onFailure(Call<List<Shop>> call, Throwable t) {
                if (binding == null) return;
                binding.itemSeller.setEnabled(true);
                Toast.makeText(getContext(), t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void observeViewModel(){
        mViewModel.getUser().observe(getViewLifecycleOwner(),users -> {
            if (users != null) {
                user = users;
                binding.name.setText(user.name);
                binding.etEmail.setText("Email: "+user.email);
                Glide.with(this)
                        .load(user.avatar_url)
                        .placeholder(R.drawable.ic_profile)
                        .transform(new CenterCrop(), new RoundedCorners(24))
                        .into(binding.ivAvatar);

                // Show Admin Dashboard for admin users
                if ("admin".equalsIgnoreCase(user.role) || "manager".equalsIgnoreCase(user.role)) {
                    binding.itemAdmin.setVisibility(View.VISIBLE);
                } else {
                    binding.itemAdmin.setVisibility(View.GONE);
                }
            }
        });
        mViewModel.getErrorMessage().observe(getViewLifecycleOwner(),error -> {
            if (error != null) {
                Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
            }
        });
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
