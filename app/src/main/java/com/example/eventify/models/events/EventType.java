package com.example.eventify.models.events;

import android.os.Parcel;
import android.os.Parcelable;

import com.example.eventify.models.solutions.SolutionCategory;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class EventType implements Parcelable {
    @SerializedName("id")
    private String id;
    
    @SerializedName("name")
    private String name;
    
    @SerializedName("description")
    private String description;
    
    @SerializedName("isActive")
    private boolean isActive;
    
    @SerializedName("suggestedCategories")
    private Set<SolutionCategory> suggestedCategories;

    public EventType() {
        this.suggestedCategories = new HashSet<>();
    }

    public EventType(String id, String name, String description, boolean isActive) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.isActive = isActive;
        this.suggestedCategories = new HashSet<>();
    }

    public EventType(String id, String name, String description, boolean isActive, Set<SolutionCategory> suggestedCategories) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.isActive = isActive;
        this.suggestedCategories = suggestedCategories != null ? suggestedCategories : new HashSet<>();
    }
    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public Set<SolutionCategory> getSuggestedCategories() {
        return suggestedCategories;
    }

    public void setSuggestedCategories(Set<SolutionCategory> suggestedCategories) {
        this.suggestedCategories = suggestedCategories != null ? suggestedCategories : new HashSet<>();
    }

    // Parcelable implementation
    protected EventType(Parcel in) {
        id = in.readString();
        name = in.readString();
        description = in.readString();
        isActive = in.readByte() != 0;
        List<SolutionCategory> categoryList = in.createTypedArrayList(SolutionCategory.CREATOR);
        suggestedCategories = new HashSet<>(categoryList);
    }

    public static final Creator<EventType> CREATOR = new Creator<EventType>() {
        @Override
        public EventType createFromParcel(Parcel in) {
            return new EventType(in);
        }

        @Override
        public EventType[] newArray(int size) {
            return new EventType[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(description);
        dest.writeByte((byte) (isActive ? 1 : 0));
        dest.writeTypedList(new ArrayList<>(suggestedCategories));
    }
}