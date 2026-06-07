package com.example.medictown.data.models;

public class ChatMessageUi {
    public ChatMessage message;
    public MessageSendState sendState;

    public ChatMessageUi(ChatMessage message, MessageSendState sendState) {
        this.message = message;
        this.sendState = sendState;
    }
}
