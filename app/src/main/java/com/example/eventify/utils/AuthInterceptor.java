package com.example.eventify.utils;

import android.content.Context;

import androidx.annotation.NonNull;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AuthInterceptor implements Interceptor {
    private final Context appContext;

    public AuthInterceptor(Context context) {
        this.appContext = context.getApplicationContext();
    }

    @NonNull @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Request original = chain.request();

        String token = new UserSession(appContext).getAuthToken();

        Request.Builder b = original.newBuilder()
                .header("Accept", "application/json");

        if (token != null && !token.isEmpty()) {
            b.header("Authorization", "Bearer " + token);
        }

        return chain.proceed(b.build());
    }
}