package com.example.eventify.services.others;

import com.example.eventify.models.others.Notification;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.*;

public interface NotificationService {

    @POST("notifications")
    Call<Notification> add(@Body Notification notification);

    @GET("notifications")
    Call<List<Notification>> getAll();

    @GET("notifications/user/{userId}")
    Call<List<Notification>> getUserNotifications(@Path("userId") String userId);

    @PUT("notifications/{id}")
    Call<Notification> markAsRead(@Path("id") String id, @Body Notification notification);

    @DELETE("notifications/{id}")
    Call<Boolean> delete(@Path("id") String id);
}
