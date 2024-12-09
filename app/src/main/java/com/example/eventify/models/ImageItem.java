package com.example.eventify.models;

public class ImageItem {
    private final int imageResId; // Resource ID for the image

    public ImageItem(int imageResId) {
        this.imageResId = imageResId;
    }

    public int getImageResId() {
        return imageResId;
    }

}