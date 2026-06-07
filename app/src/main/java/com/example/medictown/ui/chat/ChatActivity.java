package com.example.medictown.ui.chat;

import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medictown.R;
import com.example.medictown.data.api.RetrofitClient;
import com.example.medictown.data.api.SessionManager;
import com.example.medictown.data.models.Conversation;
import com.example.medictown.databinding.ActivityChatBinding;

public class ChatActivity extends AppCompatActivity {
    public static final String EXTRA_SELLER_MODE = "seller_mode";
    public static final String EXTRA_CONVERSATION_JSON = "conversation_json";
    public static final String EXTRA_CUSTOMER_NAME = "customer_name";

    private ActivityChatBinding binding;
    private ChatViewModel viewModel;
    private ChatMessageAdapter adapter;
    private LinearLayoutManager layoutManager;
    private boolean firstMessageRender = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setupWindowInsets();

        SessionManager sessionManager = new SessionManager(this);
        if (!sessionManager.isLoggedIn()) {
            Toast.makeText(this, "Vui lòng đăng nhập để chat với shop", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        viewModel = new ViewModelProvider(this).get(ChatViewModel.class);
        adapter = new ChatMessageAdapter(
                sessionManager.getUserId(),
                viewModel::retryMessage
        );
        setupRecyclerView();
        setupActions();
        observeViewModel();

        boolean sellerMode = getIntent().getBooleanExtra(EXTRA_SELLER_MODE, false);
        if (sellerMode) {
            String customerName = getIntent().getStringExtra(EXTRA_CUSTOMER_NAME);
            binding.toolbar.setTitle(
                    customerName == null || customerName.trim().isEmpty()
                            ? "Khách hàng"
                            : customerName
            );
        }

        if (viewModel.getConversation().getValue() == null) {
            if (sellerMode) {
                initializeSellerConversation(sessionManager);
            } else {
                viewModel.initializeCustomerChat(
                        sessionManager.getToken(),
                        sessionManager.getUserId()
                );
            }
        }
    }

    private void setupWindowInsets() {
        View root = binding.getRoot();
        int paddingLeft = root.getPaddingLeft();
        int paddingTop = root.getPaddingTop();
        int paddingRight = root.getPaddingRight();
        int paddingBottom = root.getPaddingBottom();

        ViewCompat.setOnApplyWindowInsetsListener(root, (view, windowInsets) -> {
            Insets systemBars = windowInsets.getInsets(
                    WindowInsetsCompat.Type.systemBars()
            );
            view.setPadding(
                    paddingLeft + systemBars.left,
                    paddingTop + systemBars.top,
                    paddingRight + systemBars.right,
                    paddingBottom + systemBars.bottom
            );
            return windowInsets;
        });
        ViewCompat.requestApplyInsets(root);
    }

    private void initializeSellerConversation(SessionManager sessionManager) {
        String conversationJson = getIntent().getStringExtra(EXTRA_CONVERSATION_JSON);
        Conversation selectedConversation = null;
        try {
            selectedConversation = RetrofitClient.getGson().fromJson(
                    conversationJson,
                    Conversation.class
            );
        } catch (Exception ignored) {
        }
        if (selectedConversation == null || selectedConversation.id == null) {
            Toast.makeText(this, "Không thể mở cuộc trò chuyện", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        viewModel.initializeConversation(
                selectedConversation,
                sessionManager.getToken(),
                sessionManager.getUserId()
        );
    }

    private void setupRecyclerView() {
        layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        binding.rvMessages.setLayoutManager(layoutManager);
        binding.rvMessages.setAdapter(adapter);
        binding.rvMessages.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (!recyclerView.canScrollVertically(-1)) {
                    viewModel.loadOlderMessages();
                }
            }
        });
    }

    private void setupActions() {
        binding.toolbar.setNavigationOnClickListener(view -> finish());
        binding.btnSend.setOnClickListener(view -> sendMessage());
        binding.edtMessage.setOnEditorActionListener((view, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                sendMessage();
                return true;
            }
            return false;
        });
    }

    private void observeViewModel() {
        viewModel.getMessages().observe(this, messageItems -> {
            int previousCount = adapter.getItemCount();
            int previousFirstPosition = layoutManager.findFirstVisibleItemPosition();
            View previousFirstView = layoutManager.findViewByPosition(previousFirstPosition);
            int previousTop = previousFirstView == null ? 0 : previousFirstView.getTop();
            boolean wasNearBottom = previousCount == 0
                    || layoutManager.findLastVisibleItemPosition() >= previousCount - 2;

            adapter.submitList(messageItems);
            int newCount = adapter.getItemCount();
            if (firstMessageRender || wasNearBottom) {
                if (newCount > 0) {
                    binding.rvMessages.scrollToPosition(newCount - 1);
                }
                firstMessageRender = false;
            } else if (newCount > previousCount && previousFirstPosition >= 0) {
                layoutManager.scrollToPositionWithOffset(
                        previousFirstPosition + (newCount - previousCount),
                        previousTop
                );
            }
        });

        viewModel.getLoading().observe(this, isLoading ->
                binding.progressLoading.setVisibility(
                        Boolean.TRUE.equals(isLoading) ? View.VISIBLE : View.GONE
                )
        );

        viewModel.getConnected().observe(this, isConnected -> {
            boolean connected = Boolean.TRUE.equals(isConnected);
            binding.tvConnectionStatus.setText(
                    connected ? "Đang trực tuyến" : "Đang kết nối..."
            );
            binding.tvConnectionStatus.setTextColor(
                    getColor(connected ? android.R.color.holo_green_dark : R.color.text_gray)
            );
        });

        viewModel.getError().observe(this, error -> {
            if (error != null && !error.trim().isEmpty()) {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendMessage() {
        String content = binding.edtMessage.getText().toString().trim();
        if (content.isEmpty()) {
            return;
        }
        viewModel.sendMessage(content);
        binding.edtMessage.setText("");
    }
}
