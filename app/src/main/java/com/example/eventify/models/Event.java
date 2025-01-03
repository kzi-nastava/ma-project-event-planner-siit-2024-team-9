package com.example.eventify.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.example.eventify.models.enums.PrivacyType;

import java.util.Date;
import java.util.Set;

public class Event implements Parcelable {
    private String id;
    private String name;
    private String description;
    private String image;
    private int maxAttendees;
    private PrivacyType privacyType;
    private Date eventStart;
    private Date eventEnd;
    private int attendance;
    private EventType eventType;
    private Location location;
    private Double price;
    private Set<Invitation> invitations;

    public Event(String id, String name, String description, String image, int maxAttendees, PrivacyType privacyType, Date eventStart, Date eventEnd, int attendance, EventType eventType, Location location, Double price, Set<Invitation> invitations) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.image = image;
        this.maxAttendees = maxAttendees;
        this.privacyType = privacyType;
        this.eventStart = eventStart;
        this.eventEnd = eventEnd;
        this.attendance = attendance;
        this.eventType = eventType;
        this.location = location;
        this.price = price;
        this.invitations = invitations;
    }

    public Event() {}

    protected Event(Parcel in) {
        id = in.readString();
        name = in.readString();
        description = in.readString();
        image = in.readString();
        maxAttendees = in.readInt();
        privacyType = PrivacyType.valueOf(in.readString()); // Enum se serijalizuje kao String
        eventStart = new Date(in.readLong());
        eventEnd = new Date(in.readLong());
        attendance = in.readInt();
        eventType = in.readParcelable(EventType.class.getClassLoader());
        location = in.readParcelable(Location.class.getClassLoader());
        price = in.readDouble();
        invitations = (Set<Invitation>) in.readSerializable();
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

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
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

    public EventType getEventType() {
        return eventType;
    }

    public void setEventType(EventType eventType) {
        this.eventType = eventType;
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

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Set<Invitation> getInvitations() {
        return invitations;
    }

    public void setInvitations(Set<Invitation> invitations) {
        this.invitations = invitations;
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
