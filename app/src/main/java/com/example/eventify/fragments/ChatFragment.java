package com.example.eventify.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.activity.OnBackPressedCallback;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.eventify.adapters.MessageAdapter;
import com.example.eventify.databinding.FragmentChatBinding;
import com.example.eventify.models.others.Message;
import com.example.eventify.models.users.User;
import com.example.eventify.services.others.MessageService;
import com.example.eventify.services.others.WebSocketService;
import com.example.eventify.utils.NavigationManager;
import com.example.eventify.utils.RetrofitClient;
import com.example.eventify.utils.UserSession;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatFragment extends Fragment implements WebSocketService.WebSocketListener {

    private static final String TAG = "ChatFragment";
    private FragmentChatBinding binding;
    private MessageAdapter messageAdapter;
    private List<Message> messages;
    private WebSocketService webSocketService;
    private User currentUser;
    private User chatPartner;
    private UserSession userSession;
    private MessageService messageService;
    private NavigationManager navigationManager;
    private OnBackPressedCallback backPressedCallback;

    public static ChatFragment newInstance(User currentUser, User chatPartner) {
        ChatFragment fragment = new ChatFragment();
        Bundle args = new Bundle();
        args.putParcelable("current_user", currentUser);
        args.putParcelable("chat_partner", chatPartner);
        fragment.setArguments(args);
        return fragment;
    }

    public ChatFragment() {
        // Required empty public constructor for Android
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        logInfo("onCreate() called");
        
        userSession = new UserSession(requireContext());
        messageService = RetrofitClient.getClient().create(MessageService.class);
        
        // Get NavigationManager from activity
        if (requireActivity() instanceof NavigationManager) {
            navigationManager = (NavigationManager) requireActivity();
        }

        // Create back press callback but don't add it yet
        backPressedCallback = new OnBackPressedCallback(false) { // Start disabled
            @Override
            public void handleOnBackPressed() {
                handleBackPress();
            }
        };
        
        // Add the callback to the activity
        requireActivity().getOnBackPressedDispatcher().addCallback(this, backPressedCallback);

        if (getArguments() != null) {
            currentUser = getArguments().getParcelable("current_user");
            chatPartner = getArguments().getParcelable("chat_partner");
        }

        if (currentUser == null || chatPartner == null) {
            logError("Missing user information - currentUser: " + currentUser + ", chatPartner: " + chatPartner);
            if (navigationManager != null) {
                navigationManager.navigateBack();
            }
            return;
        }
        
        logInfo("Users loaded - Current: " + currentUser.getEmail() + ", Partner: " + chatPartner.getEmail());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentChatBinding.inflate(inflater, container, false);
        logInfo("onCreateView() called");
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        logInfo("onViewCreated() called");
        
        // Enable back press callback only when view is created
        if (backPressedCallback != null) {
            backPressedCallback.setEnabled(true);
        }
        
        setupUI();
        setupRecyclerView();
        setupWebSocket();
        loadPreviousMessages();
    }

    private void setupUI() {
        binding.textPartnerName.setText(chatPartner.getEmail());
        
        // Setup back arrow
        binding.backButton.setOnClickListener(v -> {
            if (navigationManager != null) {
                navigationManager.navigateBack();
            }
        });
        
        binding.btnSend.setOnClickListener(v -> sendMessage());
        
        binding.editMessage.setOnEditorActionListener((v, actionId, event) -> {
            sendMessage();
            return true;
        });
    }

    private void handleBackPress() {
        logInfo("handleBackPress() called");
        
        // Disable the callback to prevent conflicts
        if (backPressedCallback != null) {
            backPressedCallback.setEnabled(false);
        }
        
        // Clean up WebSocket connection
        if (webSocketService != null) {
            webSocketService.disconnect();
            webSocketService = null;
        }
        
        // Navigate back
        if (navigationManager != null) {
            navigationManager.navigateBack();
        }
    }

    private void setupRecyclerView() {
        messages = new ArrayList<>();
        messageAdapter = new MessageAdapter(requireContext(), messages, currentUser.getId());
        
        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
        layoutManager.setStackFromEnd(true);
        binding.recyclerMessages.setLayoutManager(layoutManager);
        binding.recyclerMessages.setAdapter(messageAdapter);
    }

    private void setupWebSocket() {
        try {
            webSocketService = new WebSocketService(currentUser);
            webSocketService.setListener(this);
            webSocketService.connect();
            logInfo("WebSocket setup initiated");
        } catch (Exception e) {
            logError("Failed to setup WebSocket", e);
            // Don't block the UI if WebSocket fails
            Toast.makeText(requireContext(), "Chat connection failed", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadPreviousMessages() {
        binding.progressBar.setVisibility(View.VISIBLE);
        logInfo("Loading previous messages...");
        
        // Safely parse UUIDs with error handling
        UUID senderId;
        UUID recipientId;
        
        try {
            senderId = UUID.fromString(currentUser.getId());
            recipientId = UUID.fromString(chatPartner.getId());
            logInfo("UUIDs parsed successfully");
        } catch (IllegalArgumentException e) {
            logError("UUID parsing failed", e);
            binding.progressBar.setVisibility(View.GONE);
            Toast.makeText(requireContext(), "Invalid user data", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Add timeout to prevent blocking
        messageService.getMessages(senderId, recipientId)
                .enqueue(new Callback<List<Message>>() {
                    @Override
                    public void onResponse(Call<List<Message>> call, Response<List<Message>> response) {
                        // Check if fragment is still active
                        if (!isAdded() || binding == null) {
                            logWarning("Fragment not active, skipping message load");
                            return;
                        }
                        
                        binding.progressBar.setVisibility(View.GONE);
                        
                        try {
                            logInfo("API Response - Code: " + response.code() + ", Success: " + response.isSuccessful());
                            
                            if (response.isSuccessful()) {
                                if (response.body() != null) {
                                    logInfo("Messages received: " + response.body().size());
                                    
                                    try {
                                        messages.clear();
                                        
                                        // Add messages directly (no conversion needed)
                                        for (Message message : response.body()) {
                                            messages.add(message);
                                        }
                                        
                                        logInfo("Messages processed: " + messages.size());
                                        
                                        // Sort messages by timestamp to show in correct order
                                        messages.sort((m1, m2) -> {
                                            if (m1.getTimestamp() == null || m2.getTimestamp() == null) {
                                                return 0;
                                            }
                                            return m1.getTimestamp().compareTo(m2.getTimestamp());
                                        });
                                        
                                        if (messageAdapter != null) {
                                            messageAdapter.notifyDataSetChanged();
                                        }
                                        
                                        scrollToBottom();
                                        logInfo("Message loading completed");
                                        
                                    } catch (Exception e) {
                                        logError("Error processing messages", e);
                                        showErrorDialog("Error", "Failed to process messages");
                                    }
                                } else {
                                    logWarning("Response body is null");
                                    showErrorDialog("No Messages", "No messages received from server");
                                }
                            } else {
                                logError("API Error - Code: " + response.code() + ", Message: " + response.message());
                                showErrorDialog("API Error", "Failed to load messages: " + response.message());
                            }
                            
                        } catch (Exception e) {
                            logError("Unexpected error in response handling", e);
                            showErrorDialog("Error", "Unexpected error occurred");
                        }
                    }

                    @Override
                    public void onFailure(Call<List<Message>> call, Throwable t) {
                        // Check if fragment is still active
                        if (!isAdded() || binding == null) {
                            logWarning("Fragment not active, skipping failure handling");
                            return;
                        }
                        
                        logError("API call failed", t);
                        binding.progressBar.setVisibility(View.GONE);
                        
                        String errorMessage = "Network error";
                        if (t.getMessage() != null && !t.getMessage().isEmpty()) {
                            errorMessage += ": " + t.getMessage();
                        }
                        showErrorDialog("Network Error", errorMessage);
                    }
                });
    }

    private void sendMessage() {
        String messageText = binding.editMessage.getText().toString().trim();
        if (messageText.isEmpty()) {
            return;
        }

        logInfo("Sending message: '" + messageText + "'");

        // Check if fragment is still active
        if (!isAdded() || binding == null) {
            logWarning("Fragment not active, cannot send message");
            return;
        }

        if (webSocketService != null && webSocketService.isConnected()) {
            try {
                // Pass the full chatPartner User object instead of just UUID
                webSocketService.sendMessage(messageText, chatPartner);
                
                // Add message to local list immediately for better UX
                Message localMessage = new Message(currentUser, chatPartner, messageText);
                if (messageAdapter != null) {
                    messageAdapter.addMessage(localMessage);
                    logInfo("Message sent successfully");
                } else {
                    logWarning("MessageAdapter is null");
                }
                
                binding.editMessage.setText("");
                scrollToBottom();
                
            } catch (Exception e) {
                logError("Error sending message", e);
                Toast.makeText(requireContext(), "Failed to send message", Toast.LENGTH_SHORT).show();
            }
        } else {
            logWarning("Cannot send message - WebSocket not connected");
            Toast.makeText(requireContext(), "Chat not connected", Toast.LENGTH_SHORT).show();
        }
    }

    private void scrollToBottom() {
        if (messages.size() > 0) {
            binding.recyclerMessages.post(() -> {
                binding.recyclerMessages.smoothScrollToPosition(messages.size() - 1);
                logInfo("Scrolled to bottom (position: " + (messages.size() - 1) + ")");
            });
        }
    }

    // Helper method to show error dialogs
    private void showErrorDialog(String title, String message) {
        new AlertDialog.Builder(getContext())
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .setCancelable(true)
                .show();
    }

    // Logging methods
    private void logInfo(String message) {
        android.util.Log.i(TAG, message);
        System.out.println("[INFO] " + TAG + ": " + message);
    }

    private void logWarning(String message) {
        android.util.Log.w(TAG, message);
        System.out.println("[WARNING] " + TAG + ": " + message);
    }

    private void logError(String message) {
        android.util.Log.e(TAG, message);
        System.err.println("[ERROR] " + TAG + ": " + message);
    }

    private void logError(String message, Throwable throwable) {
        android.util.Log.e(TAG, message, throwable);
        System.err.println("[ERROR] " + TAG + ": " + message);
        if (throwable != null) {
            throwable.printStackTrace();
        }
    }

    // WebSocketService.WebSocketListener methods
    @Override
    public void onMessageReceived(Message message) {
        // Check if fragment is still active
        if (!isAdded() || binding == null || messageAdapter == null) {
            logWarning("Fragment not active, skipping message received");
            return;
        }
        
        logInfo("WebSocket message received from: " + message.getSender().getEmail());
        
        if (message.getSender().getId().equals(chatPartner.getId()) && 
            message.getRecipient().getId().equals(currentUser.getId())) {
            
            // Use runOnUiThread to ensure UI updates happen on main thread
            requireActivity().runOnUiThread(() -> {
                if (messageAdapter != null) {
                    messageAdapter.addMessage(message);
                    scrollToBottom();
                    logInfo("Message added to chat from WebSocket");
                }
            });
        } else {
            logInfo("Message not for this chat, ignoring");
        }
    }

    @Override
    public void onConnectionEstablished() {
        // Check if fragment is still active
        if (!isAdded() || binding == null) {
            return;
        }
        
        logInfo("WebSocket connection established");
        
        requireActivity().runOnUiThread(() -> {
            if (binding != null) {
                binding.textConnectionStatus.setText("Connected");
                binding.textConnectionStatus.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onConnectionClosed() {
        // Check if fragment is still active
        if (!isAdded() || binding == null) {
            return;
        }
        
        logWarning("WebSocket connection closed");
        
        requireActivity().runOnUiThread(() -> {
            if (binding != null) {
                binding.textConnectionStatus.setText("Disconnected");
                binding.textConnectionStatus.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onError(String error) {
        // Check if fragment is still active
        if (!isAdded() || binding == null) {
            return;
        }
        
        logError("WebSocket error: " + error);
        
        requireActivity().runOnUiThread(() -> {
            if (binding != null) {
                binding.textConnectionStatus.setText("Connection Error");
                binding.textConnectionStatus.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        logInfo("onDestroyView() called");
        
        // Disable back press callback
        if (backPressedCallback != null) {
            backPressedCallback.setEnabled(false);
        }
        
        // Clean up WebSocket connection
        if (webSocketService != null) {
            webSocketService.disconnect();
        }
        
        binding = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        logInfo("onDestroy() called");
        
        // Final cleanup
        if (webSocketService != null) {
            webSocketService.disconnect();
            webSocketService = null;
        }
        
        // Remove callback reference
        if (backPressedCallback != null) {
            backPressedCallback.remove();
            backPressedCallback = null;
        }
    }
} 