package com.example.eventify.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.example.eventify.models.enums.PrivacyType;

import java.util.Date;

public class Event implements Parcelable {
    private String id;
    private String name;
    private String description;
    private int maxAttendees;
    private PrivacyType privacyType;
    private Date eventStart;
    private Date eventEnd;
    private int attendance;
    private Location location;

    public Event(String id, String name, String description, int maxAttendees, PrivacyType privacyType, Date eventStart, Date eventEnd, int attendance, Location location) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.maxAttendees = maxAttendees;
        this.privacyType = privacyType;
        this.eventStart = eventStart;
        this.eventEnd = eventEnd;
        this.attendance = attendance;
        this.location = location;
    }

    public Event() {}

    protected Event(Parcel in) {
        id = in.readString();
        name = in.readString();
        description = in.readString();
        maxAttendees = in.readInt();
        privacyType = PrivacyType.valueOf(in.readString()); // Enum se serijalizuje kao String
        eventStart = new Date(in.readLong());
        eventEnd = new Date(in.readLong());
        attendance = in.readInt();
        location = in.readParcelable(Location.class.getClassLoader());
    }

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

    public int getMaxAttendees() {
        return maxAttendees;
    }

    public void setMaxAttendees(int maxAttendees) {
        this.maxAttendees = maxAttendees;
    }

    public PrivacyType getPrivacyType() {
        return privacyType;
    }

    public void setPrivacyType(PrivacyType privacyType) {
        this.privacyType = privacyType;
    }

    public Date getEventStart() {
        return eventStart;
    }

    public void setEventStart(Date eventStart) {
        this.eventStart = eventStart;
    }

    public Date getEventEnd() {
        return eventEnd;
    }

    public void setEventEnd(Date eventEnd) {
        this.eventEnd = eventEnd;
    }

    public int getAttendance() {
        return attendance;
    }

    public void setAttendance(int attendance) {
        this.attendance = attendance;
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
        dest.writeInt(maxAttendees);
        dest.writeString(privacyType.name());
        dest.writeLong(eventStart.getTime());
        dest.writeLong(eventEnd.getTime());
        dest.writeInt(attendance);
        dest.writeParcelable(location, flags);
    }

    public static final Creator<Event> CREATOR = new Creator<Event>() {
        @Override
        public Event createFromParcel(Parcel in) {
            return new Event(in);
        }

        @Override
        public Event[] newArray(int size) {
            return new Event[size];
        }
    };
}
