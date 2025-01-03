package com.example.eventify.services;

import com.example.eventify.models.Event;
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

    String BASE_URL = "http://192.168.0.26:8080/api/";

    @POST("")
    Call<Event> create(@Body Event event);

    @GET("/{id}")
    Call<Event> get(@Path("id") String id);

    @GET("events")
    Call<EventAllResponse> getAllPaginated(@Query("page") int page, @Query("size") int size, @Query("sortBy") String sortBy, @Query("ascending") boolean ascending);

    @GET("events/top")
    Call<List<Event>> getTop();

    @PUT("/update/{id}")
    Call<Boolean> update(@Path("id") String id, @Body Event updatedEvent);

    @DELETE("/{id}")
    Call<Boolean> delete(@Path("id") String id);

    @GET("/filter")
    Call<EventAllResponse> filter(
            @QueryMap Map<String, String> filters,
            @Query("page") int page,
            @Query("size") int size,
            @Query("sortBy") String sortBy,
            @Query("ascending") boolean ascending
    );

    @GET("/stats")
    Call<Map<String, Double>> getStats();

    class EventAllResponse {
        @SerializedName("content")
        public List<Event> content;
        @SerializedName("totalElements")
        public int totalElements;
    }
    // Response wrapper for filter endpoint

}
