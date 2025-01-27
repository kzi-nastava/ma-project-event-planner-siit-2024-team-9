package com.example.eventify.models.users;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.example.eventify.models.enums.UserRole;

import java.util.UUID;

public class Role implements Parcelable {
    private String id;
    private UserRole name;

    public Role(UserRole name) {
        this.name = name;
    }

    public Role(String role) {
        this.name = UserRole.valueOf(role);
    }

    public Role() {
    }

    public String getAuthority() {
        return name.toString();
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
        return name.toString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    protected Role(Parcel in) {
        id = in.readString();
        name = UserRole.valueOf(in.readString());
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int i) {
        parcel.writeString(id);
        parcel.writeString(name.toString());
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
