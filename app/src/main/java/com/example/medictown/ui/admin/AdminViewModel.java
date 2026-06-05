package com.example.medictown.ui.admin;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.medictown.data.api.RetrofitClient;
import com.example.medictown.data.api.SupabaseApi;
import com.example.medictown.data.models.Orders;
import com.example.medictown.data.models.ProductCategory;
import com.example.medictown.data.models.ProductSubcategory;
import com.example.medictown.data.models.Products;
import com.example.medictown.data.models.RevenueDailySummary;
import com.example.medictown.data.models.RevenueDashboard;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminViewModel extends ViewModel {
    private final SupabaseApi apiService;
    private final MutableLiveData<List<Orders>> allOrders = new MutableLiveData<>();
    private final MutableLiveData<List<Products>> allProducts = new MutableLiveData<>();
    private final MutableLiveData<List<ProductCategory>> productCategories = new MutableLiveData<>();
    private final MutableLiveData<List<ProductSubcategory>> productSubcategories = new MutableLiveData<>();
    private final MutableLiveData<RevenueDashboard> revenueDashboard = new MutableLiveData<>();
    private final MutableLiveData<RevenueDailySummary> revenueDailySummary = new MutableLiveData<>();
    private final MutableLiveData<List<RevenueDashboard.TopProduct>> revenueTopProducts = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public AdminViewModel() {
        this.apiService = RetrofitClient.getApiService();
    }

    public LiveData<List<Orders>> getAllOrders() { return allOrders; }
    public LiveData<List<Products>> getAllProducts() { return allProducts; }
    public LiveData<List<ProductCategory>> getProductCategories() { return productCategories; }
    public LiveData<List<ProductSubcategory>> getProductSubcategories() { return productSubcategories; }
    public LiveData<RevenueDashboard> getRevenueDashboard() { return revenueDashboard; }
    public LiveData<RevenueDailySummary> getRevenueDailySummary() { return revenueDailySummary; }
    public LiveData<List<RevenueDashboard.TopProduct>> getRevenueTopProducts() { return revenueTopProducts; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getErrorMessage() { return errorMessage; }

    public void fetchAllOrders() {
        isLoading.setValue(true);
        apiService.getOrders().enqueue(new Callback<List<Orders>>() {
            @Override
            public void onResponse(Call<List<Orders>> call, Response<List<Orders>> response) {
                isLoading.setValue(false);
                if (response.isSuccessful()) {
                    allOrders.setValue(response.body());
                } else {
                    errorMessage.setValue("Failed to fetch orders");
                }
            }

            @Override
            public void onFailure(Call<List<Orders>> call, Throwable t) {
                isLoading.setValue(false);
                errorMessage.setValue(t.getMessage());
            }
        });
    }

    public void fetchAllProducts() {
        isLoading.setValue(true);
        apiService.getProducts(null, null, 100, 0).enqueue(new Callback<List<Products>>() {
            @Override
            public void onResponse(Call<List<Products>> call, Response<List<Products>> response) {
                isLoading.setValue(false);
                if (response.isSuccessful()) {
                    allProducts.setValue(response.body());
                } else {
                    errorMessage.setValue("Failed to fetch products");
                }
            }

            @Override
            public void onFailure(Call<List<Products>> call, Throwable t) {
                isLoading.setValue(false);
                errorMessage.setValue(t.getMessage());
            }
        });
    }

    public void fetchShopOrders(String shopId) {
        isLoading.setValue(true);
        apiService.getShopOrders(shopId).enqueue(new Callback<List<Orders>>() {
            @Override
            public void onResponse(Call<List<Orders>> call, Response<List<Orders>> response) {
                isLoading.setValue(false);
                if (response.isSuccessful()) {
                    allOrders.setValue(response.body());
                } else {
                    errorMessage.setValue("Failed to fetch shop orders");
                }
            }

            @Override
            public void onFailure(Call<List<Orders>> call, Throwable t) {
                isLoading.setValue(false);
                errorMessage.setValue(t.getMessage());
            }
        });
    }

    public void fetchShopProducts(String shopId) {
        isLoading.setValue(true);
        apiService.getShopProducts(shopId).enqueue(new Callback<List<Products>>() {
            @Override
            public void onResponse(Call<List<Products>> call, Response<List<Products>> response) {
                isLoading.setValue(false);
                if (response.isSuccessful()) {
                    allProducts.setValue(response.body());
                } else {
                    errorMessage.setValue("Failed to fetch shop products");
                }
            }

            @Override
            public void onFailure(Call<List<Products>> call, Throwable t) {
                isLoading.setValue(false);
                errorMessage.setValue(t.getMessage());
            }
        });
    }

    public void fetchRevenueDashboard(String shopId, String fromDate, String toDate, String groupBy) {
        isLoading.setValue(true);
        apiService.getRevenueDashboard(shopId, fromDate, toDate, groupBy).enqueue(new Callback<RevenueDashboard>() {
            @Override
            public void onResponse(Call<RevenueDashboard> call, Response<RevenueDashboard> response) {
                isLoading.setValue(false);
                if (response.isSuccessful()) {
                    revenueDashboard.setValue(response.body());
                } else {
                    errorMessage.setValue("Failed to fetch revenue dashboard");
                }
            }

            @Override
            public void onFailure(Call<RevenueDashboard> call, Throwable t) {
                isLoading.setValue(false);
                errorMessage.setValue(t.getMessage());
            }
        });
    }

    public void fetchRevenueDailySummary(String shopId) {
        isLoading.setValue(true);
        apiService.getRevenueDailySummary(shopId, null, null).enqueue(new Callback<RevenueDailySummary>() {
            @Override
            public void onResponse(Call<RevenueDailySummary> call, Response<RevenueDailySummary> response) {
                isLoading.setValue(false);
                if (response.isSuccessful()) {
                    revenueDailySummary.setValue(response.body());
                } else {
                    errorMessage.setValue("Failed to fetch revenue daily summary");
                }
            }

            @Override
            public void onFailure(Call<RevenueDailySummary> call, Throwable t) {
                isLoading.setValue(false);
                errorMessage.setValue(t.getMessage());
            }
        });
    }

    public void fetchRevenueTopProducts(String shopId, String fromDate, String toDate) {
        apiService.getRevenueTopProducts(shopId, fromDate, toDate).enqueue(new Callback<List<RevenueDashboard.TopProduct>>() {
            @Override
            public void onResponse(Call<List<RevenueDashboard.TopProduct>> call, Response<List<RevenueDashboard.TopProduct>> response) {
                if (response.isSuccessful()) {
                    revenueTopProducts.setValue(response.body());
                } else {
                    errorMessage.setValue("Failed to fetch revenue top products");
                }
            }

            @Override
            public void onFailure(Call<List<RevenueDashboard.TopProduct>> call, Throwable t) {
                errorMessage.setValue(t.getMessage());
            }
        });
    }

    public void fetchProductTaxonomy() {
        apiService.getProductCategories().enqueue(new Callback<List<ProductCategory>>() {
            @Override
            public void onResponse(Call<List<ProductCategory>> call, Response<List<ProductCategory>> response) {
                if (response.isSuccessful()) {
                    productCategories.setValue(response.body());
                } else {
                    errorMessage.setValue("Failed to fetch product categories");
                }
            }

            @Override
            public void onFailure(Call<List<ProductCategory>> call, Throwable t) {
                errorMessage.setValue(t.getMessage());
            }
        });

        apiService.getProductSubcategories(null).enqueue(new Callback<List<ProductSubcategory>>() {
            @Override
            public void onResponse(Call<List<ProductSubcategory>> call, Response<List<ProductSubcategory>> response) {
                if (response.isSuccessful()) {
                    productSubcategories.setValue(response.body());
                } else {
                    errorMessage.setValue("Failed to fetch product subcategories");
                }
            }

            @Override
            public void onFailure(Call<List<ProductSubcategory>> call, Throwable t) {
                errorMessage.setValue(t.getMessage());
            }
        });
    }

    public void updateOrderStatus(String shopId, String orderId, String newStatus) {
        Map<String, Object> update = new HashMap<>();
        update.put("status", newStatus);
        apiService.updateShopOrderStatus(shopId, orderId, update).enqueue(new Callback<Orders>() {
            @Override
            public void onResponse(Call<Orders> call, Response<Orders> response) {
                if (response.isSuccessful()) {
                    fetchShopOrders(shopId);
                } else {
                    errorMessage.setValue("Failed to update order status");
                }
            }

            @Override
            public void onFailure(Call<Orders> call, Throwable t) {
                errorMessage.setValue(t.getMessage());
            }
        });
    }

    public void updateProductActive(String shopId, String productId, boolean isActive) {
        Map<String, Object> update = new HashMap<>();
        update.put("is_active", isActive);
        apiService.updateShopProductFields(shopId, productId, update).enqueue(new Callback<Products>() {
            @Override
            public void onResponse(Call<Products> call, Response<Products> response) {
                if (response.isSuccessful()) {
                    fetchShopProducts(shopId);
                } else {
                    errorMessage.setValue("Failed to update product status");
                }
            }

            @Override
            public void onFailure(Call<Products> call, Throwable t) {
                errorMessage.setValue(t.getMessage());
            }
        });
    }
}
