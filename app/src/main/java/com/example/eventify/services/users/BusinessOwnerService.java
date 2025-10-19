package com.example.eventify.services.users;

import com.example.eventify.models.users.User;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface BusinessOwnerService {

    @GET("businessowner/{id}")
    Call<User> get(@Path("id") String id);
}
