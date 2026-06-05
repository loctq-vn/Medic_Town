package com.example.medictown.ui.admin;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.medictown.R;
import com.example.medictown.data.api.SessionManager;
import com.example.medictown.data.models.ProductCategory;
import com.example.medictown.data.models.ProductSubcategory;
import com.example.medictown.data.models.Products;
import com.example.medictown.ui.shop.SellerProductFormFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AdminInventoryFragment extends Fragment {
    private static final int FILTER_ALL = 0;
    private static final int FILTER_YES = 1;
    private static final int FILTER_NO = 2;

    private static final int STOCK_ALL = 0;
    private static final int STOCK_IN_STOCK = 1;
    private static final int STOCK_LOW = 2;
    private static final int STOCK_OUT = 3;

    private AdminViewModel viewModel;
    private AdminInventoryAdapter adapter;
    private String currentShopId;

    private final List<Products> allProducts = new ArrayList<>();
    private final List<ProductCategory> categories = new ArrayList<>();
    private final List<ProductSubcategory> subcategories = new ArrayList<>();
    private final Map<String, String> subcategoryToCategory = new HashMap<>();

    private String searchQuery = "";
    private String selectedCategoryId = null;
    private boolean filterUncategorized = false;
    private int activeFilter = FILTER_ALL;
    private int prescriptionFilter = FILTER_ALL;
    private int featuredFilter = FILTER_ALL;
    private int bestSellerFilter = FILTER_ALL;
    private int stockFilter = STOCK_ALL;

    private MaterialButton btnFilterCategory;
    private MaterialButton btnFilterActive;
    private MaterialButton btnFilterPrescription;
    private MaterialButton btnFilterFeatured;
    private MaterialButton btnFilterBestSeller;
    private MaterialButton btnFilterStock;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_inventory, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(AdminViewModel.class);
        adapter = new AdminInventoryAdapter();
        adapter.setOnProductActionListener(new AdminInventoryAdapter.OnProductActionListener() {
            @Override
            public void onEditProduct(Products product) {
                openEditProduct(product);
            }

            @Override
            public void onCloneProduct(Products product) {
                requireActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, SellerProductFormFragment.newCloneInstance(product))
                        .addToBackStack(null)
                        .commit();
            }

            @Override
            public void onStopSellingProduct(Products product) {
                confirmStopSelling(product);
            }
        });

        RecyclerView rvInventory = view.findViewById(R.id.rvInventory);
        rvInventory.setLayoutManager(new LinearLayoutManager(getContext()));
        rvInventory.setAdapter(adapter);

        setupLocalFilters(view);
        setupActions(view);
        observeData();

        SessionManager sessionManager = new SessionManager(requireContext());
        currentShopId = sessionManager.getCurrentShopId();
        viewModel.fetchProductTaxonomy();
    }

    private void openEditProduct(Products product) {
        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, SellerProductFormFragment.newInstance(product))
                .addToBackStack(null)
                .commit();
    }

    private void confirmStopSelling(Products product) {
        if (product == null || isBlank(product.id)) {
            Toast.makeText(getContext(), "Kh\u00f4ng t\u00ecm th\u1ea5y s\u1ea3n ph\u1ea9m", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!product.is_active) {
            Toast.makeText(getContext(), "S\u1ea3n ph\u1ea9m \u0111\u00e3 ng\u1eebng b\u00e1n", Toast.LENGTH_SHORT).show();
            return;
        }
        if (isBlank(currentShopId)) {
            Toast.makeText(getContext(), "Ch\u01b0a ch\u1ecdn gian h\u00e0ng", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(requireContext())
                .setTitle("Ng\u1eebng b\u00e1n s\u1ea3n ph\u1ea9m")
                .setMessage("S\u1ea3n ph\u1ea9m s\u1ebd kh\u00f4ng c\u00f2n hi\u1ec3n th\u1ecb \u0111\u1ec3 kh\u00e1ch mua. B\u1ea1n v\u1eabn c\u00f3 th\u1ec3 b\u1eadt b\u00e1n l\u1ea1i trong form ch\u1ec9nh s\u1eeda.")
                .setNegativeButton("H\u1ee7y", null)
                .setPositiveButton("Ng\u1eebng b\u00e1n", (dialog, which) -> {
                    viewModel.updateProductActive(currentShopId, product.id, false);
                    Toast.makeText(getContext(), "\u0110\u00e3 ng\u1eebng b\u00e1n s\u1ea3n ph\u1ea9m", Toast.LENGTH_SHORT).show();
                })
                .show();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (viewModel != null) {
            loadProducts();
        }
    }

    private void setupActions(View view) {
        View btnNewProduct = view.findViewById(R.id.btnNewProduct);
        if (btnNewProduct != null) {
            btnNewProduct.setOnClickListener(v -> {
                requireActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new SellerProductFormFragment())
                        .addToBackStack(null)
                        .commit();
            });
        }
    }

    private void observeData() {
        viewModel.getAllProducts().observe(getViewLifecycleOwner(), products -> {
            allProducts.clear();
            if (products != null) {
                allProducts.addAll(products);
            }
            normalizeCategoryFilter();
            applyLocalFilters();
        });

        viewModel.getProductCategories().observe(getViewLifecycleOwner(), values -> {
            categories.clear();
            if (values != null) {
                categories.addAll(values);
            }
            normalizeCategoryFilter();
            updateFilterLabels();
            applyLocalFilters();
        });

        viewModel.getProductSubcategories().observe(getViewLifecycleOwner(), values -> {
            subcategories.clear();
            subcategoryToCategory.clear();
            if (values != null) {
                subcategories.addAll(values);
                for (ProductSubcategory subcategory : values) {
                    if (!isBlank(subcategory.id) && !isBlank(subcategory.category_id)) {
                        subcategoryToCategory.put(subcategory.id, subcategory.category_id);
                    }
                }
            }
            applyLocalFilters();
        });
    }

    private void loadProducts() {
        if (currentShopId != null && !currentShopId.isEmpty()) {
            viewModel.fetchShopProducts(currentShopId);
        } else {
            viewModel.fetchAllProducts();
        }
    }

    private void setupLocalFilters(View view) {
        btnFilterCategory = view.findViewById(R.id.btnFilterCategory);
        btnFilterActive = view.findViewById(R.id.btnFilterActive);
        btnFilterPrescription = view.findViewById(R.id.btnFilterPrescription);
        btnFilterFeatured = view.findViewById(R.id.btnFilterFeatured);
        btnFilterBestSeller = view.findViewById(R.id.btnFilterBestSeller);
        btnFilterStock = view.findViewById(R.id.btnFilterStock);

        TextInputEditText etSearch = view.findViewById(R.id.etSearch);
        if (etSearch != null) {
            etSearch.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    searchQuery = s == null ? "" : s.toString().trim().toLowerCase(Locale.ROOT);
                    applyLocalFilters();
                }

                @Override
                public void afterTextChanged(Editable s) {
                }
            });
        }

        btnFilterCategory.setOnClickListener(v -> showCategoryMenu());
        btnFilterActive.setOnClickListener(v -> showThreeStateMenu(
                btnFilterActive,
                new String[]{"Trạng thái", "Đang bán", "Ngừng bán"},
                value -> activeFilter = value
        ));
        btnFilterPrescription.setOnClickListener(v -> showThreeStateMenu(
                btnFilterPrescription,
                new String[]{"Kê đơn: Tất cả", "Cần kê đơn", "Không cần kê đơn"},
                value -> prescriptionFilter = value
        ));
        btnFilterFeatured.setOnClickListener(v -> showThreeStateMenu(
                btnFilterFeatured,
                new String[]{"Nổi bật: Tất cả", "Nổi bật", "Không nổi bật"},
                value -> featuredFilter = value
        ));
        btnFilterBestSeller.setOnClickListener(v -> showThreeStateMenu(
                btnFilterBestSeller,
                new String[]{"Bán chạy: Tất cả", "Bán chạy", "Không bán chạy"},
                value -> bestSellerFilter = value
        ));
        btnFilterStock.setOnClickListener(v -> showStockMenu());

        updateFilterLabels();
    }

    private void showCategoryMenu() {
        List<String> labels = new ArrayList<>();
        labels.add("Tất cả danh mục");
        labels.add("Chưa phân loại");
        for (ProductCategory category : categories) {
            labels.add(category.name != null ? category.name : category.id);
        }

        showWhiteDropdown(btnFilterCategory, labels, position -> {
            if (position == 0) {
                selectedCategoryId = null;
                filterUncategorized = false;
            } else if (position == 1) {
                selectedCategoryId = null;
                filterUncategorized = true;
            } else {
                selectedCategoryId = categories.get(position - 2).id;
                filterUncategorized = false;
            }
            updateFilterLabels();
            applyLocalFilters();
        });
    }

    private void showThreeStateMenu(View anchor, String[] labels, FilterSetter setter) {
        List<String> items = new ArrayList<>();
        for (String label : labels) {
            items.add(label);
        }

        showWhiteDropdown(anchor, items, position -> {
            setter.set(position);
            updateFilterLabels();
            applyLocalFilters();
        });
    }

    private void showStockMenu() {
        List<String> labels = new ArrayList<>();
        labels.add("Tồn kho: Tất cả");
        labels.add("Còn hàng");
        labels.add("Sắp hết");
        labels.add("Hết hàng");

        int[] stockValues = {STOCK_ALL, STOCK_IN_STOCK, STOCK_LOW, STOCK_OUT};
        showWhiteDropdown(btnFilterStock, labels, position -> {
            stockFilter = stockValues[position];
            updateFilterLabels();
            applyLocalFilters();
        });
    }

    private void applyLocalFilters() {
        List<Products> filteredProducts = new ArrayList<>();
        for (Products product : allProducts) {
            if (matchesSearch(product)
                    && matchesCategory(product)
                    && matchesBooleanFilter(product.is_active, activeFilter)
                    && matchesBooleanFilter(product.requires_prescription, prescriptionFilter)
                    && matchesBooleanFilter(product.is_featured, featuredFilter)
                    && matchesBooleanFilter(product.is_best_seller, bestSellerFilter)
                    && matchesStock(product)) {
                filteredProducts.add(product);
            }
        }
        adapter.setProducts(filteredProducts);
    }

    private boolean matchesSearch(Products product) {
        if (searchQuery.isEmpty()) {
            return true;
        }

        ProductCategory category = findCategory(resolveProductCategoryId(product));
        ProductSubcategory subcategory = findSubcategory(product.subcategory_id);
        return contains(product.id, searchQuery)
                || contains(product.name, searchQuery)
                || contains(product.brand, searchQuery)
                || contains(product.manufacturer, searchQuery)
                || contains(product.unit, searchQuery)
                || contains(product.uses, searchQuery)
                || contains(product.usage, searchQuery)
                || contains(product.subcategory_id, searchQuery)
                || (category != null && contains(category.name, searchQuery))
                || (subcategory != null && contains(subcategory.name, searchQuery));
    }

    private boolean matchesCategory(Products product) {
        String productCategoryId = resolveProductCategoryId(product);
        if (filterUncategorized) {
            return isBlank(productCategoryId);
        }
        if (selectedCategoryId == null) {
            return true;
        }
        return selectedCategoryId.equals(productCategoryId);
    }

    private boolean matchesBooleanFilter(boolean value, int filter) {
        if (filter == FILTER_YES) {
            return value;
        }
        if (filter == FILTER_NO) {
            return !value;
        }
        return true;
    }

    private boolean matchesStock(Products product) {
        switch (stockFilter) {
            case STOCK_IN_STOCK:
                return product.stock > 0;
            case STOCK_LOW:
                return product.stock > 0 && product.stock < 10;
            case STOCK_OUT:
                return product.stock <= 0;
            case STOCK_ALL:
            default:
                return true;
        }
    }

    private void updateFilterLabels() {
        if (btnFilterCategory != null) {
            if (filterUncategorized) {
                btnFilterCategory.setText("Chưa phân loại");
                btnFilterCategory.setBackgroundTintList(android.content.res.ColorStateList.valueOf(requireContext().getColor(R.color.admin_primary)));
                btnFilterCategory.setTextColor(Color.WHITE);
                btnFilterCategory.setIconTintResource(R.color.white);
            } else if (selectedCategoryId == null) {
                btnFilterCategory.setText("Danh mục");
                btnFilterCategory.setBackgroundTintList(android.content.res.ColorStateList.valueOf(requireContext().getColor(R.color.admin_light_green)));
                btnFilterCategory.setTextColor(requireContext().getColor(R.color.admin_text_main));
                btnFilterCategory.setIconTintResource(R.color.admin_primary);
            } else {
                ProductCategory category = findCategory(selectedCategoryId);
                btnFilterCategory.setText(category != null ? category.name : "Danh mục");
                btnFilterCategory.setBackgroundTintList(android.content.res.ColorStateList.valueOf(requireContext().getColor(R.color.admin_primary)));
                btnFilterCategory.setTextColor(Color.WHITE);
                btnFilterCategory.setIconTintResource(R.color.white);
            }
        }
        setThreeStateLabel(btnFilterActive, activeFilter, "Trạng thái", "Đang bán", "Ngừng bán");
        setThreeStateLabel(btnFilterPrescription, prescriptionFilter, "Kê đơn", "Cần kê đơn", "Không cần kê đơn");
        setThreeStateLabel(btnFilterFeatured, featuredFilter, "Nổi bật", "Nổi bật", "Không nổi bật");
        setThreeStateLabel(btnFilterBestSeller, bestSellerFilter, "Bán chạy", "Bán chạy", "Không bán chạy");
        
        if (stockFilter == STOCK_ALL) {
            btnFilterStock.setText("Tồn kho");
            btnFilterStock.setBackgroundTintList(android.content.res.ColorStateList.valueOf(requireContext().getColor(R.color.admin_light_green)));
            btnFilterStock.setTextColor(requireContext().getColor(R.color.admin_text_main));
            btnFilterStock.setIconTintResource(R.color.admin_primary);
        } else {
            btnFilterStock.setText(getStockLabelOnly());
            btnFilterStock.setBackgroundTintList(android.content.res.ColorStateList.valueOf(requireContext().getColor(R.color.admin_primary)));
            btnFilterStock.setTextColor(Color.WHITE);
            btnFilterStock.setIconTintResource(R.color.white);
        }
    }

    private String getStockLabelOnly() {
        switch (stockFilter) {
            case STOCK_IN_STOCK: return "Còn hàng";
            case STOCK_LOW: return "Sắp hết";
            case STOCK_OUT: return "Hết hàng";
            default: return "Tồn kho";
        }
    }

    private void setThreeStateLabel(MaterialButton button, int filter, String prefix, String yesLabel, String noLabel) {
        if (button == null) return;
        if (filter == FILTER_YES) {
            button.setText(yesLabel);
            button.setBackgroundTintList(android.content.res.ColorStateList.valueOf(requireContext().getColor(R.color.admin_primary)));
            button.setTextColor(Color.WHITE);
            button.setIconTintResource(R.color.white);
        } else if (filter == FILTER_NO) {
            button.setText(noLabel);
            button.setBackgroundTintList(android.content.res.ColorStateList.valueOf(requireContext().getColor(R.color.admin_primary)));
            button.setTextColor(Color.WHITE);
            button.setIconTintResource(R.color.white);
        } else {
            button.setText(prefix);
            button.setBackgroundTintList(android.content.res.ColorStateList.valueOf(requireContext().getColor(R.color.admin_light_green)));
            button.setTextColor(requireContext().getColor(R.color.admin_text_main));
            button.setIconTintResource(R.color.admin_primary);
        }
    }

    private String resolveProductCategoryId(Products product) {
        if (product == null || isBlank(product.subcategory_id)) {
            return null;
        }
        return subcategoryToCategory.get(product.subcategory_id);
    }

    private void normalizeCategoryFilter() {
        if (selectedCategoryId == null) {
            return;
        }
        if (findCategory(selectedCategoryId) == null) {
            selectedCategoryId = null;
            filterUncategorized = false;
            updateFilterLabels();
        }
    }

    private ProductCategory findCategory(String categoryId) {
        if (isBlank(categoryId)) {
            return null;
        }
        for (ProductCategory category : categories) {
            if (categoryId.equals(category.id)) {
                return category;
            }
        }
        return null;
    }

    private ProductSubcategory findSubcategory(String subcategoryId) {
        if (isBlank(subcategoryId)) {
            return null;
        }
        for (ProductSubcategory subcategory : subcategories) {
            if (subcategoryId.equals(subcategory.id)) {
                return subcategory;
            }
        }
        return null;
    }

    private void showWhiteDropdown(View anchor, List<String> labels, DropdownSelectionListener listener) {
        LinearLayout container = new LinearLayout(requireContext());
        container.setOrientation(LinearLayout.VERTICAL);
        container.setBackgroundColor(Color.WHITE);
        int minWidth = Math.max(anchor.getWidth(), dp(190));

        PopupWindow popupWindow = new PopupWindow(
                container,
                minWidth,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true
        );
        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        popupWindow.setOutsideTouchable(true);
        popupWindow.setElevation(dp(6));

        for (int i = 0; i < labels.size(); i++) {
            int position = i;
            TextView itemView = new TextView(requireContext());
            itemView.setText(labels.get(i));
            itemView.setTextColor(Color.BLACK);
            itemView.setTextSize(13);
            itemView.setSingleLine(true);
            itemView.setGravity(android.view.Gravity.CENTER_VERTICAL);
            itemView.setPadding(dp(14), 0, dp(14), 0);
            itemView.setMinHeight(dp(42));
            itemView.setBackgroundColor(Color.WHITE);
            itemView.setOnClickListener(v -> {
                popupWindow.dismiss();
                listener.onSelected(position);
            });
            container.addView(itemView, new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            ));
        }

        popupWindow.showAsDropDown(anchor, 0, dp(4));
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private boolean contains(String value, String query) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(query);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private interface DropdownSelectionListener {
        void onSelected(int position);
    }

    private interface FilterSetter {
        void set(int value);
    }
}
