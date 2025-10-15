package com.example.eventify.services.auth;

import com.example.eventify.models.auth.BusinessOwnerRegistrationRequest;
import com.example.eventify.models.auth.EventOrganizerRegistrationRequest;
import com.example.eventify.models.users.User;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface RegistrationService {
    
    @Multipart
    @POST("users")
    Call<User> registerEventOrganizer(
            @Part("dto") RequestBody dto,
            @Part MultipartBody.Part profileImageFile
    );
    
    @Multipart
    @POST("businessowners")
    Call<User> registerBusinessOwner(
            @Part("dto") RequestBody dto,
            @Part MultipartBody.Part profileImageFile,
            @Part MultipartBody.Part... businessImages
    );
}
