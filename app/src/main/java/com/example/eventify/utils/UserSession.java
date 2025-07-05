package com.example.eventify.utils;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.UUID;

public class UserSession {
    private static final String PREF_NAME = "user_session";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_AUTH_TOKEN = "auth_token";

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    public UserSession(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    public UUID getCurrentUserId() {
        String userIdString = sharedPreferences.getString(KEY_USER_ID, null);
        if (userIdString != null) {
            return UUID.fromString(userIdString);
        }
        return null;
    }

    public boolean isLoggedIn() {
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public String getUserEmail() {
        return sharedPreferences.getString(KEY_USER_EMAIL, null);
    }

    public String getAuthToken() {
        return sharedPreferences.getString(KEY_AUTH_TOKEN, null);
    }

    public void saveUserSession(UUID userId, String email, String authToken) {
        editor.putString(KEY_USER_ID, userId.toString());
        editor.putString(KEY_USER_EMAIL, email);
        editor.putString(KEY_AUTH_TOKEN, authToken);
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.apply();
    }

    public void clearSession() {
        editor.clear();
        editor.apply();
    }

    /**
     * Check if user is properly logged in with valid session data
     */
    public boolean isValidSession() {
        return isLoggedIn() && 
               getCurrentUserId() != null && 
               getUserEmail() != null && 
               getAuthToken() != null;
    }
} 