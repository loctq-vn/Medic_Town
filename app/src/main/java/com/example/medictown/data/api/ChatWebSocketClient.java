package com.example.medictown.data.api;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;

import androidx.annotation.NonNull;

import com.example.medictown.data.models.ChatSocketEvent;
import com.google.gson.JsonObject;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class ChatWebSocketClient {
    public interface Listener {
        void onConnected();
        void onEvent(ChatSocketEvent event);
        void onDisconnected();
        void onError(Throwable throwable);
    }

    private static final long HEARTBEAT_SECONDS = 25;
    private static final long MAX_RECONNECT_SECONDS = 30;

    private final Listener listener;
    private final ConnectivityManager connectivityManager;
    private final ScheduledExecutorService executor =
            Executors.newSingleThreadScheduledExecutor();

    private WebSocket webSocket;
    private ScheduledFuture<?> heartbeatFuture;
    private ScheduledFuture<?> reconnectFuture;
    private ConnectivityManager.NetworkCallback networkCallback;
    private String accessToken;
    private boolean manuallyClosed = true;
    private boolean connecting;
    private boolean connected;
    private boolean networkAvailable;
    private int reconnectAttempt;
    private long socketGeneration;

    public ChatWebSocketClient(Context context, Listener listener) {
        this.listener = listener;
        connectivityManager = (ConnectivityManager) context
                .getApplicationContext()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
    }

    public synchronized void connect(String token) {
        if (token == null || token.trim().isEmpty()) {
            listener.onError(new IllegalArgumentException("Access token is required"));
            return;
        }

        accessToken = token;
        manuallyClosed = false;
        reconnectAttempt = 0;
        networkAvailable = hasInternetConnection();
        registerNetworkCallback();
        if (networkAvailable) {
            scheduleReconnect(0);
        } else {
            listener.onDisconnected();
        }
    }

    public synchronized void disconnect() {
        manuallyClosed = true;
        accessToken = null;
        reconnectAttempt = 0;
        cancelReconnect();
        stopHeartbeat();
        unregisterNetworkCallback();
        connecting = false;
        connected = false;
        socketGeneration++;
        if (webSocket != null) {
            webSocket.close(1000, "Client closed");
            webSocket = null;
        }
        executor.shutdownNow();
    }

    private void openSocket() {
        final long generation;
        final String token;
        synchronized (this) {
            if (manuallyClosed || !networkAvailable || connecting || connected) {
                return;
            }
            connecting = true;
            generation = ++socketGeneration;
            token = accessToken;
        }

        Request request;
        try {
            request = new Request.Builder()
                    .url(buildWebSocketUrl())
                    .addHeader("Authorization", "Bearer " + token)
                    .addHeader("ngrok-skip-browser-warning", "true")
                    .build();
        } catch (Exception exception) {
            synchronized (this) {
                connecting = false;
            }
            listener.onError(exception);
            return;
        }

        WebSocket socket = RetrofitClient.getHttpClient().newWebSocket(
                request,
                createListener(generation)
        );
        synchronized (this) {
            if (generation == socketGeneration && !manuallyClosed) {
                webSocket = socket;
            } else {
                socket.cancel();
            }
        }
    }

    private WebSocketListener createListener(long generation) {
        return new WebSocketListener() {
            @Override
            public void onOpen(@NonNull WebSocket socket, @NonNull Response response) {
                synchronized (ChatWebSocketClient.this) {
                    if (generation != socketGeneration || manuallyClosed) {
                        socket.close(1000, "Stale connection");
                        return;
                    }
                    connecting = false;
                    connected = true;
                    reconnectAttempt = 0;
                    webSocket = socket;
                    cancelReconnect();
                    startHeartbeat();
                }
                listener.onConnected();
            }

            @Override
            public void onMessage(@NonNull WebSocket socket, @NonNull String text) {
                try {
                    ChatSocketEvent event = RetrofitClient.getGson()
                            .fromJson(text, ChatSocketEvent.class);
                    if (event != null && event.type != null) {
                        listener.onEvent(event);
                    }
                } catch (Exception exception) {
                    listener.onError(exception);
                }
            }

            @Override
            public void onClosing(
                    @NonNull WebSocket socket,
                    int code,
                    @NonNull String reason
            ) {
                socket.close(code, reason);
            }

            @Override
            public void onClosed(
                    @NonNull WebSocket socket,
                    int code,
                    @NonNull String reason
            ) {
                handleSocketEnded(generation, code == 1008, null);
            }

            @Override
            public void onFailure(
                    @NonNull WebSocket socket,
                    @NonNull Throwable throwable,
                    Response response
            ) {
                boolean authenticationFailure = response != null
                        && (response.code() == 401 || response.code() == 403);
                handleSocketEnded(generation, authenticationFailure, throwable);
            }
        };
    }

    private void handleSocketEnded(
            long generation,
            boolean authenticationFailure,
            Throwable throwable
    ) {
        boolean shouldReconnect;
        synchronized (this) {
            if (generation != socketGeneration) {
                return;
            }
            stopHeartbeat();
            webSocket = null;
            connecting = false;
            connected = false;
            shouldReconnect = !manuallyClosed
                    && !authenticationFailure
                    && networkAvailable;
        }

        if (throwable != null) {
            listener.onError(throwable);
        } else if (authenticationFailure) {
            listener.onError(new SecurityException("Chat authentication failed"));
        }
        if (!manuallyClosed) {
            listener.onDisconnected();
        }
        if (shouldReconnect) {
            scheduleReconnect(nextReconnectDelaySeconds());
        }
    }

    private synchronized long nextReconnectDelaySeconds() {
        long delay = Math.min(1L << Math.min(reconnectAttempt, 5), MAX_RECONNECT_SECONDS);
        reconnectAttempt++;
        return delay;
    }

    private synchronized void scheduleReconnect(long delaySeconds) {
        if (manuallyClosed || !networkAvailable || connecting || connected) {
            return;
        }
        cancelReconnect();
        reconnectFuture = executor.schedule(
                this::openSocket,
                delaySeconds,
                TimeUnit.SECONDS
        );
    }

    private synchronized void cancelReconnect() {
        if (reconnectFuture != null) {
            reconnectFuture.cancel(false);
            reconnectFuture = null;
        }
    }

    private synchronized void startHeartbeat() {
        stopHeartbeat();
        heartbeatFuture = executor.scheduleAtFixedRate(
                this::sendPing,
                HEARTBEAT_SECONDS,
                HEARTBEAT_SECONDS,
                TimeUnit.SECONDS
        );
    }

    private synchronized void stopHeartbeat() {
        if (heartbeatFuture != null) {
            heartbeatFuture.cancel(false);
            heartbeatFuture = null;
        }
    }

    private void sendPing() {
        WebSocket socket;
        synchronized (this) {
            socket = webSocket;
        }
        if (socket == null) {
            return;
        }
        JsonObject ping = new JsonObject();
        ping.addProperty("type", "ping");
        if (!socket.send(RetrofitClient.getGson().toJson(ping))) {
            socket.cancel();
        }
    }

    private synchronized void registerNetworkCallback() {
        if (connectivityManager == null || networkCallback != null) {
            return;
        }
        networkCallback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(@NonNull Network network) {
                synchronized (ChatWebSocketClient.this) {
                    networkAvailable = true;
                    reconnectAttempt = 0;
                }
                scheduleReconnect(0);
            }

            @Override
            public void onLost(@NonNull Network network) {
                boolean available = hasInternetConnection();
                WebSocket socketToCancel = null;
                synchronized (ChatWebSocketClient.this) {
                    networkAvailable = available;
                    if (!available) {
                        cancelReconnect();
                        socketToCancel = webSocket;
                    }
                }
                if (socketToCancel != null) {
                    socketToCancel.cancel();
                } else if (!available && !manuallyClosed) {
                    listener.onDisconnected();
                }
            }

            @Override
            public void onCapabilitiesChanged(
                    @NonNull Network network,
                    @NonNull NetworkCapabilities capabilities
            ) {
                boolean available = capabilities.hasCapability(
                        NetworkCapabilities.NET_CAPABILITY_INTERNET
                );
                synchronized (ChatWebSocketClient.this) {
                    networkAvailable = available;
                }
                if (available) {
                    scheduleReconnect(0);
                }
            }
        };

        NetworkRequest request = new NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build();
        connectivityManager.registerNetworkCallback(request, networkCallback);
    }

    private synchronized void unregisterNetworkCallback() {
        if (connectivityManager == null || networkCallback == null) {
            return;
        }
        try {
            connectivityManager.unregisterNetworkCallback(networkCallback);
        } catch (IllegalArgumentException ignored) {
        }
        networkCallback = null;
    }

    private boolean hasInternetConnection() {
        if (connectivityManager == null) {
            return true;
        }
        Network activeNetwork = connectivityManager.getActiveNetwork();
        if (activeNetwork == null) {
            return false;
        }
        NetworkCapabilities capabilities =
                connectivityManager.getNetworkCapabilities(activeNetwork);
        return capabilities != null
                && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
    }

    private String buildWebSocketUrl() {
        String baseUrl = SupabaseConfig.BACKEND_URL;
        String webSocketBase;
        if (baseUrl.startsWith("https://")) {
            webSocketBase = "wss://" + baseUrl.substring("https://".length());
        } else if (baseUrl.startsWith("http://")) {
            webSocketBase = "ws://" + baseUrl.substring("http://".length());
        } else {
            throw new IllegalStateException("BACKEND_URL must start with http:// or https://");
        }
        if (!webSocketBase.endsWith("/")) {
            webSocketBase += "/";
        }
        return webSocketBase + "api/chat/ws";
    }
}
