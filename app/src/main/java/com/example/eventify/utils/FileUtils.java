package com.example.eventify.utils;

import android.content.Context;
import android.net.Uri;

import com.google.gson.Gson;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class FileUtils {

    private static File getFileFromUri(Context context, Uri uri) throws Exception {
        if (context == null) {
            throw new Exception("Context is null");
        }
        if (uri == null) {
            throw new Exception("URI is null");
        }
        
        // Create a unique filename to avoid conflicts
        String fileName = "temp_" + System.currentTimeMillis() + "_" + uri.hashCode() + ".jpg";
        File file = new File(context.getCacheDir(), fileName);
        
        try (InputStream inputStream = context.getContentResolver().openInputStream(uri)) {
            if (inputStream == null) {
                throw new Exception("Could not open input stream for URI: " + uri.toString());
            }
            
            try (FileOutputStream outputStream = new FileOutputStream(file)) {
                byte[] buffer = new byte[8192]; // Increased buffer size for better performance
                int length;
                while ((length = inputStream.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, length);
                }
                outputStream.flush();
            }
        } catch (Exception e) {
            // Clean up the file if there was an error
            if (file.exists()) {
                file.delete();
            }
            throw e;
        }
        return file;
    }

    private static MultipartBody.Part prepareFilePart(String partName, Uri fileUri, Context context) throws Exception {
        File file = getFileFromUri(context, fileUri);
        RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), file);
        return MultipartBody.Part.createFormData(partName, file.getName(), requestFile);
    }

    public static List<MultipartBody.Part> prepareMultipleFiles(List<Uri> fileUris, Context context) throws Exception {
        if (fileUris == null || fileUris.isEmpty()) {
            throw new Exception("No file URIs provided");
        }
        if (context == null) {
            throw new Exception("Context is null");
        }
        
        List<MultipartBody.Part> parts = new ArrayList<>();
        for (int i = 0; i < fileUris.size(); i++) {
            Uri uri = fileUris.get(i);
            if (uri != null) {
                try {
                    parts.add(prepareFilePart("image", uri, context));
                } catch (Exception e) {
                    throw new Exception("Error processing image " + (i + 1) + ": " + e.getMessage());
                }
            }
        }
        return parts;
    }

    public static RequestBody createPartFromObject(Object object) {
        Gson gson = new Gson();
        String json = gson.toJson(object);
        return RequestBody.create(MediaType.parse("application/json"), json);
    }



}
