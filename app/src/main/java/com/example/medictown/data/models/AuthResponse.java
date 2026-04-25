package com.example.medictown.data.models;

import com.google.gson.annotations.SerializedName;

public class AuthResponse {
    @SerializedName("access_token")
    private String accessToken;
    
    @SerializedName("refresh_token")
    private String refreshToken;
    
    @SerializedName("user")
    private UserData user;

    public String getAccessToken() { return accessToken; }
    public String getRefreshToken() { return refreshToken; }
    public UserData getUser() { return user; }

    public static class UserData {
        private String id;
        private String email;
        @SerializedName("user_metadata")
        private java.util.Map<String, Object> userMetadata;

        public String getId() { return id; }
        public String getEmail() { return email; }
        public java.util.Map<String, Object> getUserMetadata() { return userMetadata; }
    }
}
