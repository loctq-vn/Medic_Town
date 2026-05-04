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
import com.example.medictown.R;
import com.example.medictown.data.api.RetrofitClient;
import com.example.medictown.data.api.SessionManager;
import com.example.medictown.data.api.SupabaseApi;
import com.example.medictown.data.api.SupabaseConfig;
import com.example.medictown.data.models.Users;
import com.example.medictown.databinding.FragmentProfileBinding;
import com.example.medictown.ui.auth.LoginActivity;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private ProfileViewModel mViewModel;
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

        setupClickListeners();
        observeViewModel();
    }

    @Override
    public void onResume() {
        super.onResume();
        SessionManager sessionManager = new SessionManager(getContext());
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

        binding.btnLogout.setOnClickListener(v -> {
            SupabaseApi api = RetrofitClient.getAuthService();
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
