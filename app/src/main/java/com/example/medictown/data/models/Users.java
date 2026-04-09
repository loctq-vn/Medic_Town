package com.example.medictown.data.models;

import java.util.Date;

public class Users {
    public String id; // Firebase Auth UID
    public String name;
    public String email;
    public String phone;
    public String avatar_url;
    public String role; // "customer" | "admin"
    public Date created_at;

    public Users() {}
}
