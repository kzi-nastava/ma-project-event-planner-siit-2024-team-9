package com.example.eventify.services.auth;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.eventify.models.auth.LoginRequest;
import com.example.eventify.models.auth.UserTokenState;
import com.example.eventify.utils.JwtUtils;
import com.example.eventify.utils.RetrofitClient;
import com.example.eventify.utils.UserSession;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginService {

    private static final String TAG = "LoginService";
    private ILoginService loginService;

    private Context context;
    private UserSession userSession;
    private static final String PREFS_NAME = "EventifyPrefs";
    private static final String TOKEN_KEY = "jwt_token";
    private static final String EXPIRY_KEY = "jwt_expiry";

    public interface LoginCallback {
        void onSuccess(String email, String role);
        void onFailure(String errorMessage);
        default void onSuspended(long remainingMs) {}
    }

    public LoginService(Context context) {
        this.context = context;
        this.userSession = new UserSession(context);
        this.loginService = RetrofitClient.getClient(this.context).create(ILoginService.class);
    }

    public void login(String email, String password, LoginCallback callback) {
        loginService.login(new LoginRequest(email, password)).enqueue(new Callback<UserTokenState>() {
            @Override
            public void onResponse(Call<UserTokenState> call, Response<UserTokenState> response) {
                if (response.isSuccessful()) {
                    UserTokenState uts = response.body();
                    if (uts == null) {
                        callback.onFailure("Invalid response from server");
                        return;
                    }

                    // 1) prvo proveri suspend
                    if (Boolean.TRUE.equals(uts.getSuspended())) {
                        long remaining = uts.getRemainingTime() != null ? uts.getRemainingTime() : 0L;
                        callback.onSuspended(remaining);
                        return;
                    }

                    // 2) pa tek onda token
                    String token = uts.getAccessToken();
                    if (token == null || token.isEmpty()) {
                        callback.onFailure("Invalid response from server");
                        return;
                    }

                    JwtUtils.JwtClaims claims = JwtUtils.decodeToken(token);
                    if (claims != null) {
                        userSession.saveUserSession(claims.getUserId(), claims.getEmail(), token);
                        saveToken(token, uts.getExpiresIn());
                        callback.onSuccess(claims.getEmail(), claims.getRole());
                    } else {
                        callback.onFailure("Failed to process authentication token");
                    }
                    return;
                }

                // ERROR grana (npr. 403 sa {suspended:true})
                int code = response.code();
                try {
                    String raw = response.errorBody() != null ? response.errorBody().string() : null;
                    if (raw != null && !raw.isEmpty()) {
                        com.google.gson.JsonObject obj = new com.google.gson.JsonParser().parse(raw).getAsJsonObject();
                        boolean suspended = obj.has("suspended") && obj.get("suspended").getAsBoolean();
                        if (suspended) {
                            long remaining = obj.has("remainingTime") ? obj.get("remainingTime").getAsLong() : 0L;
                            callback.onSuspended(remaining);
                            return;
                        }
                    }
                } catch (Exception ignore) {}

                String errorMessage = (code == 401 || code == 403) ? "Invalid email or password"
                        : (code >= 500 ? "Server error. Please try again later." : "Login failed");
                callback.onFailure(errorMessage);
            }

            @Override
            public void onFailure(Call<UserTokenState> call, Throwable t) {
                callback.onFailure("Network error: " + t.getMessage());
            }
        });
    }

    // Keep the old method for backward compatibility
    @Deprecated
    public void login(String email, String password) {
        login(email, password, new LoginCallback() {
            @Override
            public void onSuccess(String email, String role) {
                // Do nothing - handled by the caller
            }

            @Override
            public void onFailure(String errorMessage) {
                throw new RuntimeException(errorMessage);
            }
        });
    }

    private void saveToken(String token, long expiresIn) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        long expiryTimestamp = System.currentTimeMillis() + (expiresIn * 1000);  // Convert to absolute time

        editor.putString(TOKEN_KEY, token);
        editor.putLong(EXPIRY_KEY, expiryTimestamp);
        editor.apply();
    }

    public String getToken() {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        long expiryTimestamp = sharedPreferences.getLong(EXPIRY_KEY, 0);
        long currentTime = System.currentTimeMillis();

        if (currentTime >= expiryTimestamp) {
            logout();  // Token expired, remove it
            return null;
        }

        return sharedPreferences.getString(TOKEN_KEY, null);
    }

    public boolean isTokenValid() {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        long expiryTimestamp = sharedPreferences.getLong(EXPIRY_KEY, 0);
        return System.currentTimeMillis() < expiryTimestamp;
    }

    public void logout() {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(TOKEN_KEY);
        editor.remove(EXPIRY_KEY);
        editor.apply();
        
        // Clear user session as well
        userSession.clearSession();
    }

    private static final String MUTE_KEY = "notifications_muted";

    public boolean isNotificationsMuted() {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean(MUTE_KEY, false);
    }

    public void setNotificationsMuted(boolean muted) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(MUTE_KEY, muted);
        editor.apply();
    }
}
