package com.example.medictown.ui.history;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.medictown.data.models.Orders;
import com.example.medictown.data.repositories.HistoryRepository;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HistoryViewModel extends ViewModel {
    private final HistoryRepository historyRepository;

    private final MutableLiveData<List<Orders>> _allOrders = new MutableLiveData<>();
    private final MutableLiveData<List<Orders>> _filteredOrders = new MutableLiveData<>();
    public LiveData<List<Orders>> orders = _filteredOrders;

    private final MutableLiveData<String> _currentFilter = new MutableLiveData<>("all");
    public LiveData<String> currentFilter = _currentFilter;

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
                    _allOrders.setValue(response.body());
                    applyFilter(_currentFilter.getValue());
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

    public void setFilter(String filter) {
        _currentFilter.setValue(filter);
        applyFilter(filter);
    }

    private void applyFilter(String filter) {
        List<Orders> all = _allOrders.getValue();
        if (all == null) return;

        if ("all".equals(filter)) {
            _filteredOrders.setValue(all);
        } else {
            List<Orders> filtered = new ArrayList<>();
            for (Orders order : all) {
                if (filter.equals(order.status)) {
                    filtered.add(order);
                }
            }
            _filteredOrders.setValue(filtered);
        }
    }
}
