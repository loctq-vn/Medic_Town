package com.example.medictown.data.models;

import com.google.gson.annotations.SerializedName;

public class GoogleAuthRequest {
    @SerializedName("provider")
    private String provider = "google";
    
    @SerializedName("id_token")
    private String idToken;

    public GoogleAuthRequest(String idToken) {
        this.idToken = idToken;
    }

    public String getProvider() {
        return provider;
    }

    public String getIdToken() {
        return idToken;
    }
}
