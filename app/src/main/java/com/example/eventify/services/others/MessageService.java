package com.example.eventify.services.others;

import com.example.eventify.models.others.Message;

import java.util.List;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface MessageService {
    
    @GET("messages/{senderId}/{recipientId}")
    Call<List<Message>> getMessages(@Path("senderId") UUID senderId, @Path("recipientId") UUID recipientId);
} 