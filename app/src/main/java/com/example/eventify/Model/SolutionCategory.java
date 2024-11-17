package com.example.eventify.Model;

import android.os.Parcel;
import android.os.Parcelable;

import com.example.eventify.Enums.Status;

public class SolutionCategory implements Parcelable {
    private String name;
    private String description;
    private Status status;

    public SolutionCategory(String name, String description, Status status) {
        this.name = name;
        this.description = description;
        this.status = status;
    }

    public SolutionCategory() {}

    protected SolutionCategory(Parcel in) {
        name = in.readString();
        description = in.readString();
        status = Status.valueOf(in.readString());
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

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(description);
        dest.writeString(status.name());
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
