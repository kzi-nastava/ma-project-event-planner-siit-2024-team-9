package com.example.eventify.services.others;


import java.util.UUID;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface PDFService {

    @GET("pdf/event/{eventId}")
    Call<ResponseBody> getEventPdf(@Path("eventId") UUID eventId);

    @GET("pdf/price-list/{userId}")
    Call<ResponseBody> getPriceListPdf(@Path("userId") UUID userId);

    @GET("pdf/event/{eventId}/attendance")
    Call<ResponseBody> getEventAttendancePdf(@Path("eventId") UUID eventId);
}
