package com.example.eventify.services.solutions;

import com.example.eventify.models.solutions.Service;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.*;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface ServiceService {

    @POST("services")
    Call<Service> add(@Part("service") RequestBody service, @Part List<MultipartBody.Part> images);

    @GET("services/{id}")
    Call<Service> get(@Path("id") UUID id);

    @GET("services")
    Call<Collection<Service>> getAll();

    @Multipart
    @PUT("services/{id}")
    Call<Service> update(@Path("id") UUID id, @Part("service") RequestBody updatedService, @Part List<MultipartBody.Part> images);

    @DELETE("services/{id}")
    Call<Void> delete(@Path("id") UUID id);

    @GET("services/filter")
    Call<Collection<Service>> filter(
            @Query("search") String search,
            @Query("category") String category,
            @Query("type") String type,
            @Query("price") Double price,
            @Query("availability") Boolean availability);

    @GET("services/search")
    Call<Collection<Service>> search(@Query("name") String name);
}
