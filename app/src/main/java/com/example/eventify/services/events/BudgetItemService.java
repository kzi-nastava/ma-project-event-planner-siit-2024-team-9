package com.example.eventify.services.events;

import com.example.eventify.models.events.BudgetItem;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

import java.util.UUID;

public interface BudgetItemService {

    // Create a new budget item
    @POST("budget-items")
    Call<BudgetItem> add(@Body BudgetItem budgetItem);

    // Get a budget item by its ID
    @GET("budget-items/{id}")
    Call<BudgetItem> get(@Path("id") UUID id);

    // Update a budget item by its ID
    @PUT("budget-items/{id}")
    Call<BudgetItem> update(@Path("id") UUID id, @Body BudgetItem budgetItem);

    // Delete a budget item by its ID
    @DELETE("budget-items/{id}")
    Call<Void> delete(@Path("id") UUID id);
}
