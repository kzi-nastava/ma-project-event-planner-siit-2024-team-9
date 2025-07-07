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
import java.util.Date;
import java.text.SimpleDateFormat;
import java.text.ParseException;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    public static final String BASE_URL = "http://192.168.1.6:8080/api/";
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
                    .registerTypeAdapter(Date.class, new DateTypeAdapter()) // Handle Date objects
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

    // Date Type Adapter to handle various date formats from backend
    private static class DateTypeAdapter implements JsonSerializer<Date>, JsonDeserializer<Date> {
        private static final SimpleDateFormat[] dateFormats = {
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss"),
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS"),
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS"),
            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"),
            new SimpleDateFormat("yyyy-MM-dd")
        };

        @Override
        public JsonElement serialize(Date src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(dateFormats[0].format(src));
        }

        @Override
        public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            try {
                if (json.isJsonArray()) {
                    // Handle array format [year, month, day, hour, minute, second]
                    JsonArray array = json.getAsJsonArray();
                    int year = array.get(0).getAsInt();
                    int month = array.get(1).getAsInt() - 1; // Month is 0-based in Date
                    int day = array.get(2).getAsInt();
                    int hour = array.size() > 3 ? array.get(3).getAsInt() : 0;
                    int minute = array.size() > 4 ? array.get(4).getAsInt() : 0;
                    int second = array.size() > 5 ? array.get(5).getAsInt() : 0;
                    
                    java.util.Calendar calendar = java.util.Calendar.getInstance();
                    calendar.set(year, month, day, hour, minute, second);
                    calendar.set(java.util.Calendar.MILLISECOND, 0);
                    return calendar.getTime();
                } else if (json.isJsonPrimitive()) {
                    String dateString = json.getAsString();
                    
                    // Try different date formats
                    for (SimpleDateFormat format : dateFormats) {
                        try {
                            return format.parse(dateString);
                        } catch (ParseException e) {
                            // Try next format
                        }
                    }
                    
                    throw new JsonParseException("Unable to parse date: " + dateString);
                }
                throw new JsonParseException("Unexpected date format");
            } catch (Exception e) {
                android.util.Log.e("DateTypeAdapter", "Error deserializing Date: " + e.getMessage(), e);
                android.util.Log.e("DateTypeAdapter", "JSON value was: " + json);
                return new Date(); // Fallback to current time
            }
        }
    }
}
