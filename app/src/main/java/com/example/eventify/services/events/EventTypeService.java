package com.example.eventify.services.events;

import com.example.eventify.models.events.EventType;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

import java.util.Collection;
import java.util.UUID;

public interface EventTypeService {

    // Create a new event type
    @POST("event-types")
    Call<EventType> add(@Body EventType eventType);

    // Get an event type by its ID
    @GET("event-types/{id}")
    Call<EventType> get(@Path("id") UUID id);

    // Get all event types
    @GET("event-types")
    Call<Collection<EventType>> getAll();

    // Update an event type by its ID
    @PUT("event-types/{id}")
    Call<EventType> update(@Path("id") UUID id, @Body EventType eventType);

    // Delete an event type by its ID
    @DELETE("event-types/{id}")
    Call<Void> delete(@Path("id") UUID id);
}
