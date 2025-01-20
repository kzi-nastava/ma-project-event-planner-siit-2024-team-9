package com.example.eventify.services;

public class ServiceFactory<T> {

    static String BASE_URL = "http://192.168.0.26:8080/api/";

    private static Object instance;

    private ServiceFactory() {}

    public static <T> T getInstance(Class<T> clazz) {
        if (instance == null) {
            synchronized (ServiceFactory.class) {
                if (instance == null) {
                    try {
                        instance = clazz.getDeclaredConstructor().newInstance();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return clazz.cast(instance);
    }

}