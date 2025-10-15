package com.example.eventify.services.events;

import com.example.eventify.models.users.User;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface EventFavoritesService {
    
    @POST("users/{userId}/favorites/events/")
    Call<User> addToFavoriteEvents(@Path("userId") String userId, @Body EventFavoriteRequest request);
    
    @GET("users/{userId}/favorites/events/{eventId}")
    Call<Boolean> isFavoriteEvent(@Path("userId") String userId, @Path("eventId") String eventId);
    
    @DELETE("users/{userId}/favorites/events/{eventId}")
    Call<User> removeFromFavoriteEvents(@Path("userId") String userId, @Path("eventId") String eventId);
    
    class EventFavoriteRequest {
        private String eventId;
        
        public EventFavoriteRequest(String eventId) {
            this.eventId = eventId;
        }
        
        public String getEventId() {
            return eventId;
        }
        
        public void setEventId(String eventId) {
            this.eventId = eventId;
        }
    }
}
