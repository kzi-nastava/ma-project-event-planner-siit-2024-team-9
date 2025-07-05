package com.example.eventify.models.users;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.example.eventify.models.enums.UserRole;
import com.google.gson.annotations.SerializedName;

import java.util.UUID;

public class Role implements Parcelable {
    // Include id field to match backend RoleDTO
    @SerializedName("id")
    private String id;
    
    // Include name field to match backend RoleDTO
    @SerializedName("name")
    private UserRole name;

    public Role(UserRole name) {
        this.name = name;
        // Generate a random UUID for id to match backend expectations
        this.id = UUID.randomUUID().toString();
    }

    public Role(String role) {
        this.name = UserRole.valueOf(role);
        this.id = UUID.randomUUID().toString();
    }

    public Role() {
        this.id = UUID.randomUUID().toString();
    }

    public String getAuthority() {
        return name != null ? name.toString() : null;
    }

    public void setName(UserRole name) {
        this.name = name;
    }

    public UserRole getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return name != null ? name.toString() : "null";
    }

    @Override
    public int describeContents() {
        return 0;
    }

    protected Role(Parcel in) {
        id = in.readString();
        String nameString = in.readString();
        if (nameString != null) {
            try {
                this.name = UserRole.valueOf(nameString);
            } catch (IllegalArgumentException e) {
                this.name = null;
            }
        }
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int i) {
        parcel.writeString(id);
        parcel.writeString(name != null ? name.toString() : null);
    }

    public static final Creator<Role> CREATOR = new Creator<Role>() {
        @Override
        public Role createFromParcel(Parcel in) {
            return new Role(in);
        }

        @Override
        public Role[] newArray(int size) {
            return new Role[size];
        }
    };
}
