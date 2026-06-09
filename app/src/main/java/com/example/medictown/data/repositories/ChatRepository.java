package com.example.medictown.data.repositories;

import android.content.Context;
import android.net.Uri;
import android.widget.Toast;

import com.example.medictown.data.api.ChatWebSocketClient;
import com.example.medictown.data.api.RetrofitClient;
import com.example.medictown.data.api.SessionManager;
import com.example.medictown.data.api.SupabaseApi;
import com.example.medictown.data.api.SupabaseConfig;
import com.example.medictown.data.models.ChatMessage;
import com.example.medictown.data.models.ChatMessageMetadata;
import com.example.medictown.data.models.ChatMessagePage;
import com.example.medictown.data.models.ChatMessageRequest;
import com.example.medictown.data.models.ChatReadRequest;
import com.example.medictown.data.models.ChatReadResult;
import com.example.medictown.data.models.ChatSocketEvent;
import com.example.medictown.data.models.Conversation;
import com.example.medictown.data.models.SellerConversationItem;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
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
        sendMessage(
                conversationId,
                clientMessageId,
                content,
                "text",
                new ChatMessageMetadata(),
                callback
        );
    }

    public void sendMessage(
            String conversationId,
            String clientMessageId,
            String content,
            String messageType,
            ChatMessageMetadata metadata,
            Callback<ChatMessage> callback
    ) {
        apiService.sendChatMessage(
                new ChatMessageRequest(
                        conversationId,
                        clientMessageId,
                        content,
                        messageType,
                        metadata
                )
        ).enqueue(callback);
    }

    public void uploadChatImage(
            Context context,
            String conversationId,
            Uri fileUri,
            okhttp3.Callback callback
    ) {
        OkHttpClient client = new OkHttpClient();
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(fileUri);
            byte[] bytes = getBytes(inputStream);
            ImageType imageType = detectImageType(bytes);

            String fileName = "chat_" + System.currentTimeMillis() + "." + imageType.extension;
            String mimeType = imageType.mimeType;

            RequestBody requestBody = RequestBody.create(bytes, MediaType.parse(mimeType));
            String token = new SessionManager(context).getToken();

            Request request = new Request.Builder()
                    .url(SupabaseConfig.BACKEND_URL
                            + "api/chat/images?conversation_id="
                            + conversationId
                            + "&filename="
                            + fileName)
                    .post(requestBody)
                    .addHeader("Authorization", "Bearer " + token)
                    .addHeader("ngrok-skip-browser-warning", "true")
                    .addHeader("Content-Type", mimeType)
                    .build();
            client.newCall(request).enqueue(callback);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "Loi upload anh chat", Toast.LENGTH_SHORT).show();
            if (callback != null) {
                callback.onFailure(null, new IOException(e));
            }
        }
    }

    private ImageType detectImageType(byte[] bytes) throws IOException {
        if (bytes.length >= 3
                && (bytes[0] & 0xFF) == 0xFF
                && (bytes[1] & 0xFF) == 0xD8
                && (bytes[2] & 0xFF) == 0xFF) {
            return new ImageType("jpg", "image/jpeg");
        }
        if (bytes.length >= 8
                && (bytes[0] & 0xFF) == 0x89
                && bytes[1] == 'P'
                && bytes[2] == 'N'
                && bytes[3] == 'G'
                && bytes[4] == '\r'
                && bytes[5] == '\n'
                && bytes[6] == 0x1A
                && bytes[7] == '\n') {
            return new ImageType("png", "image/png");
        }
        if (bytes.length >= 12
                && bytes[0] == 'R'
                && bytes[1] == 'I'
                && bytes[2] == 'F'
                && bytes[3] == 'F'
                && bytes[8] == 'W'
                && bytes[9] == 'E'
                && bytes[10] == 'B'
                && bytes[11] == 'P') {
            return new ImageType("webp", "image/webp");
        }
        throw new IOException(
                "Định dạng ảnh không được hỗ trợ. Vui lòng chọn JPG, PNG hoặc WebP"
        );
    }

    private static class ImageType {
        final String extension;
        final String mimeType;

        ImageType(String extension, String mimeType) {
            this.extension = extension;
            this.mimeType = mimeType;
        }
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

    private byte[] getBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];
        int len;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }
}
