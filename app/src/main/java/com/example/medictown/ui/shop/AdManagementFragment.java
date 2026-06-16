package com.example.medictown.ui.shop;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.medictown.R;
import com.example.medictown.data.api.SessionManager;
import com.example.medictown.data.models.Advertisement;
import com.example.medictown.data.repositories.AdvertisementRepository;
import com.example.medictown.databinding.FragmentAdManagementBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdManagementFragment extends Fragment {
    private static final String[] POSITION_LABELS = {
            "Tất cả vị trí", "Trang chủ", "Banner sản phẩm", "Popup", "Thanh toán"
    };
    private static final String[] POSITION_VALUES = {
            null, "home_banner", "product_list", "popup", "checkout_banner"
    };

    private FragmentAdManagementBinding binding;
    private SellerAdvertisementAdapter adapter;
    private AdvertisementRepository repository;
    private SessionManager sessionManager;
    private final List<Advertisement> allAdvertisements = new ArrayList<>();
    private String searchQuery;
    private String statusFilter;
    private String positionFilter;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        binding = FragmentAdManagementBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        repository = new AdvertisementRepository();
        sessionManager = new SessionManager(requireContext());
        setupList();
        setupSearchAndFilters();
        setupActions();
        loadAdvertisements();
    }

    private void setupList() {
        adapter = new SellerAdvertisementAdapter(
                new SellerAdvertisementAdapter.OnAdvertisementActionListener() {
                    @Override
                    public void onToggle(Advertisement item, boolean enabled) {
                        updateActive(item, enabled);
                    }

                    @Override
                    public void onDetails(Advertisement item) {
                        openAdForm(AdFormFragment.newDetailsInstance(item));
                    }

                    @Override
                    public void onEdit(Advertisement item) {
                        openAdForm(AdFormFragment.newEditInstance(item));
                    }

                    @Override
                    public void onDelete(Advertisement item) {
                        confirmDelete(item);
                    }
                }
        );
        binding.rvAdvertisements.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvAdvertisements.setAdapter(adapter);
    }

    private void setupSearchAndFilters() {
        binding.etAdSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchQuery = normalize(s == null ? null : s.toString());
                applyFilters();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        binding.adStatusChipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            int checkedId = checkedIds.isEmpty() ? R.id.chipAllAds : checkedIds.get(0);
            if (checkedId == R.id.chipRunningAds) statusFilter = "active";
            else if (checkedId == R.id.chipEndingAds) statusFilter = "draft";
            else if (checkedId == R.id.chipPausedAds) statusFilter = "paused";
            else if (checkedId == R.id.chipExpiredAds) statusFilter = "expired";
            else statusFilter = null;
            applyFilters();
        });

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                POSITION_LABELS
        );
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerAdPosition.setAdapter(spinnerAdapter);
        binding.spinnerAdPosition.setOnItemSelectedListener(
                new android.widget.AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(
                            android.widget.AdapterView<?> parent,
                            View selectedView,
                            int position,
                            long id
                    ) {
                        positionFilter = POSITION_VALUES[position];
                        applyFilters();
                    }

                    @Override
                    public void onNothingSelected(android.widget.AdapterView<?> parent) {
                    }
                }
        );
    }

    private void setupActions() {
        View.OnClickListener createListener = v ->
                openAdForm(AdFormFragment.newCreateInstance());
        binding.btnCreateAd.setOnClickListener(createListener);
        binding.fabCreateAd.setOnClickListener(createListener);
    }

    private void loadAdvertisements() {
        if (binding == null || repository == null || sessionManager == null) return;
        String shopId = sessionManager.getCurrentShopId();
        if (shopId == null || shopId.isEmpty()) {
            allAdvertisements.clear();
            updateSummary();
            applyFilters();
            Toast.makeText(requireContext(), "Chưa chọn gian hàng", Toast.LENGTH_SHORT).show();
            return;
        }

        repository.getShopAdvertisements(
                shopId,
                new Callback<List<Advertisement>>() {
                    @Override
                    public void onResponse(
                            Call<List<Advertisement>> call,
                            Response<List<Advertisement>> response
                    ) {
                        if (binding == null) return;
                        if (!response.isSuccessful() || response.body() == null) {
                            Toast.makeText(
                                    requireContext(),
                                    "Không thể tải quảng cáo",
                                    Toast.LENGTH_SHORT
                            ).show();
                            return;
                        }
                        allAdvertisements.clear();
                        allAdvertisements.addAll(response.body());
                        updateSummary();
                        applyFilters();
                    }

                    @Override
                    public void onFailure(Call<List<Advertisement>> call, Throwable throwable) {
                        if (binding == null) return;
                        Toast.makeText(
                                requireContext(),
                                throwable.getMessage(),
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }
        );
    }

    private void applyFilters() {
        if (binding == null || adapter == null) return;
        List<Advertisement> filtered = new ArrayList<>();
        String normalizedQuery = searchQuery == null
                ? null
                : searchQuery.toLowerCase(Locale.ROOT);
        for (Advertisement advertisement : allAdvertisements) {
            if (statusFilter != null && !statusFilter.equals(advertisement.status)) continue;
            if (positionFilter != null && !positionFilter.equals(advertisement.position)) continue;
            if (normalizedQuery != null
                    && !containsIgnoreCase(advertisement.title, normalizedQuery)
                    && !containsIgnoreCase(advertisement.description, normalizedQuery)) {
                continue;
            }
            filtered.add(advertisement);
        }
        adapter.setItems(filtered);
    }

    private boolean containsIgnoreCase(String value, String normalizedQuery) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(normalizedQuery);
    }

    private void updateSummary() {
        int active = 0;
        int paused = 0;
        int expired = 0;
        for (Advertisement advertisement : allAdvertisements) {
            if ("active".equals(advertisement.status)) active++;
            else if ("paused".equals(advertisement.status)) paused++;
            else if ("expired".equals(advertisement.status)) expired++;
        }
        binding.tvRunningCount.setText(String.valueOf(active));
        binding.tvPausedCount.setText(String.valueOf(paused));
        binding.tvExpiredCount.setText(String.valueOf(expired));
    }

    private void updateActive(Advertisement item, boolean enabled) {
        String shopId = sessionManager.getCurrentShopId();
        repository.updateActive(
                shopId,
                item.id,
                enabled,
                new Callback<Advertisement>() {
                    @Override
                    public void onResponse(
                            Call<Advertisement> call,
                            Response<Advertisement> response
                    ) {
                        if (binding == null) return;
                        if (!response.isSuccessful()) {
                            Toast.makeText(
                                    requireContext(),
                                    "Không thể đổi trạng thái quảng cáo",
                                    Toast.LENGTH_SHORT
                            ).show();
                            applyFilters();
                            return;
                        }
                        Advertisement updated = response.body();
                        if (updated != null) replaceAdvertisement(updated);
                        updateSummary();
                        applyFilters();
                    }

                    @Override
                    public void onFailure(Call<Advertisement> call, Throwable throwable) {
                        if (binding == null) return;
                        Toast.makeText(
                                requireContext(),
                                "Không thể đổi trạng thái quảng cáo",
                                Toast.LENGTH_SHORT
                        ).show();
                        applyFilters();
                    }
                }
        );
    }

    private void confirmDelete(Advertisement item) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Xóa quảng cáo")
                .setMessage("Bạn có chắc muốn xóa \"" + item.title + "\"?")
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Xóa", (dialog, which) -> deleteAdvertisement(item))
                .show();
    }

    private void deleteAdvertisement(Advertisement item) {
        repository.delete(
                sessionManager.getCurrentShopId(),
                item.id,
                new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (binding == null) return;
                        if (response.isSuccessful()) {
                            Toast.makeText(
                                    requireContext(),
                                    "Đã xóa quảng cáo",
                                    Toast.LENGTH_SHORT
                            ).show();
                            removeAdvertisement(item.id);
                            updateSummary();
                            applyFilters();
                        } else {
                            Toast.makeText(
                                    requireContext(),
                                    "Không thể xóa quảng cáo",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable throwable) {
                        if (binding == null) return;
                        Toast.makeText(
                                requireContext(),
                                "Không thể xóa quảng cáo",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }
        );
    }

    private void replaceAdvertisement(Advertisement updated) {
        for (int index = 0; index < allAdvertisements.size(); index++) {
            if (sameId(allAdvertisements.get(index).id, updated.id)) {
                allAdvertisements.set(index, updated);
                return;
            }
        }
    }

    private void removeAdvertisement(String advertisementId) {
        allAdvertisements.removeIf(item -> sameId(item.id, advertisementId));
    }

    private boolean sameId(String first, String second) {
        return first != null && first.equals(second);
    }

    private void openAdForm(Fragment fragment) {
        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }

    private String normalize(String value) {
        if (value == null || value.trim().isEmpty()) return null;
        return value.trim();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
