package com.example.eventify.services.events;

import com.example.eventify.models.events.Budget;
import com.example.eventify.models.events.BudgetItem;
import com.example.eventify.models.solutions.Product;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

import java.util.List;
import java.util.UUID;

public interface BudgetService {

    // Create a new budget
    @POST("budgets")
    Call<Budget> add(@Body Budget budget);

    // Get a budget by its ID
    @GET("budgets/{id}")
    Call<Budget> get(@Path("id") UUID id);

    // Get budget items by budget ID
    @GET("budgets/items/{id}")
    Call<List<BudgetItem>> getBudgetItems(@Path("id") UUID id);

    // Update a budget by its ID
    @PUT("budgets/update/{id}")
    Call<Budget> update(@Path("id") UUID id, @Body Budget budget);

    // Buy a product in a budget
    @PUT("budgets/buy/{id}")
    Call<Budget> buy(@Path("id") UUID id, @Body Product product);

    // Delete a budget by its ID
    @DELETE("budgets/{id}")
    Call<Void> delete(@Path("id") UUID id);
}
