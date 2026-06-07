package com.example.medictown.data.models;

import java.util.ArrayList;
import java.util.List;

public class ChatMessagePage {
    public List<ChatMessage> items = new ArrayList<>();
    public String next_cursor;
    public String next_cursor_id;
}
