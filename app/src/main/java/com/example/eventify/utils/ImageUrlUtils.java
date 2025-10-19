package com.example.eventify.utils;

public class ImageUrlUtils {
    
    // Base URL for images (without /api/ since images are served directly)
    private static final String IMAGE_BASE_URL = "http://192.168.0.27:8080";
    
    /**
     * Construct the full image URL from a filename for products
     * @param filename The filename stored in the database
     * @return The complete URL to access the image
     */
    public static String getImageUrl(String filename) {
        return getImageUrl(filename, "product");
    }
    
    /**
     * Construct the full image URL from a filename for a specific type
     * @param filename The filename stored in the database
     * @param type The type of image (product, service, etc.)
     * @return The complete URL to access the image, or null if it's a web URL
     */
    public static String getImageUrl(String filename, String type) {
        if (filename == null || filename.trim().isEmpty()) {
            return null;
        }
        
        // Ignore web URLs - only handle local file paths
        if (filename.startsWith("http://") || filename.startsWith("https://")) {
            return null;
        }
        
        // Extract just the filename from any path
        String fileName = filename;
        if (filename.contains("\\")) {
            fileName = filename.substring(filename.lastIndexOf("\\") + 1);
        } else if (filename.contains("/")) {
            fileName = filename.substring(filename.lastIndexOf("/") + 1);
        }
        
        // Construct the URL for the specified type
        return IMAGE_BASE_URL + "/images/" + type + "/" + fileName;
    }
    
    /**
     * Get the base URL for images
     * @return The base URL for images
     */
    public static String getImageBaseUrl() {
        return IMAGE_BASE_URL;
    }
}
