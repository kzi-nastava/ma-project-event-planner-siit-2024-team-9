package com.example.eventify.services.solutions;

import com.example.eventify.models.solutions.Service;

import retrofit2.Call;
import retrofit2.http.*;

import java.util.Collection;
import java.util.UUID;

public interface ServiceService {


    // Add a new service
    @POST("services")
    Call<Service> add(@Body Service service);

    // Get a service by its ID
    @GET("services/{id}")
    Call<Service> get(@Path("id") UUID id);

    // Get all services
    @GET("services")
    Call<Collection<Service>> getAll();

    // Update a service by its ID
    @PUT("services/{id}")
    Call<Service> update(@Path("id") UUID id, @Body Service updatedService);

    // Delete a service by its ID
    @DELETE("services/{id}")
    Call<Void> delete(@Path("id") UUID id);

    // Filter services by search criteria
    @GET("services/filter")
    Call<Collection<Service>> filter(
            @Query("search") String search,
            @Query("category") String category,
            @Query("type") String type,
            @Query("price") Double price,
            @Query("availability") Boolean availability);

    // Search services by name
    @GET("services/search")
    Call<Collection<Service>> search(@Query("name") String name);
}
