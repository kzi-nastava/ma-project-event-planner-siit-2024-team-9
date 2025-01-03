package com.example.eventify.models.events;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.UUID;

public class EventType implements Parcelable {

    private UUID id;
    private String name;
    private String description;
    private boolean isActive;

    // Constructor
    public EventType(String name, String description, boolean isActive) {
        this.name = name;
        this.description = description;
        this.isActive = isActive;
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    // Parcelable implementation
    protected EventType(Parcel in) {
        id = (UUID) in.readSerializable(); // UUID is serialized
        name = in.readString();
        description = in.readString();
        isActive = in.readByte() != 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeSerializable(id); // UUID is serialized
        dest.writeString(name);
        dest.writeString(description);
        dest.writeByte((byte) (isActive ? 1 : 0));
    }

    @Override
    public int describeContents() {
        return 0;
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
}
