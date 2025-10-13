package com.example.eventify.services.solutions;

import com.example.eventify.models.solutions.reservations.CreateServiceReservationRequest;
import com.example.eventify.models.solutions.reservations.ServiceReservation;
import com.example.eventify.models.enums.Status;

import java.util.Collection;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.http.*;

public interface ServiceReservationService {

    @POST("service-reservations")
    Call<ServiceReservation> create(@Body CreateServiceReservationRequest payload);

    @GET("service-reservations/{id}")
    Call<ServiceReservation> get(@Path("id") UUID id);

    @GET("service-reservations")
    Call<Collection<ServiceReservation>> getAll(@Query("status") Status status);

    @GET("service-reservations/service/{serviceId}")
    Call<Collection<ServiceReservation>> byService(@Path("serviceId") UUID serviceId);

    @GET("service-reservations/provider/{providerId}")
    Call<Collection<ServiceReservation>> byProvider(@Path("providerId") UUID providerId);

    @GET("service-reservations/customer/{customerId}")
    Call<Collection<ServiceReservation>> byCustomer(@Path("customerId") UUID customerId);

    @GET("service-reservations/provider/{providerId}/between")
    Call<Collection<ServiceReservation>> providerBetween(@Path("providerId") UUID providerId,
                                                         @Query("from") String fromIso,
                                                         @Query("to") String toIso);

    @GET("service-reservations/customer/{customerId}/between")
    Call<Collection<ServiceReservation>> customerBetween(@Path("customerId") UUID customerId,
                                                         @Query("from") String fromIso,
                                                         @Query("to") String toIso);

    @PUT("service-reservations/approve/{id}")
    Call<Boolean> approve(@Path("id") UUID id);

    @PUT("service-reservations/decline/{id}")
    Call<Boolean> decline(@Path("id") UUID id);

    @PUT("service-reservations/cancel/{id}")
    Call<Boolean> cancel(@Path("id") UUID id);

    @DELETE("service-reservations/{id}")
    Call<Boolean> delete(@Path("id") UUID id);
}
