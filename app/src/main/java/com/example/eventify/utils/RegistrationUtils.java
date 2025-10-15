package com.example.eventify.utils;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.example.eventify.models.auth.BusinessOwnerRegistrationRequest;
import com.example.eventify.models.auth.EventOrganizerRegistrationRequest;
import com.google.gson.Gson;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class RegistrationUtils {
    private static final String TAG = "RegistrationUtils";

    public static RequestBody createJsonRequestBody(Object object) {
        Gson gson = new Gson();
        String json = gson.toJson(object);
        return RequestBody.create(MediaType.parse("application/json"), json);
    }

    public static MultipartBody.Part createImagePart(Context context, Uri imageUri, String fieldName) {
        try {
            File file = createFileFromUri(context, imageUri);
            if (file != null) {
                RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), file);
                return MultipartBody.Part.createFormData(fieldName, file.getName(), requestFile);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error creating image part", e);
        }
        return null;
    }

    public static MultipartBody.Part[] createImageParts(Context context, Uri[] imageUris, String fieldName) {
        MultipartBody.Part[] parts = new MultipartBody.Part[imageUris.length];
        for (int i = 0; i < imageUris.length; i++) {
            parts[i] = createImagePart(context, imageUris[i], fieldName);
        }
        return parts;
    }

    private static File createFileFromUri(Context context, Uri uri) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            if (inputStream == null) return null;

            File tempFile = File.createTempFile("temp_image", ".jpg", context.getCacheDir());
            FileOutputStream outputStream = new FileOutputStream(tempFile);

            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }

            inputStream.close();
            outputStream.close();
            return tempFile;
        } catch (IOException e) {
            Log.e(TAG, "Error creating file from URI", e);
            return null;
        }
    }

    public static boolean validateEventOrganizerInput(String email, String password, String confirmPassword,
                                                    String address, String phone, String firstName, String lastName) {
        if (email == null || email.trim().isEmpty()) return false;
        if (password == null || password.length() < 6) return false;
        if (!password.equals(confirmPassword)) return false;
        if (address == null || address.trim().isEmpty()) return false;
        if (phone == null || phone.trim().isEmpty()) return false;
        if (firstName == null || firstName.trim().isEmpty()) return false;
        if (lastName == null || lastName.trim().isEmpty()) return false;
        return true;
    }

    public static boolean validateBusinessOwnerInput(String email, String password, String confirmPassword,
                                                   String address, String phone, String name, String description) {
        if (email == null || email.trim().isEmpty()) return false;
        if (password == null || password.length() < 6) return false;
        if (!password.equals(confirmPassword)) return false;
        if (address == null || address.trim().isEmpty()) return false;
        if (phone == null || phone.trim().isEmpty()) return false;
        if (name == null || name.trim().isEmpty()) return false;
        if (description == null || description.trim().isEmpty()) return false;
        return true;
    }
}
