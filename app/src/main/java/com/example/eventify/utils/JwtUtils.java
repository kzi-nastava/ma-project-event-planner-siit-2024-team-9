package com.example.eventify.utils;

import android.util.Base64;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class JwtUtils {
    private static final String TAG = "JwtUtils";

    public static class JwtClaims {
        private String email;
        private UUID userId;
        private String role;

        public JwtClaims(String email, UUID userId, String role) {
            this.email = email;
            this.userId = userId;
            this.role = role;
        }

        public String getEmail() { return email; }
        public UUID getUserId() { return userId; }
        public String getRole() { return role; }
    }

    public static JwtClaims decodeToken(String token) {
        try {
            // JWT format: header.payload.signature
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                Log.e(TAG, "Invalid JWT token format");
                return null;
            }

            // Decode the payload (second part)
            String payload = parts[1];
            
            // Add padding if needed for Base64 decoding
            int paddingLength = 4 - (payload.length() % 4);
            if (paddingLength != 4) {
                payload += "=".repeat(paddingLength);
            }

            // Decode Base64
            byte[] decodedBytes = Base64.decode(payload, Base64.URL_SAFE);
            String jsonPayload = new String(decodedBytes, StandardCharsets.UTF_8);
            
            Log.d(TAG, "Decoded JWT payload: " + jsonPayload);

            // Parse JSON
            Gson gson = new Gson();
            JsonObject claims = gson.fromJson(jsonPayload, JsonObject.class);

            // Extract user information
            String email = claims.has("sub") ? claims.get("sub").getAsString() : null;
            UUID userId = null;
            if (claims.has("id")) {
                try {
                    userId = UUID.fromString(claims.get("id").getAsString());
                } catch (Exception e) {
                    Log.e(TAG, "Error parsing user ID: " + e.getMessage());
                }
            }
            String role = claims.has("role") ? claims.get("role").getAsString() : null;

            if (email != null && userId != null) {
                return new JwtClaims(email, userId, role);
            } else {
                Log.e(TAG, "Missing required claims in JWT token");
                return null;
            }

        } catch (Exception e) {
            Log.e(TAG, "Error decoding JWT token: " + e.getMessage());
            return null;
        }
    }
} 