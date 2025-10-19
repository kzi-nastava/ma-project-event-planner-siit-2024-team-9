package com.example.eventify.services.solutions;

import com.example.eventify.models.solutions.Product;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.*;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface ProductService {

    @Multipart
    @POST("products")
    Call<Product> add(@Part("product") RequestBody product, @Part List<MultipartBody.Part> images);

    @GET("products/{id}")
    Call<Product> get(@Path("id") UUID id);

    @GET("products")
    Call<Collection<Product>> getAll();

    @GET("products/owner/{ownerId}")
    Call<Collection<Product>> getByOwner(@Path("ownerId") UUID ownerId);

    @Multipart
    @PUT("products/{id}")
    Call<Product> update(@Path("id") UUID id, @Part("product") RequestBody updatedProduct, @Part List<MultipartBody.Part> images);

    @DELETE("products/{id}")
    Call<Void> delete(@Path("id") UUID id);

    @GET("products/filter")
    Call<Collection<Product>> filter(
            @Query("keyword") String keyword,
            @Query("category") String category,
            @Query("eventType") String eventType,
            @Query("price") Double price,
            @Query("availability") Boolean availability);

    @GET("products/search")
    Call<Collection<Product>> search(@Query("name") String name);
}
