package com.example.eventify.models.solutions;

import android.os.Parcel;
import android.os.Parcelable;

import com.example.eventify.models.enums.Status;
import com.example.eventify.models.events.EventType;

import java.util.Set;

public class Product extends Solution implements Parcelable {

    public Product(
            String name,
            SolutionCategory category,
            Set<EventType> eventTypes,
            Status status,
            String description,
            double price,
            double discount,
            Set<String> images,
            boolean visibility,
            boolean availability
    ) {
        super(name, category, eventTypes, status, description, price, discount, images, visibility, availability);
    }

    public Product() {}

    public Product(Parcel in) {
        super(in);
    }

    public static final Creator<Product> CREATOR = new Creator<Product>() {
        @Override
        public Product createFromParcel(Parcel in) {
            return new Product(in);
        }

        @Override
        public Product[] newArray(int size) {
            return new Product[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

}
