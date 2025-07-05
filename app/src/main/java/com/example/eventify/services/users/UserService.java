package com.example.eventify.services.users;

import com.example.eventify.models.events.Event;
import com.example.eventify.models.users.User;
import com.example.eventify.models.solutions.Solution;

import java.util.UUID;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface UserService {

    @GET("users/{id}")
    Call<User> get(@Path("id") String id);

    @GET("users/{userId}/favorites/check/{solutionId}")
    Call<Boolean> isFavorite(@Path("userId") UUID userId, @Path("solutionId") UUID solutionId);

    @POST("users/{userId}/favorites")
    Call<User> addToFavorites(@Path("userId") UUID userId, @Body Solution solutionDto);

    @DELETE("users/{userId}/favorites/{solutionId}")
    Call<User> removeFromFavorites(@Path("userId") UUID userId, @Path("solutionId") UUID solutionId);

}

