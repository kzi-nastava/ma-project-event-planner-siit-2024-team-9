package com.example.eventify.utils;

import com.example.eventify.models.users.BusinessOwner;
import com.example.eventify.models.users.User;
import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;

public class UserTypeAdapter implements JsonDeserializer<User>, JsonSerializer<User> {

    private final Gson defaultGson = new Gson(); // For default deserialization

    @Override
    public User deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        if (json == null || json.isJsonNull()) {
            return null;
        }
        
        try {
            JsonObject jsonObject = json.getAsJsonObject();
            
            // Check if there's a role field to determine the user type
            if (jsonObject.has("role") && !jsonObject.get("role").isJsonNull()) {
                JsonElement roleElement = jsonObject.get("role");
                if (roleElement.isJsonObject()) {
                    JsonObject roleObject = roleElement.getAsJsonObject();
                    if (roleObject.has("name") && !roleObject.get("name").isJsonNull()) {
                        String roleName = roleObject.get("name").getAsString();
                        
                        // Deserialize based on role type
                        switch (roleName) {
                            case "BUSINESS_OWNER":
                                return defaultGson.fromJson(json, BusinessOwner.class);
                            case "EVENT_ORGANIZER":
                                // If you have EventOrganizer class, deserialize to it
                                // return defaultGson.fromJson(json, EventOrganizer.class);
                            case "ADMIN":
                                // If you have Admin class, deserialize to it
                                // return defaultGson.fromJson(json, Admin.class);
                            case "AUTHENTICATED_USER":
                            default:
                                // Fall back to base User class
                                return defaultGson.fromJson(json, User.class);
                        }
                    }
                }
            }
            
            // If no role information, deserialize as base User
            return defaultGson.fromJson(json, User.class);
        } catch (Exception e) {
            // Log the error and return a default User object or null
            android.util.Log.e("UserTypeAdapter", "Error deserializing User: " + e.getMessage(), e);
            try {
                // Try to deserialize as base User as fallback
                return defaultGson.fromJson(json, User.class);
            } catch (Exception fallbackException) {
                android.util.Log.e("UserTypeAdapter", "Fallback deserialization also failed: " + fallbackException.getMessage(), fallbackException);
                return null;
            }
        }
    }

    @Override
    public JsonElement serialize(User src, Type typeOfSrc, JsonSerializationContext context) {
        if (src == null) {
            return null;
        }
        
        try {
            // Use default serialization first
            JsonElement jsonElement = defaultGson.toJsonTree(src, src.getClass());
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            
            // Add the role.name property at the root level for Jackson polymorphic deserialization
            if (src.getRole() != null && src.getRole().getName() != null) {
                jsonObject.addProperty("role.name", src.getRole().getName().toString());
            }
            
            return jsonObject;
        } catch (Exception e) {
            // Log the error and return a simple JSON object
            android.util.Log.e("UserTypeAdapter", "Error serializing User: " + e.getMessage(), e);
            JsonObject fallbackObject = new JsonObject();
            if (src.getId() != null) {
                fallbackObject.addProperty("id", src.getId());
            }
            if (src.getEmail() != null) {
                fallbackObject.addProperty("email", src.getEmail());
            }
            return fallbackObject;
        }
    }
} 