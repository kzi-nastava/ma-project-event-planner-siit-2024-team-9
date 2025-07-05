package com.example.eventify.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventify.R;
import com.example.eventify.models.others.Message;
import com.example.eventify.models.users.User;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    
    private static final int VIEW_TYPE_SENT = 1;
    private static final int VIEW_TYPE_RECEIVED = 2;
    
    private Context context;
    private List<Message> messages;
    private String currentUserId;

    public MessageAdapter(Context context, List<Message> messages, String currentUserId) {
        this.context = context;
        this.messages = messages;
        this.currentUserId = currentUserId;
    }

    @Override
    public int getItemViewType(int position) {
        if (messages == null || position < 0 || position >= messages.size()) {
            return VIEW_TYPE_RECEIVED; // Default fallback
        }
        
        Message message = messages.get(position);
        if (message == null || message.getSender() == null || message.getSender().getId() == null) {
            return VIEW_TYPE_RECEIVED; // Default fallback
        }
        
        if (message.getSender().getId().equals(currentUserId)) {
            return VIEW_TYPE_SENT;
        } else {
            return VIEW_TYPE_RECEIVED;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_SENT) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_message_sent, parent, false);
            return new SentMessageViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.item_message_received, parent, false);
            return new ReceivedMessageViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (messages == null || position < 0 || position >= messages.size()) {
            return; // Safety check
        }
        
        Message message = messages.get(position);
        if (message == null) {
            return; // Safety check
        }
        
        if (holder instanceof SentMessageViewHolder) {
            ((SentMessageViewHolder) holder).bind(message);
        } else if (holder instanceof ReceivedMessageViewHolder) {
            ((ReceivedMessageViewHolder) holder).bind(message);
        }
    }

    @Override
    public int getItemCount() {
        return messages != null ? messages.size() : 0;
    }

    public void addMessage(Message message) {
        if (messages == null || message == null) {
            return; // Safety check
        }
        
        messages.add(message);
        notifyItemInserted(messages.size() - 1);
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
        notifyDataSetChanged();
    }

    static class SentMessageViewHolder extends RecyclerView.ViewHolder {
        TextView messageText;
        TextView timeText;

        SentMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.text_message);
            timeText = itemView.findViewById(R.id.text_time);
        }

        void bind(Message message) {
            if (message == null) return;
            
            if (messageText != null) {
                messageText.setText(message.getContent() != null ? message.getContent() : "");
            }
            
            if (timeText != null) {
                timeText.setText(formatTime(message.getTimestamp()));
            }
        }
    }

    static class ReceivedMessageViewHolder extends RecyclerView.ViewHolder {
        TextView messageText;
        TextView timeText;
        TextView senderName;

        ReceivedMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.text_message);
            timeText = itemView.findViewById(R.id.text_time);
            senderName = itemView.findViewById(R.id.text_sender_name);
        }

        void bind(Message message) {
            if (message == null) return;
            
            if (messageText != null) {
                messageText.setText(message.getContent() != null ? message.getContent() : "");
            }
            
            if (timeText != null) {
                timeText.setText(formatTime(message.getTimestamp()));
            }
            
            if (senderName != null) {
                String senderEmail = "";
                if (message.getSender() != null && message.getSender().getEmail() != null) {
                    senderEmail = message.getSender().getEmail();
                }
                senderName.setText(senderEmail);
            }
        }
    }

    private static String formatTime(LocalDateTime timestamp) {
        if (timestamp == null) {
            return "";
        }
        
        try {
            return timestamp.format(DateTimeFormatter.ofPattern("HH:mm"));
        } catch (Exception e) {
            return ""; // Return empty string if formatting fails
        }
    }
} 