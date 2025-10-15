package com.example.eventify.services.pdf;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Path;

public interface PdfService {
    
    @GET("pdf/event/{eventId}")
    Call<okhttp3.ResponseBody> getEventPdf(@Path("eventId") String eventId, @Header("Accept") String accept);
    
    @GET("pdf/price-list/{userId}")
    Call<okhttp3.ResponseBody> getPriceListPdf(@Path("userId") String userId, @Header("Accept") String accept);
    
    @GET("pdf/event/{eventId}/attendance")
    Call<okhttp3.ResponseBody> getAttendancePdf(@Path("eventId") String eventId, @Header("Accept") String accept);
}
