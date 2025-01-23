package com.example.eventify.services.solutions;

import com.example.eventify.models.solutions.Review;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

import java.util.Collection;
import java.util.UUID;

public interface ReviewService {

    // Get all reviews
    @GET("reviews")
    Call<Collection<Review>> getAll();

    // Get pending reviews
    @GET("reviews/pending")
    Call<Collection<Review>> getPending();

    // Get all reviews for a specific solution
    @GET("reviews/solution/{id}")
    Call<Collection<Review>> getBySolution(@Path("id") UUID solutionId);

    // Get reviews by owner
    @GET("reviews/owner/{id}")
    Call<Collection<Review>> getByOwner(@Path("id") UUID ownerId);

    // Get a specific review by ID
    @GET("reviews/{id}")
    Call<Review> get(@Path("id") UUID id);

    // Create a new review
    @POST("reviews")
    Call<Review> add(@Body Review review);

    // Update a review by ID
    @PUT("reviews/{id}")
    Call<Review> update(@Path("id") UUID id, @Body Review review);

    // Delete a review by ID
    @DELETE("reviews/{id}")
    Call<Boolean> delete(@Path("id") UUID id);
}
