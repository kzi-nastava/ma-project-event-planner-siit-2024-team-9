package com.example.eventify.utils;

import com.example.eventify.models.users.User;
import com.example.eventify.models.users.BusinessOwner;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.JsonArray;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    public static final String BASE_URL = "http://192.168.1.5:8080/api/";
    private static Retrofit retrofit;

    public static Retrofit getClient() {
        if (retrofit == null) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            UserTypeAdapter userTypeAdapter = new UserTypeAdapter();
            
            Gson gson = new GsonBuilder()
                    .setDateFormat("yyyy-MM-dd'T'HH:mm:ss") // Handle LocalDateTime format from backend
                    .registerTypeAdapter(User.class, userTypeAdapter) // Add polymorphic User handling
                    .registerTypeAdapter(BusinessOwner.class, userTypeAdapter) // Add polymorphic BusinessOwner handling
                    .registerTypeAdapter(UUID.class, new UUIDTypeAdapter()) // Handle UUID serialization
                    .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeTypeAdapter()) // Handle LocalDateTime
                    .setLenient() // Be more lenient with JSON parsing
                    .create();

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();
        }
        return retrofit;
    }

    // UUID Type Adapter
    private static class UUIDTypeAdapter implements JsonSerializer<UUID>, JsonDeserializer<UUID> {
        @Override
        public JsonElement serialize(UUID src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(src.toString());
        }

        @Override
        public UUID deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            try {
                return UUID.fromString(json.getAsString());
            } catch (Exception e) {
                android.util.Log.e("UUIDTypeAdapter", "Error deserializing UUID: " + e.getMessage(), e);
                return null;
            }
        }
    }

    // LocalDateTime Type Adapter
    private static class LocalDateTimeTypeAdapter implements JsonSerializer<LocalDateTime>, JsonDeserializer<LocalDateTime> {
        private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

        @Override
        public JsonElement serialize(LocalDateTime src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(src.format(formatter));
        }

        @Override
        public LocalDateTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            try {
                if (json.isJsonArray()) {
                    // Handle array format [year, month, day, hour, minute, second]
                    JsonArray array = json.getAsJsonArray();
                    int year = array.get(0).getAsInt();
                    int month = array.get(1).getAsInt();
                    int day = array.get(2).getAsInt();
                    int hour = array.get(3).getAsInt();
                    int minute = array.get(4).getAsInt();
                    int second = array.get(5).getAsInt();
                    
                    return LocalDateTime.of(year, month, day, hour, minute, second);
                } else if (json.isJsonPrimitive()) {
                    String dateString = json.getAsString();
                    // Handle different possible formats
                    if (dateString.contains("T")) {
                        // Remove microseconds if present
                        if (dateString.contains(".")) {
                            dateString = dateString.substring(0, dateString.indexOf('.'));
                        }
                        return LocalDateTime.parse(dateString, formatter);
                    }
                    return LocalDateTime.parse(dateString);
                }
                throw new JsonParseException("Unexpected timestamp format");
            } catch (Exception e) {
                android.util.Log.e("LocalDateTimeTypeAdapter", "Error deserializing LocalDateTime: " + e.getMessage(), e);
                android.util.Log.e("LocalDateTimeTypeAdapter", "JSON value was: " + json);
                return LocalDateTime.now(); // Fallback to current time
            }
        }
    }
}
