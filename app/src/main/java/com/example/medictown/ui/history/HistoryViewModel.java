package com.example.medictown.ui.history;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.medictown.data.models.Orders;
import com.example.medictown.data.repositories.HistoryRepository;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HistoryViewModel extends ViewModel {
    private final HistoryRepository historyRepository;

    private final MutableLiveData<List<Orders>> _orders = new MutableLiveData<>();
    public LiveData<List<Orders>> orders = _orders;

    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>(false);
    public LiveData<Boolean> isLoading = _isLoading;

    private final MutableLiveData<String> _errorMessage = new MutableLiveData<>();
    public LiveData<String> errorMessage = _errorMessage;

    public HistoryViewModel() {
        historyRepository = new HistoryRepository();
    }

    public void fetchOrders(String userId) {
        _isLoading.setValue(true);
        historyRepository.getUserOrders(userId, new Callback<List<Orders>>() {
            @Override
            public void onResponse(Call<List<Orders>> call, Response<List<Orders>> response) {
                _isLoading.setValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    _orders.setValue(response.body());
                } else {
                    _errorMessage.setValue("Lỗi khi lấy lịch sử đơn hàng: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<Orders>> call, Throwable t) {
                _isLoading.setValue(false);
                _errorMessage.setValue("Lỗi kết nối: " + t.getMessage());
            }
        });
    }
}
