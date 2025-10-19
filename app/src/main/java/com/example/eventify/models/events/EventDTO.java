package com.example.eventify.models.events;

import com.example.eventify.models.others.Location;
import com.example.eventify.models.enums.PrivacyType;
import com.example.eventify.models.users.EventOrganizerDTO;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.Date;
import java.util.List;
import java.util.Set;

public class EventDTO {
    @SerializedName("id")
    @Expose
    private String id;
    
    @SerializedName("name")
    @Expose
    private String name;
    
    @SerializedName("organizer")
    @Expose
    private EventOrganizerDTO organizer;
    
    @SerializedName("description")
    @Expose
    private String description;
    
    @SerializedName("image")
    @Expose
    private String image;
    
    @SerializedName("maxAttendees")
    @Expose
    private int maxAttendees;
    
    @SerializedName("privacyType")
    @Expose
    private PrivacyType privacyType;
    
    @SerializedName("eventStart")
    @Expose
    private Date eventStart;
    
    @SerializedName("eventEnd")
    @Expose
    private Date eventEnd;
    
    @SerializedName("attendance")
    @Expose
    private int attendance;
    
    @SerializedName("location")
    @Expose
    private Location location;
    
    @SerializedName("price")
    @Expose
    private Double price;
    
    @SerializedName("eventType")
    @Expose
    private EventType eventType;
    
    @SerializedName("activities")
    @Expose
    private List<Activity> activities;

    public EventDTO() {
    }

    public EventDTO(String id, String name, EventOrganizerDTO organizer, String description, String image, 
                   int maxAttendees, PrivacyType privacyType, Date eventStart, Date eventEnd, 
                   int attendance, Location location, Double price, EventType eventType, 
                   List<Activity> activities) {
        this.id = id;
        this.name = name;
        this.organizer = organizer;
        this.description = description;
        this.image = image;
        this.maxAttendees = maxAttendees;
        this.privacyType = privacyType;
        this.eventStart = eventStart;
        this.eventEnd = eventEnd;
        this.attendance = attendance;
        this.location = location;
        this.price = price;
        this.eventType = eventType;
        this.activities = activities;
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

    public EventOrganizerDTO getOrganizer() {
        return organizer;
    }

    public void setOrganizer(EventOrganizerDTO organizer) {
        this.organizer = organizer;
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

    public EventType getEventType() {
        return eventType;
    }

    public void setEventType(EventType eventType) {
        this.eventType = eventType;
    }

    public List<Activity> getActivities() {
        return activities;
    }

    public void setActivities(List<Activity> activities) {
        this.activities = activities;
    }
}
