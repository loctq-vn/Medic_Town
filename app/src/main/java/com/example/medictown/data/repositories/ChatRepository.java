package com.example.medictown.data.repositories;

import android.content.Context;

import com.example.medictown.data.api.ChatWebSocketClient;
import com.example.medictown.data.api.RetrofitClient;
import com.example.medictown.data.api.SupabaseApi;
import com.example.medictown.data.models.ChatMessage;
import com.example.medictown.data.models.ChatMessagePage;
import com.example.medictown.data.models.ChatMessageRequest;
import com.example.medictown.data.models.ChatReadRequest;
import com.example.medictown.data.models.ChatReadResult;
import com.example.medictown.data.models.ChatSocketEvent;
import com.example.medictown.data.models.Conversation;
import com.example.medictown.data.models.SellerConversationItem;

import java.util.List;

import retrofit2.Callback;

public class ChatRepository {
    public interface RealtimeListener {
        void onConnected();
        void onEvent(ChatSocketEvent event);
        void onDisconnected();
        void onError(Throwable throwable);
    }

    private final SupabaseApi apiService;
    private ChatWebSocketClient webSocketClient;

    public ChatRepository() {
        apiService = RetrofitClient.getApiService();
    }

    public void getOrCreateConversation(Callback<Conversation> callback) {
        apiService.getOrCreateChatConversation().enqueue(callback);
    }

    public void loadMessages(
            String conversationId,
            String before,
            String beforeId,
            int limit,
            Callback<ChatMessagePage> callback
    ) {
        apiService.getChatMessages(
                conversationId,
                before,
                beforeId,
                null,
                null,
                limit
        ).enqueue(callback);
    }

    public void loadMessagesAfter(
            String conversationId,
            String after,
            String afterId,
            int limit,
            Callback<ChatMessagePage> callback
    ) {
        apiService.getChatMessages(
                conversationId,
                null,
                null,
                after,
                afterId,
                limit
        ).enqueue(callback);
    }

    public void sendMessage(
            String conversationId,
            String clientMessageId,
            String content,
            Callback<ChatMessage> callback
    ) {
        apiService.sendChatMessage(
                new ChatMessageRequest(conversationId, clientMessageId, content)
        ).enqueue(callback);
    }

    public void markRead(
            String conversationId,
            Callback<ChatReadResult> callback
    ) {
        apiService.markChatRead(new ChatReadRequest(conversationId)).enqueue(callback);
    }

    public void getSellerConversations(
            Callback<List<SellerConversationItem>> callback
    ) {
        apiService.getSellerChatConversations().enqueue(callback);
    }

    public synchronized void connectRealtime(
            Context context,
            String accessToken,
            RealtimeListener listener
    ) {
        disconnectRealtime();
        webSocketClient = new ChatWebSocketClient(context, new ChatWebSocketClient.Listener() {
            @Override
            public void onConnected() {
                listener.onConnected();
            }

            @Override
            public void onEvent(ChatSocketEvent event) {
                listener.onEvent(event);
            }

            @Override
            public void onDisconnected() {
                listener.onDisconnected();
            }

            @Override
            public void onError(Throwable throwable) {
                listener.onError(throwable);
            }
        });
        webSocketClient.connect(accessToken);
    }

    public synchronized void disconnectRealtime() {
        if (webSocketClient != null) {
            webSocketClient.disconnect();
            webSocketClient = null;
        }
    }
}
