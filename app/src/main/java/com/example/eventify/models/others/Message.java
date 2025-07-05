package com.example.eventify.models.others;

import android.os.Parcel;
import android.os.Parcelable;

import com.example.eventify.models.users.User;
import com.google.gson.annotations.SerializedName;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class Message implements Parcelable {
    @SerializedName("id")
    private String id;
    
    @SerializedName("sender")
    private User sender;
    
    @SerializedName("recipient")
    private User recipient;
    
    @SerializedName("content")
    private String content;
    
    @SerializedName("timestamp")
    private LocalDateTime timestamp;

    public Message() {}

    public Message(String id, User sender, User recipient, String content, LocalDateTime timestamp) {
        this.id = id;
        this.sender = sender;
        this.recipient = recipient;
        this.content = content;
        this.timestamp = timestamp;
    }

    public Message(User sender, User recipient, String content) {
        this.sender = sender;
        this.recipient = recipient;
        this.content = content;
        this.timestamp = LocalDateTime.now();
    }

    // Constructor for backward compatibility with string timestamp
    public Message(String id, User sender, User recipient, String content, String timestampStr) {
        this.id = id;
        this.sender = sender;
        this.recipient = recipient;
        this.content = content;
        this.timestamp = timestampStr != null ? LocalDateTime.parse(timestampStr) : LocalDateTime.now();
    }

    protected Message(Parcel in) {
        id = in.readString();
        sender = in.readParcelable(User.class.getClassLoader());
        recipient = in.readParcelable(User.class.getClassLoader());
        content = in.readString();
        String timestampStr = in.readString();
        timestamp = timestampStr != null ? LocalDateTime.parse(timestampStr) : null;
    }

    public static final Creator<Message> CREATOR = new Creator<Message>() {
        @Override
        public Message createFromParcel(Parcel in) {
            return new Message(in);
        }

        @Override
        public Message[] newArray(int size) {
            return new Message[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeParcelable(sender, flags);
        dest.writeParcelable(recipient, flags);
        dest.writeString(content);
        dest.writeString(timestamp != null ? timestamp.toString() : null);
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public User getSender() { return sender; }
    public void setSender(User sender) { this.sender = sender; }

    public User getRecipient() { return recipient; }
    public void setRecipient(User recipient) { this.recipient = recipient; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    // Backward compatibility method for string timestamp
    public void setTimestamp(String timestampStr) {
        this.timestamp = timestampStr != null ? LocalDateTime.parse(timestampStr) : null;
    }
    
    // Backward compatibility method to get timestamp as string
    public String getTimestampString() {
        return timestamp != null ? timestamp.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null;
    }
}
