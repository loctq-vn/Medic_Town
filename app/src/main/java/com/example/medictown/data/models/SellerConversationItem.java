package com.example.medictown.data.models;

public class SellerConversationItem {
    public Conversation conversation;
    public Customer customer;
    public ChatMessage last_message;
    public int unread_count;

    public static class Customer {
        public String id;
        public String name;
        public String avatar_url;
    }
}
