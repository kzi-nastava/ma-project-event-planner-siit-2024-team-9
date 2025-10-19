package com.example.eventify.services.users;

import com.example.eventify.models.events.Event;
import com.example.eventify.models.users.User;
import com.example.eventify.models.users.EventOrganizerDTO;
import com.example.eventify.models.users.BusinessOwnerDTO;
import com.example.eventify.models.solutions.Solution;

import java.util.List;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface UserService {

    @GET("users/{id}")
    Call<User> get(@Path("id") String id);

    @PUT("users/{id}")
    Call<User> update(@Path("id") String id, @Body User user);

    @GET("users/{userId}/favorites/check/{solutionId}")
    Call<Boolean> isFavorite(@Path("userId") UUID userId, @Path("solutionId") UUID solutionId);

    @POST("users/{userId}/favorites")
    Call<User> addToFavorites(@Path("userId") UUID userId, @Body Solution solutionDto);

    @DELETE("users/{userId}/favorites/{solutionId}")
    Call<User> removeFromFavorites(@Path("userId") UUID userId, @Path("solutionId") UUID solutionId);

    @GET("users/{userId}/favorites")
    Call<List<Solution>> getFavorites(@Path("userId") String userId);

    @POST("users/{userId}/favorites/events/")
    Call<User> addToFavoriteEvents(@Path("userId") String userId, @Body String eventId);

    @GET("users/{userId}/favorites/events/{eventId}")
    Call<Boolean> isFavoriteEvent(@Path("userId") String userId, @Path("eventId") String eventId);

    @DELETE("users/{userId}/favorites/events/{eventId}")
    Call<User> removeFromFavoriteEvents(@Path("userId") String userId, @Path("eventId") String eventId);

    @GET("users/{userId}/favorites/events")
    Call<List<Event>> getFavoriteEvents(@Path("userId") String userId);

    @GET("users/{userId}/calendar")
    Call<List<Event>> getCalendarEvents(@Path("userId") String userId);
    
    @PUT("users/{userId}")
    Call<User> updateEventOrganizer(@Path("userId") String userId, @Body EventOrganizerDTO user);
    
    @PUT("users/{userId}")
    Call<User> updateBusinessOwner(@Path("userId") String userId, @Body BusinessOwnerDTO user);

    @PUT("users/{userId}/password")
    Call<Void> changePassword(@Path("userId") String userId, @Body PasswordChangeRequest request);

    @PUT("users/{userId}/deactivate")
    Call<Void> deactivateAccount(@Path("userId") String userId);

    // Password change request model
    class PasswordChangeRequest {
        private String currentPassword;
        private String newPassword;

        public PasswordChangeRequest(String currentPassword, String newPassword) {
            this.currentPassword = currentPassword;
            this.newPassword = newPassword;
        }

        public String getCurrentPassword() {
            return currentPassword;
        }

        public void setCurrentPassword(String currentPassword) {
            this.currentPassword = currentPassword;
        }

        public String getNewPassword() {
            return newPassword;
        }

        public void setNewPassword(String newPassword) {
            this.newPassword = newPassword;
        }
    }
}

