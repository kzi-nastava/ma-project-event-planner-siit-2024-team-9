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
        File file = new File(context.getCacheDir(), "temp_" + System.currentTimeMillis());
        try (InputStream inputStream = context.getContentResolver().openInputStream(uri);
             FileOutputStream outputStream = new FileOutputStream(file)) {

            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
        }
        return file;
    }

    private static MultipartBody.Part prepareFilePart(String partName, Uri fileUri, Context context) throws Exception {
        File file = getFileFromUri(context, fileUri);
        RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), file);
        return MultipartBody.Part.createFormData(partName, file.getName(), requestFile);
    }

    public static List<MultipartBody.Part> prepareMultipleFiles(List<Uri> fileUris, Context context) throws Exception {
        List<MultipartBody.Part> parts = new ArrayList<>();
        for (Uri uri : fileUris) {
            parts.add(prepareFilePart("image", uri, context));
        }
        return parts;
    }

    public static RequestBody createPartFromObject(Object object) {
        Gson gson = new Gson();
        String json = gson.toJson(object);
        return RequestBody.create(MediaType.parse("application/json"), json);
    }



}
