package com.example.medictown.ui.chat;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.medictown.data.api.RetrofitClient;
import com.example.medictown.data.models.ChatSocketEvent;
import com.example.medictown.data.models.SellerConversationItem;
import com.example.medictown.data.repositories.ChatRepository;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SellerChatViewModel extends AndroidViewModel {
    private static final long REFRESH_DEBOUNCE_MS = 300;

    private final ChatRepository repository = new ChatRepository();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final MutableLiveData<List<SellerConversationItem>> conversations =
            new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> connected = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>();

    private final Runnable refreshRunnable = this::loadConversations;
    private String accessToken;

    public SellerChatViewModel(@NonNull Application application) {
        super(application);
    }

    public LiveData<List<SellerConversationItem>> getConversations() {
        return conversations;
    }

    public LiveData<Boolean> getLoading() {
        return loading;
    }

    public LiveData<Boolean> getConnected() {
        return connected;
    }

    public LiveData<String> getError() {
        return error;
    }

    public void initialize(String token) {
        accessToken = token;
        RetrofitClient.setAuthToken(token);
        loadConversations();
    }

    public void loadConversations() {
        loading.setValue(true);
        repository.getSellerConversations(new Callback<List<SellerConversationItem>>() {
            @Override
            public void onResponse(
                    Call<List<SellerConversationItem>> call,
                    Response<List<SellerConversationItem>> response
            ) {
                loading.setValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    conversations.setValue(response.body());
                    error.setValue(null);
                } else {
                    error.setValue("Không thể tải danh sách tin nhắn: " + response.code());
                }
            }

            @Override
            public void onFailure(
                    Call<List<SellerConversationItem>> call,
                    Throwable throwable
            ) {
                loading.setValue(false);
                error.setValue(connectionError(throwable));
            }
        });
    }

    public void connectRealtime() {
        if (accessToken == null || accessToken.trim().isEmpty()) {
            error.setValue("Access token is required");
            return;
        }
        repository.connectRealtime(
                getApplication(),
                accessToken,
                new ChatRepository.RealtimeListener() {
            @Override
            public void onConnected() {
                connected.postValue(true);
                scheduleRefresh();
            }

            @Override
            public void onEvent(ChatSocketEvent event) {
                if ("chat.message.created".equals(event.type)) {
                    scheduleRefresh();
                }
            }

            @Override
            public void onDisconnected() {
                connected.postValue(false);
            }

            @Override
            public void onError(Throwable throwable) {
                connected.postValue(false);
                error.postValue(connectionError(throwable));
            }
        });
    }

    public void disconnectRealtime() {
        repository.disconnectRealtime();
        connected.setValue(false);
    }

    private void scheduleRefresh() {
        handler.removeCallbacks(refreshRunnable);
        handler.postDelayed(refreshRunnable, REFRESH_DEBOUNCE_MS);
    }

    private String connectionError(Throwable throwable) {
        String message = throwable == null ? null : throwable.getMessage();
        return message == null || message.trim().isEmpty()
                ? "Lỗi kết nối"
                : "Lỗi kết nối: " + message;
    }

    @Override
    protected void onCleared() {
        handler.removeCallbacksAndMessages(null);
        repository.disconnectRealtime();
        super.onCleared();
    }
}
