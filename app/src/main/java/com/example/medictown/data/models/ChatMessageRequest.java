package com.example.medictown.data.models;

public class ChatMessageRequest {
    public final String conversation_id;
    public final String client_message_id;
    public final String content;
    public final String message_type;
    public final ChatMessageMetadata metadata;

    public ChatMessageRequest(String conversationId, String clientMessageId, String content) {
        this(conversationId, clientMessageId, content, "text", new ChatMessageMetadata());
    }

    public ChatMessageRequest(
            String conversationId,
            String clientMessageId,
            String content,
            String messageType,
            ChatMessageMetadata metadata
    ) {
        this.conversation_id = conversationId;
        this.client_message_id = clientMessageId;
        this.content = content;
        this.message_type = messageType;
        this.metadata = metadata == null ? new ChatMessageMetadata() : metadata;
    }
}
