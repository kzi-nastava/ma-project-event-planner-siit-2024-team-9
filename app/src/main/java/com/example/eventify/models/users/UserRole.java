package com.example.eventify.models.users;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.util.UUID;

public class UserRole implements Parcelable {
    @SerializedName("id")
    private UUID id;
    
    @SerializedName("name")
    private String name;

    public UserRole() {}

    public UserRole(UUID id, String name) {
        this.id = id;
        this.name = name;
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    // Parcelable implementation
    protected UserRole(Parcel in) {
        id = (UUID) in.readSerializable();
        name = in.readString();
    }

    public static final Creator<UserRole> CREATOR = new Creator<UserRole>() {
        @Override
        public UserRole createFromParcel(Parcel in) {
            return new UserRole(in);
        }

        @Override
        public UserRole[] newArray(int size) {
            return new UserRole[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeSerializable(id);
        dest.writeString(name);
    }
}
