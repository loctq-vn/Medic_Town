package com.example.medictown.data.models;

import com.google.gson.annotations.SerializedName;
import java.util.Map;

public class AuthRequest {
    private String email;
    private String password;
    private Map<String, Object> data; // For metadata like full_name, phone

    public AuthRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public AuthRequest(String email, String password, Map<String, Object> data) {
        this.email = email;
        this.password = password;
        this.data = data;
    }

    // Getters and Setters if needed
}
