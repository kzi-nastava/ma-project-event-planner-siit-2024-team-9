package com.example.eventify.services.events;

import com.example.eventify.models.events.EventType;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

public interface EventTypeService {
    @GET("event-types")
    Call<List<EventType>> getAllEventTypes();
    
    @GET("event-types")
    Call<List<EventType>> getAll();
}