package com.example.eventify.utils;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

public class DeepLinkStorage {
    private static final String PREF = "deeplink_pref";
    private static final String KEY_TYPE = "type";
    private static final String KEY_EVENT = "eventId";
    private static final String KEY_EMAIL = "email";

    public static void save(Context ctx, DeepLinkPayload p) {
        SharedPreferences sp = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE);
        sp.edit()
                .putString(KEY_TYPE, p.type.name())
                .putString(KEY_EVENT, p.eventId)
                .putString(KEY_EMAIL, p.email)
                .apply();
    }

    public static DeepLinkPayload pop(Context ctx) {
        SharedPreferences sp = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE);
        String t = sp.getString(KEY_TYPE, null);
        if (t == null) return null;
        String event = sp.getString(KEY_EVENT, null);
        String email = sp.getString(KEY_EMAIL, null);
        sp.edit().clear().apply();
        return new DeepLinkPayload(DeepLinkPayload.Type.valueOf(t), event, email);
    }
}
