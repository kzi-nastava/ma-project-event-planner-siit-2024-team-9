package com.example.eventify.models.others;

import com.example.eventify.models.enums.NotificationType;
import com.example.eventify.models.users.User;

import java.util.Date;

public class Notification {

    private String id;           // UUID kao string
    private String title;
    private String message;
    private NotificationType type;
    private User receiver;
    private Date createdAt;
    private boolean isRead;

    public Notification() { }

    public Notification(String id, String title, String message, NotificationType type,
                        User receiver, Date createdAt, boolean isRead) {
        this.id = id;
        this.title = title;
        this.message = message;
        this.type = type;
        this.receiver = receiver;
        this.createdAt = createdAt;
        this.isRead = isRead;
    }

    public Notification(String title, String message, NotificationType type, User receiver, boolean isRead) {
        this(null, title, message, type, receiver, null, isRead);
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public NotificationType getType() { return type; }
    public void setType(NotificationType type) { this.type = type; }

    public User getReceiver() { return receiver; }
    public void setReceiver(User receiver) { this.receiver = receiver; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }
}