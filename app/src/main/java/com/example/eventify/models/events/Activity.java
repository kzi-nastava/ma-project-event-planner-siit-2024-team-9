package com.example.eventify.models.events;

import android.os.Parcel;
import android.os.Parcelable;

import com.example.eventify.models.others.Location;

public class Activity implements Parcelable {
    private String id;
    private String name;
    private String description;
    private String startDate;
    private String startTime;
    private String endTime;
    private Location location;

    public Activity() {}

    public Activity(String id, String name, String description, String startDate, String startTime, String endTime, Location location) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.startDate = startDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.location = location;
    }

    protected Activity(Parcel in) {
        id = in.readString();
        name = in.readString();
        description = in.readString();
        startDate = in.readString();
        startTime = in.readString();
        endTime = in.readString();
        location = in.readParcelable(Location.class.getClassLoader());
    }

    public static final Creator<Activity> CREATOR = new Creator<Activity>() {
        @Override
        public Activity createFromParcel(Parcel in) {
            return new Activity(in);
        }

        @Override
        public Activity[] newArray(int size) {
            return new Activity[size];
        }
    };

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

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(description);
        dest.writeString(startDate);
        dest.writeString(startTime);
        dest.writeString(endTime);
        dest.writeParcelable(location, flags);
    }
}

