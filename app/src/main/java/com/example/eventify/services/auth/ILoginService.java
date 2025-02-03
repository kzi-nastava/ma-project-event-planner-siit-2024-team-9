package com.example.eventify.services.auth;

import com.example.eventify.models.auth.LoginRequest;
import com.example.eventify.models.auth.UserTokenState;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ILoginService {

    @POST("users/login")
    Call<UserTokenState> login(@Body LoginRequest loginRequest);
}
