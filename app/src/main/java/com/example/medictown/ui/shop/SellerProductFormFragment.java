package com.example.medictown.ui.shop;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.medictown.data.api.SessionManager;
import com.example.medictown.data.models.Products;
import com.example.medictown.data.repositories.ShopRepository;
import com.example.medictown.databinding.FragmentSellerProductFormBinding;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SellerProductFormFragment extends Fragment {
    private static final String ARG_PRODUCT = "product";

    private FragmentSellerProductFormBinding binding;
    private ShopRepository repository;
    private SessionManager sessionManager;
    private Products editingProduct;

    public static SellerProductFormFragment newInstance(Products product) {
        SellerProductFormFragment fragment = new SellerProductFormFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_PRODUCT, product);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSellerProductFormBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        repository = new ShopRepository();
        sessionManager = new SessionManager(requireContext());

        if (getArguments() != null) {
            editingProduct = (Products) getArguments().getSerializable(ARG_PRODUCT);
        }

        binding.cbActive.setChecked(true);
        if (editingProduct != null) {
            bindProduct(editingProduct);
            binding.btnAddProduct.setText("Lưu thay đổi");
        }

        binding.btnBack.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());
        binding.btnAddProduct.setOnClickListener(v -> submitProduct());
    }

    private void bindProduct(Products product) {
        binding.etName.setText(product.name);
        binding.etBrand.setText(product.brand);
        binding.etManufacturer.setText(product.manufacturer);
        binding.etPrice.setText(String.valueOf(product.price));
        binding.etSalePrice.setText(product.sale_price != null ? String.valueOf(product.sale_price) : "");
        binding.etStock.setText(String.valueOf(product.stock));
        binding.etImages.setText(product.images != null ? String.join("\n", product.images) : "");
        binding.etDescription.setText(product.description);
        binding.etUsage.setText(product.usage);
        binding.etIndications.setText(product.indications);
        binding.etContraindications.setText(product.contraindications);
        binding.cbRequiresPrescription.setChecked(product.requires_prescription);
        binding.cbFeatured.setChecked(product.is_featured);
        binding.cbBestSeller.setChecked(product.is_best_seller);
        binding.cbActive.setChecked(product.is_active);
    }

    private void submitProduct() {
        String shopId = sessionManager.getCurrentShopId();
        if (shopId == null || shopId.isEmpty()) {
            Toast.makeText(getContext(), "Chưa chọn gian hàng", Toast.LENGTH_SHORT).show();
            return;
        }

        Products product = buildProductFromForm();
        if (product == null) return;

        binding.progressBar.setVisibility(View.VISIBLE);
        binding.btnAddProduct.setEnabled(false);

        Callback<Products> callback = new Callback<Products>() {
            @Override
            public void onResponse(Call<Products> call, Response<Products> response) {
                if (binding == null) return;
                binding.progressBar.setVisibility(View.GONE);
                binding.btnAddProduct.setEnabled(true);
                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(getContext(), buildErrorMessage(response), Toast.LENGTH_LONG).show();
                    return;
                }
                Toast.makeText(
                        getContext(),
                        editingProduct == null ? "Đã thêm sản phẩm" : "Đã cập nhật sản phẩm",
                        Toast.LENGTH_SHORT
                ).show();
                requireActivity().getSupportFragmentManager().popBackStack();
            }

            @Override
            public void onFailure(Call<Products> call, Throwable t) {
                if (binding == null) return;
                binding.progressBar.setVisibility(View.GONE);
                binding.btnAddProduct.setEnabled(true);
                Toast.makeText(getContext(), t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        };

        if (editingProduct == null) {
            repository.createProduct(shopId, product, callback);
        } else {
            repository.updateProduct(shopId, editingProduct.id, product, callback);
        }
    }

    private Products buildProductFromForm() {
        String name = text(binding.etName);
        if (name.isEmpty()) {
            binding.etName.setError("Vui lòng nhập tên sản phẩm");
            return null;
        }

        Double price = parseDouble(text(binding.etPrice));
        if (price == null) {
            binding.etPrice.setError("Giá không hợp lệ");
            return null;
        }

        Integer stock = parseInt(text(binding.etStock));
        if (stock == null) {
            binding.etStock.setError("Tồn kho không hợp lệ");
            return null;
        }

        String salePriceText = text(binding.etSalePrice);
        Double salePrice = parseOptionalDouble(salePriceText);
        if (!salePriceText.isEmpty() && salePrice == null) {
            binding.etSalePrice.setError("Giá sale không hợp lệ");
            return null;
        }

        Products product = new Products();
        product.name = name;
        product.brand = emptyToNull(text(binding.etBrand));
        product.manufacturer = emptyToNull(text(binding.etManufacturer));
        product.price = price;
        product.sale_price = salePrice;
        product.stock = stock;
        product.images = parseImages(text(binding.etImages));
        product.description = emptyToNull(text(binding.etDescription));
        product.usage = emptyToNull(text(binding.etUsage));
        product.indications = emptyToNull(text(binding.etIndications));
        product.contraindications = emptyToNull(text(binding.etContraindications));
        product.requires_prescription = binding.cbRequiresPrescription.isChecked();
        product.is_featured = binding.cbFeatured.isChecked();
        product.is_best_seller = binding.cbBestSeller.isChecked();
        product.is_active = binding.cbActive.isChecked();
        return product;
    }

    private String buildErrorMessage(Response<Products> response) {
        String body = null;
        try {
            body = response.errorBody() != null ? response.errorBody().string() : null;
        } catch (IOException ignored) {
        }
        if (body == null || body.trim().isEmpty()) {
            return "Không thể lưu sản phẩm: HTTP " + response.code();
        }
        return "Không thể lưu sản phẩm: HTTP " + response.code() + " - " + body;
    }

    private String text(android.widget.EditText editText) {
        return editText.getText().toString().trim();
    }

    private String emptyToNull(String value) {
        return value.isEmpty() ? null : value;
    }

    private Double parseDouble(String value) {
        try {
            double parsed = Double.parseDouble(value);
            return parsed >= 0 ? parsed : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Double parseOptionalDouble(String value) {
        return value.isEmpty() ? null : parseDouble(value);
    }

    private Integer parseInt(String value) {
        try {
            int parsed = Integer.parseInt(value);
            return parsed >= 0 ? parsed : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private List<String> parseImages(String value) {
        List<String> images = new ArrayList<>();
        if (value.isEmpty()) return images;
        String[] parts = value.split("[,\\n]");
        for (String part : parts) {
            String url = part.trim();
            if (!url.isEmpty()) {
                images.add(url);
            }
        }
        return images;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
