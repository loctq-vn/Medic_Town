package com.example.medictown.ui.product;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.medictown.data.models.ProductCategory;
import com.example.medictown.data.models.Products;
import com.example.medictown.data.repositories.ProductRepository;
import java.text.Normalizer;
import java.util.Locale;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProductViewModel extends ViewModel {
    private final ProductRepository repository;
    private final MutableLiveData<List<Products>> featuredProducts = new MutableLiveData<>();
    private final MutableLiveData<List<Products>> allProducts = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private String activeCategoryId;

    public ProductViewModel() {
        this.repository = new ProductRepository();
        loadFeaturedProducts();
    }

    public LiveData<List<Products>> getFeaturedProducts() {
        return featuredProducts;
    }
    
    public LiveData<List<Products>> getAllProducts() {
        return allProducts;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }
    
    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void loadFeaturedProducts() {
        isLoading.setValue(true);
        repository.getFeaturedProducts(new Callback<List<Products>>() {
            @Override
            public void onResponse(Call<List<Products>> call, Response<List<Products>> response) {
                isLoading.setValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    featuredProducts.setValue(response.body());
                } else {
                    errorMessage.setValue("Lỗi khi tải sản phẩm nổi bật: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<List<Products>> call, Throwable t) {
                isLoading.setValue(false);
                errorMessage.setValue("Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    public void loadAllProducts() {
        activeCategoryId = null;
        isLoading.setValue(true);
        repository.getAllProducts(new Callback<List<Products>>() {
            @Override
            public void onResponse(Call<List<Products>> call, Response<List<Products>> response) {
                isLoading.setValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    allProducts.setValue(response.body());
                } else {
                    errorMessage.setValue("Lỗi khi tải danh sách sản phẩm: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<List<Products>> call, Throwable t) {
                isLoading.setValue(false);
                errorMessage.setValue("Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    public void searchProducts(String query) {
        isLoading.setValue(true);
        repository.searchProducts(query, activeCategoryId, new Callback<List<Products>>() {
            @Override
            public void onResponse(Call<List<Products>> call, Response<List<Products>> response) {
                isLoading.setValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    allProducts.setValue(response.body());
                } else {
                    errorMessage.setValue("Không tìm thấy sản phẩm: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<List<Products>> call, Throwable t) {
                isLoading.setValue(false);
                errorMessage.setValue("Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    public void loadProductsByCategoryKey(String categoryKey) {
        isLoading.setValue(true);
        repository.getProductCategories(new Callback<List<ProductCategory>>() {
            @Override
            public void onResponse(Call<List<ProductCategory>> call, Response<List<ProductCategory>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String categoryId = resolveCategoryId(categoryKey, response.body());
                    if (categoryId != null) {
                        loadProductsByCategory(categoryId);
                        return;
                    }
                }

                String fallbackCategoryId = fallbackCategoryId(categoryKey);
                if (fallbackCategoryId != null) {
                    loadProductsByCategory(fallbackCategoryId);
                } else {
                    isLoading.setValue(false);
                    errorMessage.setValue("KhÃ´ng tÃ¬m tháº¥y danh má»¥c phÃ¹ há»£p");
                }
            }

            @Override
            public void onFailure(Call<List<ProductCategory>> call, Throwable t) {
                String fallbackCategoryId = fallbackCategoryId(categoryKey);
                if (fallbackCategoryId != null) {
                    loadProductsByCategory(fallbackCategoryId);
                } else {
                    isLoading.setValue(false);
                    errorMessage.setValue("Lá»—i káº¿t ná»‘i: " + t.getMessage());
                }
            }
        });
    }

    public void loadProductsByCategory(String categoryId) {
        activeCategoryId = categoryId;
        isLoading.setValue(true);
        repository.getProductsByCategory(categoryId, new Callback<List<Products>>() {
            @Override
            public void onResponse(Call<List<Products>> call, Response<List<Products>> response) {
                isLoading.setValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    allProducts.setValue(response.body());
                } else {
                    errorMessage.setValue("Lá»—i khi táº£i sáº£n pháº©m theo danh má»¥c: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<List<Products>> call, Throwable t) {
                isLoading.setValue(false);
                errorMessage.setValue("Lá»—i káº¿t ná»‘i: " + t.getMessage());
            }
        });
    }

    private String resolveCategoryId(String categoryKey, List<ProductCategory> categories) {
        for (ProductCategory category : categories) {
            if (matchesCategory(category, categoryKey)) {
                return category.id;
            }
        }
        return null;
    }

    private boolean matchesCategory(ProductCategory category, String categoryKey) {
        String normalizedId = normalize(category.id);
        String normalizedName = normalize(category.name);
        for (String token : categoryTokens(categoryKey)) {
            String normalizedToken = normalize(token);
            if (normalizedId.equals(normalizedToken)
                    || normalizedName.equals(normalizedToken)
                    || normalizedId.contains(normalizedToken)
                    || normalizedName.contains(normalizedToken)) {
                return true;
            }
        }
        return false;
    }

    private String[] categoryTokens(String categoryKey) {
        if ("medicine".equals(categoryKey)) {
            return new String[]{"medicine", "medicines", "drug", "drugs", "thuoc"};
        }
        if ("supplement".equals(categoryKey)) {
            return new String[]{"supplement", "supplements", "functional_food", "thuc_pham_chuc_nang"};
        }
        if ("cosmetic".equals(categoryKey)) {
            return new String[]{"cosmetic", "cosmetics", "duoc_my_pham", "my_pham"};
        }
        if ("device".equals(categoryKey)) {
            return new String[]{"device", "devices", "medical_device", "medical_devices", "thiet_bi_y_te"};
        }
        if ("personal_care".equals(categoryKey)) {
            return new String[]{"personal_care", "personal", "cham_soc_ca_nhan"};
        }
        return new String[]{categoryKey};
    }

    private String fallbackCategoryId(String categoryKey) {
        if ("medicine".equals(categoryKey)) return "medicine";
        if ("supplement".equals(categoryKey)) return "supplement";
        if ("cosmetic".equals(categoryKey)) return "cosmetic";
        if ("device".equals(categoryKey)) return "device";
        if ("personal_care".equals(categoryKey)) return "personal_care";
        return categoryKey;
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }
        String ascii = value.replace('đ', 'd').replace('Đ', 'D');
        ascii = Normalizer.normalize(ascii, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return ascii.toLowerCase(Locale.US)
                .replaceAll("[^a-z0-9]+", "_")
                .replaceAll("^_+|_+$", "");
    }
}
