package com.example.eventify.services.events;

import com.example.eventify.models.events.Budget;
import com.example.eventify.models.events.Event;
import com.example.eventify.models.solutions.Service;
import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;

public interface EventService {

    @POST("events")
    Call<Event> create(@Body Event event);

    @GET("events/{id}")
    Call<Event> get(@Path("id") String id);

    @GET("events/name")
    Call<Event> getByName(@Query("name") String name);

    @GET("events/budget/{id}")
    Call<Budget> getBudget(@Path("id") String id);

    @GET("events")
    Call<EventAllResponse> getAllPaginated(@Query("page") int page, @Query("size") int size, @Query("sortBy") String sortBy, @Query("ascending") boolean ascending);

    @GET("events/top")
    Call<List<Event>> getTop();

    @PUT("events/update/{id}")
    Call<Boolean> update(@Path("id") String id, @Body Event updatedEvent);

    @DELETE("events/{id}")
    Call<Boolean> delete(@Path("id") String id);

    @GET("events/filter")
    Call<EventAllResponse> filter(
            @QueryMap Map<String, String> filters,
            @Query("page") int page,
            @Query("size") int size,
            @Query("sortBy") String sortBy,
            @Query("ascending") boolean ascending
    );

    @GET("events/stats")
    Call<Map<String, Double>> getStats();

    class EventAllResponse {
        @SerializedName("content")
        public List<Event> content;
        @SerializedName("totalElements")
        public int totalElements;
    }
    // Response wrapper for filter endpoint

}
