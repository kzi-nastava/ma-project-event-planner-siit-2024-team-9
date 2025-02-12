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


    // Get a budget by its ID
    @GET("budgets/{id}")
    Call<Budget> get(@Path("id") UUID id);

    @GET("budgets/items/{id}")
    Call<List<BudgetItem>> getItems(@Path("id") UUID id);

    @PUT("budgets/update/{id}")
    Call<Budget> update(@Path("id") UUID id, @Body Budget budget);

    @PUT("budgets/buy/{id}")
    Call<Budget> buy(@Path("id") UUID id, @Body Product product);


}
