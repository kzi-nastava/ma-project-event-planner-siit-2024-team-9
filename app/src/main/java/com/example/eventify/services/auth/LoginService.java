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
    private ILoginService loginService = RetrofitClient.getClient().create(ILoginService.class);

    private Context context;
    private UserSession userSession;
    private static final String PREFS_NAME = "EventifyPrefs";
    private static final String TOKEN_KEY = "jwt_token";
    private static final String EXPIRY_KEY = "jwt_expiry";

    public interface LoginCallback {
        void onSuccess(String email, String role);
        void onFailure(String errorMessage);
    }

    public LoginService(Context context) {
        this.context = context;
        this.userSession = new UserSession(context);
    }

    public void login(String email, String password, LoginCallback callback) {
        loginService.login(new LoginRequest(email, password)).enqueue(new Callback<UserTokenState>() {
            @Override
            public void onResponse(Call<UserTokenState> call, Response<UserTokenState> response) {
                if (response.isSuccessful()) {
                    UserTokenState userTokenState = response.body();
                    if (userTokenState == null) {
                        callback.onFailure("Invalid response from server");
                        return;
                    }
                    
                    // Extract user information from JWT token
                    String token = userTokenState.getAccessToken();
                    JwtUtils.JwtClaims claims = JwtUtils.decodeToken(token);
                    
                    if (claims != null) {
                        // Save user session with decoded information
                        userSession.saveUserSession(claims.getUserId(), claims.getEmail(), token);
                        Log.i(TAG, "Login successful for user: " + claims.getEmail() + " with role: " + claims.getRole());
                        
                        // Save token for backward compatibility
                        saveToken(token, userTokenState.getExpiresIn());
                        
                        callback.onSuccess(claims.getEmail(), claims.getRole());
                    } else {
                        Log.e(TAG, "Failed to decode JWT token");
                        callback.onFailure("Failed to process authentication token");
                    }
                } else {
                    Log.e(TAG, "Login failed with response code: " + response.code());
                    String errorMessage = "Login failed";
                    if (response.code() == 401) {
                        errorMessage = "Invalid email or password";
                    } else if (response.code() >= 500) {
                        errorMessage = "Server error. Please try again later.";
                    }
                    callback.onFailure(errorMessage);
                }
            }

            @Override
            public void onFailure(Call<UserTokenState> call, Throwable t) {
                Log.e(TAG, "Login failed: " + t.getMessage());
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
}
