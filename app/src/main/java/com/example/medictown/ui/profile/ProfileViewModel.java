package com.example.medictown.ui.profile;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.medictown.data.models.Users;
import com.example.medictown.data.repositories.ProfileRepository;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileViewModel extends ViewModel {
    private final ProfileRepository repository;
    private final MutableLiveData<Users> user = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);

    public ProfileViewModel() {
        this.repository = new ProfileRepository();
    }
    public LiveData<Users> getUser(){return user;}
    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }
    public void fetchCurrentUserProfile() {
        isLoading.setValue(true);

        repository.getCurrentUser(new Callback<Users>() {
            @Override
            public void onResponse(
                    Call<Users> call,
                    Response<Users> response
            ) {
                isLoading.setValue(false);

                if (response.isSuccessful() && response.body() != null) {
                    user.setValue(response.body());
                } else {
                    errorMessage.setValue(
                            "Không thể tải hồ sơ: HTTP" + response.code()
                    );
                }
            }

            @Override
            public void onFailure(Call<Users> call, Throwable throwable) {
                isLoading.setValue(false);

                errorMessage.setValue(
                        "Lỗi kết nối: " + throwable.getMessage()
                );
            }
        });
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }
}
