package com.example.eventify.services.solutions;

import com.example.eventify.models.solutions.SolutionCategory;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

import java.util.Collection;
import java.util.UUID;

public interface SolutionCategoryService {

    // Create a new solution category
    @POST("solution-categories")
    Call<SolutionCategory> add(@Body SolutionCategory solutionCategory);

    // Get a specific solution category by ID
    @GET("solution-categories/{id}")
    Call<SolutionCategory> get(@Path("id") UUID id);

    // Get active solution categories
    @GET("solution-categories/active")
    Call<Collection<SolutionCategory>> getActive();

    // Get proposed solution categories
    @GET("solution-categories/proposed")
    Call<Collection<SolutionCategory>> getProposed();

    // Get specific solution categories based on parameters
    @GET("solution-categories/specific")
    Call<Collection<SolutionCategory>> getSpecific(
            @Query("categories") String categories,
            @Query("type") String type);

    // Update a solution category by ID
    @PUT("solution-categories/{id}")
    Call<SolutionCategory> update(@Path("id") UUID id, @Body SolutionCategory solutionCategory);

    // Delete a solution category by ID
    @DELETE("solution-categories/{id}")
    Call<Void> delete(@Path("id") UUID id);
}
