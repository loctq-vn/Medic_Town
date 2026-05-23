package com.example.medictown.ui.admin;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.medictown.data.api.RetrofitClient;
import com.example.medictown.data.api.SupabaseApi;
import com.example.medictown.data.models.Orders;
import com.example.medictown.data.models.Products;
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
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public AdminViewModel() {
        this.apiService = RetrofitClient.getApiService();
    }

    public LiveData<List<Orders>> getAllOrders() { return allOrders; }
    public LiveData<List<Products>> getAllProducts() { return allProducts; }
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
        apiService.getProducts(0, 100).enqueue(new Callback<List<Products>>() {
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

    public void updateOrderStatus(String orderId, String newStatus) {
        Map<String, Object> update = new HashMap<>();
        update.put("status", newStatus);
        apiService.updateOrderStatus("eq." + orderId, update).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    fetchAllOrders();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                errorMessage.setValue(t.getMessage());
            }
        });
    }
}
