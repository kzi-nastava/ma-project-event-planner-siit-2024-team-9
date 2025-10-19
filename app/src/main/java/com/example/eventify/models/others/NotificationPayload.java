package com.example.eventify.models.others;

import java.util.Date;

public class NotificationPayload {
    public String id;
    public String title;
    public String message;
    public String notificationType;
    public Date createdAt;
    public boolean isRead;
    // opcionalno: receiver (UserDTO), deepLink ako dodaš kasnije
}
