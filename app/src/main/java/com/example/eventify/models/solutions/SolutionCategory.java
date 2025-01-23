package com.example.eventify.models.solutions;

import android.os.Parcel;
import android.os.Parcelable;

import com.example.eventify.models.enums.Status;

public class SolutionCategory implements Parcelable {
    private String id;
    private String categoryName;
    private String description;

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    private boolean isActive;

    // Default constructor
    public SolutionCategory() {}

    // Parameterized constructor
    public SolutionCategory(String id, String categoryName, String description, boolean isActive) {
        this.id = id;
        this.categoryName = categoryName;
        this.description = description;
        this.isActive = isActive;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return categoryName;
    }

    public void setName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }



    // Parcelable methods
    protected SolutionCategory(Parcel in) {
        id = in.readString();
        categoryName = in.readString();
        description = in.readString();
        isActive = Boolean.parseBoolean(in.readString()); // Assuming Status is an enum
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(categoryName);
        dest.writeString(description);
        dest.writeString(String.valueOf(isActive));
    }

    public static final Creator<SolutionCategory> CREATOR = new Creator<SolutionCategory>() {
        @Override
        public SolutionCategory createFromParcel(Parcel in) {
            return new SolutionCategory(in);
        }

        @Override
        public SolutionCategory[] newArray(int size) {
            return new SolutionCategory[size];
        }
    };
}
