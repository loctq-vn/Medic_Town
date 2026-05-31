package com.example.medictown.ui.shop;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.MotionEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.content.Context;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medictown.MainActivity;
import com.example.medictown.R;
import com.example.medictown.data.api.SessionManager;
import com.example.medictown.data.models.ProductCategory;
import com.example.medictown.data.models.ProductSubcategory;
import com.example.medictown.data.models.Products;
import com.example.medictown.data.repositories.ShopRepository;
import com.example.medictown.databinding.FragmentSellerProductFormBinding;
import com.example.medictown.ui.product.ProductImageAdapter;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SellerProductFormFragment extends Fragment {
    private static final String ARG_PRODUCT = "product";
    private static final int MIN_PRODUCT_NAME_LENGTH = 3;
    private static final int MAX_PRODUCT_NAME_LENGTH = 120;
    private static final int MAX_PRODUCT_TEXT_LENGTH = 2000;

    private FragmentSellerProductFormBinding binding;
    private ShopRepository repository;
    private SessionManager sessionManager;
    private Products editingProduct;
    private final List<ProductCategory> categories = new ArrayList<>();
    private final List<ProductSubcategory> allSubcategories = new ArrayList<>();
    private final List<ProductSubcategory> visibleSubcategories = new ArrayList<>();
    private ArrayAdapter<ProductCategory> categoryAdapter;
    private ArrayAdapter<ProductSubcategory> subcategoryAdapter;
    private ProductImageAdapter productImageAdapter;
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private final List<String> productImageUrls = new ArrayList<>();
    private boolean categoriesLoaded = false;
    private boolean subcategoriesLoaded = false;
    private boolean productImageUploading = false;

    public static SellerProductFormFragment newInstance(Products product) {
        SellerProductFormFragment fragment = new SellerProductFormFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_PRODUCT, product);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        if (imageUri != null) {
                            uploadSelectedProductImage(imageUri);
                        }
                    }
                }
        );
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
        if (requireActivity() instanceof MainActivity) {
            ((MainActivity) requireActivity()).setNavBarsVisibility(false);
        }

        if (getArguments() != null) {
            editingProduct = (Products) getArguments().getSerializable(ARG_PRODUCT);
        }

        setupProductImages();
        binding.cbActive.setChecked(true);
        if (editingProduct != null) {
            bindProduct(editingProduct);
            binding.tvFormTitle.setText("Cập nhật sản phẩm");
            binding.btnAddProduct.setText("Lưu thay đổi");
        }

        setupCategorySpinners();
        loadCategories();
        loadSubcategories();

        binding.btnBack.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());
        binding.btnCancelProduct.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());
        binding.btnAddProduct.setOnClickListener(v -> submitProduct());
        binding.cbRequiresPrescription.setOnCheckedChangeListener((buttonView, isChecked) -> updatePrescriptionStyle(isChecked));
        updatePrescriptionStyle(binding.cbRequiresPrescription.isChecked());
        setupFormFocusClearing();
    }

    private void setupFormFocusClearing() {
        clearTextFocusWhenTouchingNonInput(binding.getRoot());
        binding.layoutImageUploadArea.setOnClickListener(v -> {
            clearFormFocus();
            openGallery();
        });
    }

    private void clearTextFocusWhenTouchingNonInput(View view) {
        if (!(view instanceof EditText)) {
            view.setOnTouchListener((touchedView, event) -> {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    clearFormFocus();
                }
                return false;
            });
        }

        if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                clearTextFocusWhenTouchingNonInput(viewGroup.getChildAt(i));
            }
        }
    }

    private void clearFormFocus() {
        if (binding == null) return;
        binding.etName.clearFocus();
        binding.etBrand.clearFocus();
        binding.etManufacturer.clearFocus();
        binding.etPrice.clearFocus();
        binding.etSalePrice.clearFocus();
        binding.etStock.clearFocus();
        binding.etUnit.clearFocus();
        binding.etUses.clearFocus();
        binding.etUsage.clearFocus();
        binding.etSideEffects.clearFocus();
        binding.etPrecautions.clearFocus();
        binding.etStorage.clearFocus();
        binding.spCategory.clearFocus();
        binding.spSubcategory.clearFocus();

        View focusedView = requireActivity().getCurrentFocus();
        if (focusedView != null) {
            InputMethodManager inputMethodManager =
                    (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(focusedView.getWindowToken(), 0);
        }
    }

    private void updatePrescriptionStyle(boolean requiresPrescription) {
        if (binding == null) return;
        binding.layoutPrescriptionChip.setBackgroundResource(
                requiresPrescription
                        ? com.example.medictown.R.drawable.bg_prescription_chip_on
                        : com.example.medictown.R.drawable.bg_prescription_chip
        );
        binding.tvPrescriptionLabel.setTextColor(ContextCompat.getColor(
                requireContext(),
                requiresPrescription
                        ? com.example.medictown.R.color.error
                        : com.example.medictown.R.color.prescription_off_text
        ));
    }

    private void setupProductImages() {
        productImageAdapter = new ProductImageAdapter(imageUrl -> {
        });
        productImageAdapter.setShowRemoveButton(true);
        productImageAdapter.setOnImageRemoveListener((position, imageUrl) -> {
            if (position < 0 || position >= productImageUrls.size()) {
                return;
            }
            productImageUrls.remove(position);
            productImageAdapter.removeImageAt(position);
            syncProductImagesField();
            Toast.makeText(getContext(), "Đã xóa ảnh sản phẩm", Toast.LENGTH_SHORT).show();
        });
        binding.rvSelectedProductImages.setLayoutManager(
                new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        );
        binding.rvSelectedProductImages.setAdapter(productImageAdapter);
        attachProductImageDragHelper();
        updateProductImagesField();
    }

    private void attachProductImageDragHelper() {
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT,
                0
        ) {
            @Override
            public boolean onMove(
                    @NonNull RecyclerView recyclerView,
                    @NonNull RecyclerView.ViewHolder viewHolder,
                    @NonNull RecyclerView.ViewHolder target
            ) {
                int fromPosition = viewHolder.getBindingAdapterPosition();
                int toPosition = target.getBindingAdapterPosition();
                if (
                        fromPosition == RecyclerView.NO_POSITION
                                || toPosition == RecyclerView.NO_POSITION
                                || fromPosition >= productImageUrls.size()
                                || toPosition >= productImageUrls.size()
                ) {
                    return false;
                }

                Collections.swap(productImageUrls, fromPosition, toPosition);
                productImageAdapter.moveImage(fromPosition, toPosition);
                syncProductImagesField();
                return true;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            }
        });
        itemTouchHelper.attachToRecyclerView(binding.rvSelectedProductImages);
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePickerLauncher.launch(intent);
    }

    private void uploadSelectedProductImage(Uri imageUri) {
        String shopId = sessionManager.getCurrentShopId();
        if (shopId == null || shopId.isEmpty()) {
            Toast.makeText(getContext(), "Chưa chọn gian hàng", Toast.LENGTH_SHORT).show();
            return;
        }

        productImageUploading = true;
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.btnAddProduct.setEnabled(false);

        repository.uploadProductImage(requireContext(), shopId, imageUri, new okhttp3.Callback() {
            @Override
            public void onFailure(@NonNull okhttp3.Call call, @NonNull IOException e) {
                if (getActivity() == null || binding == null) return;
                requireActivity().runOnUiThread(() -> {
                    if (binding == null) return;
                    productImageUploading = false;
                    binding.progressBar.setVisibility(View.GONE);
                    binding.btnAddProduct.setEnabled(true);
                    Toast.makeText(getContext(), "Không thể tải ảnh sản phẩm", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(@NonNull okhttp3.Call call, @NonNull okhttp3.Response response) throws IOException {
                String responseBody = response.body() != null ? response.body().string() : "";
                if (getActivity() == null || binding == null) return;
                requireActivity().runOnUiThread(() -> {
                    if (binding == null) return;
                    productImageUploading = false;
                    binding.progressBar.setVisibility(View.GONE);
                    binding.btnAddProduct.setEnabled(true);

                    if (!response.isSuccessful()) {
                        Toast.makeText(getContext(), "Không thể tải ảnh sản phẩm", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    try {
                        JSONObject jsonObject = new JSONObject(responseBody);
                        productImageUrls.add(jsonObject.getString("url"));
                        updateProductImagesField();
                        Toast.makeText(getContext(), "Đã thêm ảnh sản phẩm", Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Toast.makeText(getContext(), "Không đọc được đường dẫn ảnh", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void updateProductImagesField() {
        if (binding == null) return;
        binding.etImages.setText(String.join("\n", productImageUrls));
        if (productImageAdapter != null) {
            productImageAdapter.setImages(productImageUrls);
        }
        binding.rvSelectedProductImages.setVisibility(productImageUrls.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private void syncProductImagesField() {
        if (binding == null) return;
        binding.etImages.setText(String.join("\n", productImageUrls));
        binding.rvSelectedProductImages.setVisibility(productImageUrls.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private void setupCategorySpinners() {
        categoryAdapter = new ArrayAdapter<>(
                requireContext(),
                R.layout.item_admin_spinner,
                categories
        );
        categoryAdapter.setDropDownViewResource(R.layout.item_admin_spinner_dropdown);
        binding.spCategory.setAdapter(categoryAdapter);

        subcategoryAdapter = new ArrayAdapter<>(
                requireContext(),
                R.layout.item_admin_spinner,
                visibleSubcategories
        );
        subcategoryAdapter.setDropDownViewResource(R.layout.item_admin_spinner_dropdown);
        binding.spSubcategory.setAdapter(subcategoryAdapter);

        binding.spCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position >= 0 && position < categories.size()) {
                    updateVisibleSubcategories(categories.get(position).id);
                    selectEditingSubcategoryIfNeeded();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                updateVisibleSubcategories(null);
            }
        });

        binding.spSubcategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (editingProduct == null && position >= 0 && position < visibleSubcategories.size()) {
                    binding.cbRequiresPrescription.setChecked(
                            visibleSubcategories.get(position).requires_prescription_default
                    );
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void loadCategories() {
        repository.getProductCategories(new Callback<List<ProductCategory>>() {
            @Override
            public void onResponse(Call<List<ProductCategory>> call, Response<List<ProductCategory>> response) {
                if (binding == null) return;
                categoriesLoaded = true;
                categories.clear();
                if (response.isSuccessful() && response.body() != null) {
                    categories.addAll(response.body());
                } else {
                    Toast.makeText(getContext(), "Khong the tai danh muc", Toast.LENGTH_SHORT).show();
                }
                categoryAdapter.notifyDataSetChanged();
                applyInitialCategorySelection();
            }

            @Override
            public void onFailure(Call<List<ProductCategory>> call, Throwable t) {
                if (binding == null) return;
                categoriesLoaded = true;
                Toast.makeText(getContext(), t.getMessage(), Toast.LENGTH_SHORT).show();
                applyInitialCategorySelection();
            }
        });
    }

    private void loadSubcategories() {
        repository.getProductSubcategories(null, new Callback<List<ProductSubcategory>>() {
            @Override
            public void onResponse(Call<List<ProductSubcategory>> call, Response<List<ProductSubcategory>> response) {
                if (binding == null) return;
                subcategoriesLoaded = true;
                allSubcategories.clear();
                if (response.isSuccessful() && response.body() != null) {
                    allSubcategories.addAll(response.body());
                } else {
                    Toast.makeText(getContext(), "Khong the tai danh muc con", Toast.LENGTH_SHORT).show();
                }
                applyInitialCategorySelection();
            }

            @Override
            public void onFailure(Call<List<ProductSubcategory>> call, Throwable t) {
                if (binding == null) return;
                subcategoriesLoaded = true;
                Toast.makeText(getContext(), t.getMessage(), Toast.LENGTH_SHORT).show();
                applyInitialCategorySelection();
            }
        });
    }

    private void applyInitialCategorySelection() {
        if (!categoriesLoaded || !subcategoriesLoaded || categories.isEmpty()) {
            return;
        }

        if (editingProduct != null && editingProduct.subcategory_id != null) {
            ProductSubcategory productSubcategory = findSubcategory(editingProduct.subcategory_id);
            if (productSubcategory != null) {
                int categoryIndex = findCategoryIndex(productSubcategory.category_id);
                if (categoryIndex >= 0) {
                    binding.spCategory.setSelection(categoryIndex);
                    updateVisibleSubcategories(productSubcategory.category_id);
                    selectEditingSubcategoryIfNeeded();
                    return;
                }
            }
        }

        updateVisibleSubcategories(categories.get(0).id);
    }

    private void updateVisibleSubcategories(String categoryId) {
        visibleSubcategories.clear();
        if (categoryId != null) {
            for (ProductSubcategory subcategory : allSubcategories) {
                if (categoryId.equals(subcategory.category_id)) {
                    visibleSubcategories.add(subcategory);
                }
            }
        }
        subcategoryAdapter.notifyDataSetChanged();
        if (!visibleSubcategories.isEmpty()) {
            binding.spSubcategory.setSelection(0);
        }
    }

    private void selectEditingSubcategoryIfNeeded() {
        if (editingProduct == null || editingProduct.subcategory_id == null) {
            return;
        }
        int subcategoryIndex = findVisibleSubcategoryIndex(editingProduct.subcategory_id);
        if (subcategoryIndex >= 0) {
            binding.spSubcategory.setSelection(subcategoryIndex);
        }
    }

    private ProductSubcategory findSubcategory(String subcategoryId) {
        for (ProductSubcategory subcategory : allSubcategories) {
            if (subcategoryId.equals(subcategory.id)) {
                return subcategory;
            }
        }
        return null;
    }

    private int findCategoryIndex(String categoryId) {
        for (int i = 0; i < categories.size(); i++) {
            if (categoryId.equals(categories.get(i).id)) {
                return i;
            }
        }
        return -1;
    }

    private int findVisibleSubcategoryIndex(String subcategoryId) {
        for (int i = 0; i < visibleSubcategories.size(); i++) {
            if (subcategoryId.equals(visibleSubcategories.get(i).id)) {
                return i;
            }
        }
        return -1;
    }

    private void bindProduct(Products product) {
        binding.etName.setText(product.name);
        binding.etBrand.setText(product.brand);
        binding.etManufacturer.setText(product.manufacturer);
        binding.etPrice.setText(String.valueOf(product.price));
        binding.etSalePrice.setText(product.sale_price != null ? String.valueOf(product.sale_price) : "");
        binding.etUnit.setText(product.unit);
        binding.etStock.setText(String.valueOf(product.stock));
        binding.etImages.setText(product.images != null ? String.join("\n", product.images) : "");
        productImageUrls.clear();
        if (product.images != null) {
            productImageUrls.addAll(product.images);
        }
        updateProductImagesField();
        binding.etUses.setText(product.uses);
        binding.etUsage.setText(product.usage);
        binding.etSideEffects.setText(product.side_effects);
        binding.etPrecautions.setText(product.precautions);
        binding.etStorage.setText(product.storage);
        binding.cbRequiresPrescription.setChecked(product.requires_prescription);
        binding.cbActive.setChecked(product.is_active);
    }

    private void submitProduct() {
        String shopId = sessionManager.getCurrentShopId();
        if (productImageUploading) {
            Toast.makeText(getContext(), "Vui lòng chờ ảnh tải lên xong", Toast.LENGTH_SHORT).show();
            return;
        }
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
        if (name.length() < MIN_PRODUCT_NAME_LENGTH) {
            binding.etName.setError("Tên sản phẩm phải có ít nhất " + MIN_PRODUCT_NAME_LENGTH + " ký tự");
            return null;
        }
        if (name.length() > MAX_PRODUCT_NAME_LENGTH) {
            binding.etName.setError("Tên sản phẩm không được quá " + MAX_PRODUCT_NAME_LENGTH + " ký tự");
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

        if (text(binding.etUnit).length() > 50) {
            binding.etUnit.setError("Don vi khong duoc qua 50 ky tu");
            return null;
        }

        String salePriceText = text(binding.etSalePrice);
        Double salePrice = parseOptionalDouble(salePriceText);
        if (!salePriceText.isEmpty() && salePrice == null) {
            binding.etSalePrice.setError("Giá khuyến mãi không hợp lệ");
            return null;
        }
        if (salePrice != null && salePrice <= 0) {
            binding.etSalePrice.setError("Giá khuyến mãi phải lớn hơn 0");
            return null;
        }
        if (salePrice != null && salePrice > price) {
            binding.etSalePrice.setError("Giá khuyến mãi phải nhỏ hơn hoặc bằng giá bán");
            return null;
        }

        ProductSubcategory selectedSubcategory = getSelectedSubcategory();
        if (selectedSubcategory == null) {
            Toast.makeText(getContext(), "Vui lòng chọn danh mục con", Toast.LENGTH_SHORT).show();
            return null;
        }

        List<String> images = parseImages(text(binding.etImages));
        if (images.isEmpty()) {
            Toast.makeText(getContext(), "Vui lòng thêm ít nhất 1 ảnh sản phẩm", Toast.LENGTH_SHORT).show();
            return null;
        }
        for (String imageUrl : images) {
            if (!isValidImageUrl(imageUrl)) {
                Toast.makeText(getContext(), "Đường dẫn ảnh không hợp lệ: " + imageUrl, Toast.LENGTH_LONG).show();
                return null;
            }
        }

        if (!validateTextLength(binding.etUses, "Công dụng")) return null;
        if (!validateTextLength(binding.etUsage, "Cách dùng")) return null;
        if (!validateTextLength(binding.etSideEffects, "Tác dụng phụ")) return null;
        if (!validateTextLength(binding.etPrecautions, "Lưu ý")) return null;
        if (!validateTextLength(binding.etStorage, "Bảo quản")) return null;

        String uses = text(binding.etUses);
        String usage = text(binding.etUsage);
        String sideEffects = text(binding.etSideEffects);
        String precautions = text(binding.etPrecautions);
        String storage = text(binding.etStorage);

        Products product = new Products();
        product.name = name;
        product.brand = emptyToNull(text(binding.etBrand));
        product.manufacturer = emptyToNull(text(binding.etManufacturer));
        product.price = price;
        product.sale_price = salePrice;
        product.unit = emptyToNull(text(binding.etUnit));
        product.stock = stock;
        product.images = images;
        product.uses = emptyToNull(uses);
        product.usage = emptyToNull(usage);
        product.side_effects = emptyToNull(sideEffects);
        product.precautions = emptyToNull(precautions);
        product.storage = emptyToNull(storage);
        product.subcategory_id = selectedSubcategory.id;
        product.requires_prescription = binding.cbRequiresPrescription.isChecked();
        product.is_featured = false;
        product.is_best_seller = false;
        product.is_active = binding.cbActive.isChecked();
        return product;
    }

    private boolean validateTextLength(android.widget.EditText editText, String label) {
        String value = text(editText);
        if (value.length() > MAX_PRODUCT_TEXT_LENGTH) {
            editText.setError(label + " không được quá " + MAX_PRODUCT_TEXT_LENGTH + " ký tự");
            return false;
        }
        return true;
    }

    private boolean isValidImageUrl(String value) {
        Uri uri = Uri.parse(value);
        String scheme = uri.getScheme();
        String host = uri.getHost();
        return host != null && ("http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme));
    }

    private ProductSubcategory getSelectedSubcategory() {
        int position = binding.spSubcategory.getSelectedItemPosition();
        if (position < 0 || position >= visibleSubcategories.size()) {
            return null;
        }
        return visibleSubcategories.get(position);
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
        if (requireActivity() instanceof MainActivity) {
            ((MainActivity) requireActivity()).setNavBarsVisibility(true);
        }
        binding = null;
    }
}
