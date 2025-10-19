package com.example.eventify.services.events;

import com.example.eventify.models.events.EventType;
import com.example.eventify.models.solutions.SolutionCategory;

import java.util.Collection;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface EventTypeService {

    @GET("event-types")
    Call<Collection<EventType>> getAll();

    @GET("event-types/{id}")
    Call<EventType> get(@Path("id") String id);

    @POST("event-types")
    Call<EventType> create(@Body EventType eventType);

    @PUT("event-types/{id}")
    Call<EventType> update(@Path("id") String id, @Body EventType eventType);

    @DELETE("event-types/{id}")
    Call<Void> delete(@Path("id") String id);
}
