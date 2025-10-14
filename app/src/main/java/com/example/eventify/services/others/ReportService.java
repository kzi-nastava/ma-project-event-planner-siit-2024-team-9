package com.example.eventify.services.others;

import com.example.eventify.models.others.Report;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface ReportService {
    @POST("reports")
    Call<Report> add(@Body Report report);

    @GET("reports")
    Call<Collection<Report>> getAll();

    @GET("reports/pending")
    Call<Collection<Report>> getPending();

    @PUT("reports/approve/{userId}")
    Call<Boolean> approve(@Path("userId") UUID userId);

    @PUT("reports/decline/{userId}")
    Call<Boolean> decline(@Path("userId") UUID userId);

    @PUT("reports/{id}")
    Call<Report> update(@Path("id") String id, @Body Report report);

    @DELETE("reports/{id}")
    Call<Boolean> delete(@Path("id") String id);
}
