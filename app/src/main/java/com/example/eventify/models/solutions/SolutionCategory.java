package com.example.eventify.models.solutions;

import android.os.Parcel;
import android.os.Parcelable;

import com.example.eventify.models.enums.Status;
import com.google.gson.annotations.SerializedName;

public class SolutionCategory implements Parcelable {
    @SerializedName("id")
    private String id;
    
    @SerializedName("categoryName")
    private String categoryName;
    
    @SerializedName("categoryDescription")
    private String categoryDescription;
    
    @SerializedName("isActive")
    private boolean isActive;
    
    @SerializedName("isDeleted")
    private boolean isDeleted = false;

    // Default constructor
    public SolutionCategory() {}

    // Parameterized constructor
    public SolutionCategory(String id, String categoryName, String categoryDescription, boolean isActive) {
        this.id = id;
        this.categoryName = categoryName;
        this.categoryDescription = categoryDescription;
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
        return categoryDescription;
    }

    public void setDescription(String categoryDescription) {
        this.categoryDescription = categoryDescription;
    }
    
    public String getCategoryDescription() {
        return categoryDescription;
    }

    public void setCategoryDescription(String categoryDescription) {
        this.categoryDescription = categoryDescription;
    }
    
    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    // Parcelable methods
    protected SolutionCategory(Parcel in) {
        id = in.readString();
        categoryName = in.readString();
        categoryDescription = in.readString();
        isActive = in.readByte() != 0;
        isDeleted = in.readByte() != 0;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(categoryName);
        dest.writeString(categoryDescription);
        dest.writeByte((byte) (isActive ? 1 : 0));
        dest.writeByte((byte) (isDeleted ? 1 : 0));
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
