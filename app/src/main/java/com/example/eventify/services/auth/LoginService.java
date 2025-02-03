package com.example.eventify.services.auth;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.eventify.models.auth.LoginRequest;
import com.example.eventify.models.auth.UserTokenState;
import com.example.eventify.utils.RetrofitClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginService {

    private ILoginService loginService = RetrofitClient.getClient().create(ILoginService.class);

    private Context context;
    private static final String PREFS_NAME = "EventifyPrefs";
    private static final String TOKEN_KEY = "jwt_token";
    private static final String EXPIRY_KEY = "jwt_expiry";



    public LoginService(Context context) {
        this.context = context;
    }


    public void login(String email, String password){
        Call<UserTokenState> call = loginService.login(new LoginRequest(email, password));
        call.enqueue(new Callback<UserTokenState>() {
            @Override
            public void onResponse(Call<UserTokenState> call, Response<UserTokenState> response) {
                if(response.isSuccessful()){
                    UserTokenState userTokenState = response.body();
                    if(userTokenState == null){
                        throw new RuntimeException("Error logging in");
                    }
                    // Save token  
                    saveToken(userTokenState.getAccessToken(), userTokenState.getExpiresIn());

                }
            }

            @Override
            public void onFailure(Call<UserTokenState> call, Throwable t) {
                // Handle error
                throw new RuntimeException("Error logging in");
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
    }

}
