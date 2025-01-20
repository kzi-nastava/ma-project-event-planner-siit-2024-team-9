package com.example.eventify.services.solutions;

import com.example.eventify.models.solutions.Product;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

public interface ProductService {

    // Create a new product
    @POST("products")
    Call<Product> add(@Body Product product);

    // Get a product by ID
    @GET("products/{id}")
    Call<Product> get(@Path("id") UUID id);

    // Get all products
    @GET("products")
    Call<Collection<Product>> getAll();

    // Update a product by ID
    @PUT("products/{id}")
    Call<Product> update(@Path("id") UUID id, @Body Product product);

    // Delete a product by ID
    @DELETE("products/{id}")
    Call<Void> delete(@Path("id") UUID id);

    // Filter products by optional parameters
    @GET("products/filter")
    Call<Collection<Product>> filter(@QueryMap Map<String, String> filters);

    // Search products by name
    @GET("products/search")
    Call<Collection<Product>> searchByName(@Query("name") String name);

    // Get products by business owner ID
    @GET("products/businessOwner/{id}")
    Call<Collection<Product>> getByBusinessOwner(@Path("id") UUID id);
}
