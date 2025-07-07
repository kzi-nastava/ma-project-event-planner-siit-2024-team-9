package com.example.eventify.services.users;

import com.example.eventify.models.users.BusinessOwner;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface BusinessOwnerService {

    @GET("businessowner/{id}")
    Call<BusinessOwner> get(@Path("id") String id);
}
