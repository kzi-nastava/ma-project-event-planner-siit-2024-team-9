package com.example.eventify.utils;

import android.content.Context;

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
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class RetrofitClient {

    public static final String BASE_URL = "http://192.168.0.100:8080/api/";
private static Retrofit retrofit;

    public static Retrofit getClient(Context ctx) {
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
                    .addInterceptor(new AuthInterceptor(ctx.getApplicationContext()))
                    .addInterceptor(logging)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(ScalarsConverterFactory.create()) // Add support for ResponseBody
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
        static {
            for (SimpleDateFormat f : dateFormats) {
                f.setLenient(true);
            }
        }

        @Override
        public JsonElement serialize(Date src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(dateFormats[0].format(src));
        }

        @Override
        public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            try {
                if (json == null || json.isJsonNull()) return null;

                if (json.isJsonPrimitive() && json.getAsJsonPrimitive().isNumber()) {
                    long epoch = json.getAsLong();
                    return new Date(epoch);
                }

                if (json.isJsonPrimitive() && json.getAsJsonPrimitive().isString()) {
                    String s = json.getAsString().trim();
                    if (s.isEmpty()) return null;

                    if (s.matches("^\\d{10,}$")) {
                        long epoch = Long.parseLong(s);
                        if (s.length() == 10) { // epoch seconds
                            epoch *= 1000L;
                        }
                        return new Date(epoch);
                    }

                    for (SimpleDateFormat fmt : dateFormats) {
                        try {
                            return fmt.parse(s);
                        } catch (ParseException ignored) {}
                    }
                }

                if (json.isJsonArray()) {
                    JsonArray array = json.getAsJsonArray();
                    int year = array.get(0).getAsInt();
                    int month = array.get(1).getAsInt() - 1;
                    int day = array.get(2).getAsInt();
                    int hour = array.size() > 3 ? array.get(3).getAsInt() : 0;
                    int minute = array.size() > 4 ? array.get(4).getAsInt() : 0;
                    int second = array.size() > 5 ? array.get(5).getAsInt() : 0;

                    java.util.Calendar cal = java.util.Calendar.getInstance();
                    cal.set(year, month, day, hour, minute, second);
                    cal.set(java.util.Calendar.MILLISECOND, 0);
                    return cal.getTime();
                }

                throw new JsonParseException("Unexpected date format: " + json);
            } catch (Exception e) {
                android.util.Log.e("DateTypeAdapter", "Error deserializing Date: " + e.getMessage(), e);
                android.util.Log.e("DateTypeAdapter", "JSON value was: " + json);
                return new Date();
            }
        }
    }

}
