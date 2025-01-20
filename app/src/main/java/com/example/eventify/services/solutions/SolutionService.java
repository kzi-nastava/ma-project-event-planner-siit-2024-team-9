package com.example.eventify.services;

import com.example.eventify.models.solutions.Solution;
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

public interface SolutionService {

    String BASE_URL = "http://192.168.0.26:8080/api/";

    @POST("solutions")
    Call<Solution> create(@Body Solution solution);

    @GET("solutions/{id}")
    Call<Solution> get(@Path("id") String id);

    @GET("solutions")
    Call<SolutionAllResponse> getAllPaginated(@Query("page") int page, @Query("size") int size, @Query("sortBy") String sortBy, @Query("ascending") boolean ascending);

    @GET("solutions/top")
    Call<List<Solution>> getTop();

    @PUT("solutions/update/{id}")
    Call<Boolean> update(@Path("id") String id, @Body Solution updatedSolution);

    @DELETE("solutions/{id}")
    Call<Boolean> delete(@Path("id") String id);

    @GET("solutions/filter")
    Call<SolutionAllResponse> filter(
            @QueryMap Map<String, String> filters,
            @Query("page") int page,
            @Query("size") int size,
            @Query("sortBy") String sortBy,
            @Query("ascending") boolean ascending
    );

    @GET("solutions/stats")
    Call<Map<String, Double>> getStats();

    class SolutionAllResponse {
        @SerializedName("content")
        public List<Solution> content;
        @SerializedName("totalElements")
        public int totalElements;
    }
}
