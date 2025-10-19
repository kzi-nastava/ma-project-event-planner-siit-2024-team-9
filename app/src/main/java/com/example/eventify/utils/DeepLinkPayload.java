package com.example.eventify.utils;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

public class DeepLinkPayload {
    public enum Type { JOIN_EVENT, REGISTER }

    public final Type type;
    public final String eventId;
    public final String email;

    public DeepLinkPayload(Type type, String eventId, String email) {
        this.type = type;
        this.eventId = eventId;
        this.email = email;
    }

    public static DeepLinkPayload fromIntent(Intent intent) {
        if (intent == null) return null;
        Uri data = intent.getData();
        if (data == null) return null;

        // Primere:
        // eventify://app/event/join/<EVENT_ID>
        // eventify://app/register?email=...&eventId=...

        String host = data.getHost();          // "app"
        String path = data.getPath();          // npr "/event/join/123e4567..."
        if (host == null || path == null) return null;

        if (path.startsWith("/event/join")) {
            // poslednji segment je eventId
            String last = data.getLastPathSegment();
            if (last != null && !last.isEmpty()) {
                return new DeepLinkPayload(Type.JOIN_EVENT, last, null);
            }
        } else if (path.startsWith("/register")) {
            String email = data.getQueryParameter("email");
            String eventId = data.getQueryParameter("eventId");
            return new DeepLinkPayload(Type.REGISTER, eventId, email);
        }

        return null;
    }

    public void putInto(Intent intent) {
        intent.putExtra("dl_type", type.name());
        if (eventId != null) intent.putExtra("dl_eventId", eventId);
        if (email != null) intent.putExtra("dl_email", email);
    }

    public static DeepLinkPayload fromBundle(Bundle b) {
        if (b == null) return null;
        String t = b.getString("dl_type", null);
        if (t == null) return null;
        Type type = Type.valueOf(t);
        String eventId = b.getString("dl_eventId", null);
        String email = b.getString("dl_email", null);
        return new DeepLinkPayload(type, eventId, email);
    }
}

