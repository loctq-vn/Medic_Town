package com.example.medictown.data.models;

public class ChatMessageRequest {
    public final String conversation_id;
    public final String client_message_id;
    public final String content;

    public ChatMessageRequest(String conversationId, String clientMessageId, String content) {
        this.conversation_id = conversationId;
        this.client_message_id = clientMessageId;
        this.content = content;
    }
}
