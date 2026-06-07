package com.example.medictown.data.models;

public class ChatSocketEvent {
    public String type;
    public String event_id;
    public String occurred_at;
    public Data data;

    public static class Data {
        public String conversation_id;
        public String message_id;
    }
}
