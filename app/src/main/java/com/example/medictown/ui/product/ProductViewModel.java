package com.example.medictown.ui.product;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.medictown.data.models.Products;
import com.example.medictown.data.repositories.ProductRepository;
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
}
