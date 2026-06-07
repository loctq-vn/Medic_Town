package com.example.medictown.ui.chat;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.medictown.data.api.RetrofitClient;
import com.example.medictown.data.models.ChatMessage;
import com.example.medictown.data.models.ChatMessagePage;
import com.example.medictown.data.models.ChatMessageUi;
import com.example.medictown.data.models.ChatReadResult;
import com.example.medictown.data.models.ChatSocketEvent;
import com.example.medictown.data.models.Conversation;
import com.example.medictown.data.models.MessageSendState;
import com.example.medictown.data.repositories.ChatRepository;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatViewModel extends AndroidViewModel {
    private static final int PAGE_SIZE = 50;

    private final ChatRepository repository;
    private final Object messageLock = new Object();
    private final MutableLiveData<Conversation> conversation = new MutableLiveData<>();
    private final MutableLiveData<List<ChatMessageUi>> messages =
            new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> loadingOlder = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> connected = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>();

    private String accessToken;
    private String currentUserId;
    private String nextCursor;
    private String nextCursorId;
    private boolean syncInProgress;
    private boolean syncRequested;

    public ChatViewModel(@NonNull Application application) {
        super(application);
        repository = new ChatRepository();
    }

    public LiveData<Conversation> getConversation() {
        return conversation;
    }

    public LiveData<List<ChatMessageUi>> getMessages() {
        return messages;
    }

    public LiveData<Boolean> getLoading() {
        return loading;
    }

    public LiveData<Boolean> getLoadingOlder() {
        return loadingOlder;
    }

    public LiveData<Boolean> getConnected() {
        return connected;
    }

    public LiveData<String> getError() {
        return error;
    }

    public void initializeCustomerChat(String token, String userId) {
        prepareSession(token, userId);
        loading.setValue(true);
        repository.getOrCreateConversation(new Callback<Conversation>() {
            @Override
            public void onResponse(
                    Call<Conversation> call,
                    Response<Conversation> response
            ) {
                if (response.isSuccessful() && response.body() != null) {
                    setConversationAndStart(response.body());
                } else {
                    loading.setValue(false);
                    error.setValue("Khong the tao cuoc tro chuyen: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Conversation> call, Throwable throwable) {
                loading.setValue(false);
                error.setValue(connectionError(throwable));
            }
        });
    }

    public void initializeConversation(
            Conversation selectedConversation,
            String token,
            String userId
    ) {
        prepareSession(token, userId);
        if (selectedConversation == null || selectedConversation.id == null) {
            error.setValue("Conversation is required");
            return;
        }
        setConversationAndStart(selectedConversation);
    }

    public void loadOlderMessages() {
        Conversation current = conversation.getValue();
        if (current == null || current.id == null || nextCursor == null) {
            return;
        }
        if (Boolean.TRUE.equals(loadingOlder.getValue())) {
            return;
        }

        loadingOlder.setValue(true);
        repository.loadMessages(
                current.id,
                nextCursor,
                nextCursorId,
                PAGE_SIZE,
                new Callback<ChatMessagePage>() {
                    @Override
                    public void onResponse(
                            Call<ChatMessagePage> call,
                            Response<ChatMessagePage> response
                    ) {
                        loadingOlder.setValue(false);
                        if (response.isSuccessful() && response.body() != null) {
                            applyPage(response.body());
                        } else {
                            error.setValue("Khong the tai tin nhan cu: " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<ChatMessagePage> call, Throwable throwable) {
                        loadingOlder.setValue(false);
                        error.setValue(connectionError(throwable));
                    }
                }
        );
    }

    public void sendMessage(String rawContent) {
        Conversation current = conversation.getValue();
        String content = rawContent == null ? "" : rawContent.trim();
        if (current == null || current.id == null) {
            error.setValue("Cuoc tro chuyen chua san sang");
            return;
        }
        if (content.isEmpty()) {
            return;
        }

        String clientMessageId = UUID.randomUUID().toString();
        ChatMessage optimistic = createOptimisticMessage(
                current,
                clientMessageId,
                content
        );
        upsertUiMessage(new ChatMessageUi(optimistic, MessageSendState.SENDING));
        sendPendingMessage(optimistic);
    }

    public void retryMessage(String clientMessageId) {
        ChatMessageUi pending = findByClientMessageId(clientMessageId);
        if (pending == null || pending.message == null) {
            return;
        }
        pending.sendState = MessageSendState.SENDING;
        upsertUiMessage(pending);
        sendPendingMessage(pending.message);
    }

    public void markRead() {
        Conversation current = conversation.getValue();
        if (current == null || current.id == null) {
            return;
        }
        repository.markRead(current.id, new Callback<ChatReadResult>() {
            @Override
            public void onResponse(
                    Call<ChatReadResult> call,
                    Response<ChatReadResult> response
            ) {
                if (!response.isSuccessful()) {
                    error.setValue("Khong the danh dau da doc: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ChatReadResult> call, Throwable throwable) {
                error.setValue(connectionError(throwable));
            }
        });
    }

    public void connectRealtime() {
        if (accessToken == null || accessToken.isEmpty()) {
            error.setValue("Access token is required");
            return;
        }
        repository.connectRealtime(
                getApplication(),
                accessToken,
                new ChatRepository.RealtimeListener() {
            @Override
            public void onConnected() {
                connected.postValue(true);
                syncLatestMessages();
            }

            @Override
            public void onEvent(ChatSocketEvent event) {
                if (!"chat.message.created".equals(event.type) || event.data == null) {
                    return;
                }
                Conversation current = conversation.getValue();
                if (current != null && current.id != null
                        && current.id.equals(event.data.conversation_id)) {
                    syncLatestMessages();
                }
            }

            @Override
            public void onDisconnected() {
                connected.postValue(false);
            }

            @Override
            public void onError(Throwable throwable) {
                connected.postValue(false);
                error.postValue(connectionError(throwable));
            }
        });
    }

    public void disconnectRealtime() {
        repository.disconnectRealtime();
        connected.setValue(false);
    }

    private void prepareSession(String token, String userId) {
        accessToken = token;
        currentUserId = userId;
        RetrofitClient.setAuthToken(token);
        nextCursor = null;
        nextCursorId = null;
        syncInProgress = false;
        syncRequested = false;
        messages.setValue(new ArrayList<>());
    }

    private void setConversationAndStart(Conversation value) {
        conversation.setValue(value);
        loadInitialMessages();
        connectRealtime();
    }

    private void loadInitialMessages() {
        Conversation current = conversation.getValue();
        if (current == null || current.id == null) {
            return;
        }
        loading.setValue(true);
        repository.loadMessages(
                current.id,
                null,
                null,
                PAGE_SIZE,
                new Callback<ChatMessagePage>() {
                    @Override
                    public void onResponse(
                            Call<ChatMessagePage> call,
                            Response<ChatMessagePage> response
                    ) {
                        loading.setValue(false);
                        if (response.isSuccessful() && response.body() != null) {
                            applyPage(response.body());
                            markRead();
                        } else {
                            error.setValue("Khong the tai lich su chat: " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<ChatMessagePage> call, Throwable throwable) {
                        loading.setValue(false);
                        error.setValue(connectionError(throwable));
                    }
                }
        );
    }

    private void syncLatestMessages() {
        Conversation current = conversation.getValue();
        if (current == null || current.id == null) {
            return;
        }
        synchronized (messageLock) {
            if (syncInProgress) {
                syncRequested = true;
                return;
            }
            syncInProgress = true;
            syncRequested = false;
        }

        ChatMessage latest = findLatestServerMessage();
        if (latest == null || latest.created_at == null || latest.id == null) {
            loadRecentMessagesForSync(current.id);
            return;
        }
        loadMessagesAfter(current.id, latest.created_at, latest.id);
    }

    private void loadRecentMessagesForSync(String conversationId) {
        repository.loadMessages(
                conversationId,
                null,
                null,
                PAGE_SIZE,
                new Callback<ChatMessagePage>() {
                    @Override
                    public void onResponse(
                            Call<ChatMessagePage> call,
                            Response<ChatMessagePage> response
                    ) {
                        if (response.isSuccessful() && response.body() != null) {
                            mergeServerMessages(response.body().items);
                            markRead();
                        }
                        finishSync();
                    }

                    @Override
                    public void onFailure(Call<ChatMessagePage> call, Throwable throwable) {
                        error.postValue(connectionError(throwable));
                        finishSync();
                    }
                }
        );
    }

    private void loadMessagesAfter(
            String conversationId,
            String after,
            String afterId
    ) {
        repository.loadMessagesAfter(
                conversationId,
                after,
                afterId,
                PAGE_SIZE,
                new Callback<ChatMessagePage>() {
                    @Override
                    public void onResponse(
                            Call<ChatMessagePage> call,
                            Response<ChatMessagePage> response
                    ) {
                        if (!response.isSuccessful() || response.body() == null) {
                            error.postValue(
                                    "Khong the dong bo tin nhan: " + response.code()
                            );
                            finishSync();
                            return;
                        }

                        ChatMessagePage page = response.body();
                        mergeServerMessages(page.items);
                        if (page.next_cursor != null && page.next_cursor_id != null) {
                            loadMessagesAfter(
                                    conversationId,
                                    page.next_cursor,
                                    page.next_cursor_id
                            );
                            return;
                        }
                        markRead();
                        finishSync();
                    }

                    @Override
                    public void onFailure(Call<ChatMessagePage> call, Throwable throwable) {
                        error.postValue(connectionError(throwable));
                        finishSync();
                    }
                }
        );
    }

    private ChatMessage findLatestServerMessage() {
        ChatMessage latest = null;
        List<ChatMessageUi> current = messages.getValue();
        if (current == null) {
            return null;
        }
        for (ChatMessageUi item : current) {
            ChatMessage message = item == null ? null : item.message;
            if (message == null || message.id == null || message.created_at == null) {
                continue;
            }
            if (latest == null
                    || compareMessagePosition(message, latest) > 0) {
                latest = message;
            }
        }
        return latest;
    }

    private int compareMessagePosition(ChatMessage first, ChatMessage second) {
        int createdAtComparison = first.created_at.compareTo(second.created_at);
        if (createdAtComparison != 0) {
            return createdAtComparison;
        }
        return first.id.compareTo(second.id);
    }

    private void finishSync() {
        boolean runAgain;
        synchronized (messageLock) {
            syncInProgress = false;
            runAgain = syncRequested;
            syncRequested = false;
        }
        if (runAgain) {
            syncLatestMessages();
        }
    }

    private void applyPage(ChatMessagePage page) {
        if (page == null) {
            return;
        }
        nextCursor = page.next_cursor;
        nextCursorId = page.next_cursor_id;
        mergeServerMessages(page.items);
    }

    private void sendPendingMessage(ChatMessage pending) {
        repository.sendMessage(
                pending.conversation_id,
                pending.client_message_id,
                pending.content,
                new Callback<ChatMessage>() {
                    @Override
                    public void onResponse(
                            Call<ChatMessage> call,
                            Response<ChatMessage> response
                    ) {
                        if (response.isSuccessful() && response.body() != null) {
                            replaceWithServerMessage(response.body());
                        } else {
                            setSendState(
                                    pending.client_message_id,
                                    MessageSendState.FAILED
                            );
                            error.setValue("Gui tin nhan that bai: " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<ChatMessage> call, Throwable throwable) {
                        setSendState(
                                pending.client_message_id,
                                MessageSendState.FAILED
                        );
                        error.setValue(connectionError(throwable));
                    }
                }
        );
    }

    private ChatMessage createOptimisticMessage(
            Conversation current,
            String clientMessageId,
            String content
    ) {
        ChatMessage message = new ChatMessage();
        message.conversation_id = current.id;
        message.sender_id = currentUserId;
        message.sender_type = currentUserId != null
                && currentUserId.equals(current.customer_id)
                ? "customer"
                : "seller";
        message.content = content;
        message.message_type = "text";
        message.client_message_id = clientMessageId;
        message.created_at = Instant.now().toString();
        return message;
    }

    private void replaceWithServerMessage(ChatMessage serverMessage) {
        upsertUiMessage(new ChatMessageUi(serverMessage, MessageSendState.SENT));
    }

    private void mergeServerMessages(List<ChatMessage> serverMessages) {
        if (serverMessages == null) {
            return;
        }
        synchronized (messageLock) {
            Map<String, ChatMessageUi> merged = new LinkedHashMap<>();
            List<ChatMessageUi> current = messages.getValue();
            if (current != null) {
                for (ChatMessageUi item : current) {
                    merged.put(messageKey(item.message), item);
                }
            }
            for (ChatMessage serverMessage : serverMessages) {
                removeMatchingClientId(merged, serverMessage.client_message_id);
                merged.put(
                        messageKey(serverMessage),
                        new ChatMessageUi(serverMessage, MessageSendState.SENT)
                );
            }
            publishSorted(new ArrayList<>(merged.values()));
        }
    }

    private void upsertUiMessage(ChatMessageUi item) {
        synchronized (messageLock) {
            Map<String, ChatMessageUi> merged = new LinkedHashMap<>();
            List<ChatMessageUi> current = messages.getValue();
            if (current != null) {
                for (ChatMessageUi existing : current) {
                    merged.put(messageKey(existing.message), existing);
                }
            }
            removeMatchingClientId(merged, item.message.client_message_id);
            merged.put(messageKey(item.message), item);
            publishSorted(new ArrayList<>(merged.values()));
        }
    }

    private void setSendState(String clientMessageId, MessageSendState state) {
        ChatMessageUi item = findByClientMessageId(clientMessageId);
        if (item != null) {
            item.sendState = state;
            upsertUiMessage(item);
        }
    }

    private ChatMessageUi findByClientMessageId(String clientMessageId) {
        List<ChatMessageUi> current = messages.getValue();
        if (current == null) {
            return null;
        }
        for (ChatMessageUi item : current) {
            if (item.message != null
                    && clientMessageId != null
                    && clientMessageId.equals(item.message.client_message_id)) {
                return item;
            }
        }
        return null;
    }

    private void removeMatchingClientId(
            Map<String, ChatMessageUi> values,
            String clientMessageId
    ) {
        if (clientMessageId == null) {
            return;
        }
        values.entrySet().removeIf(entry -> {
            ChatMessage message = entry.getValue().message;
            return message != null
                    && clientMessageId.equals(message.client_message_id);
        });
    }

    private String messageKey(ChatMessage message) {
        if (message.id != null) {
            return "id:" + message.id;
        }
        return "client:" + message.client_message_id;
    }

    private void publishSorted(List<ChatMessageUi> values) {
        Collections.sort(values, Comparator.comparing(
                item -> item.message.created_at == null ? "" : item.message.created_at
        ));
        messages.postValue(values);
    }

    private String connectionError(Throwable throwable) {
        String message = throwable == null ? null : throwable.getMessage();
        return message == null || message.isEmpty()
                ? "Loi ket noi"
                : "Loi ket noi: " + message;
    }

    @Override
    protected void onCleared() {
        repository.disconnectRealtime();
        super.onCleared();
    }
}
