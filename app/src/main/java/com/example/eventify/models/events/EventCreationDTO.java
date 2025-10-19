package com.example.eventify.models.events;

import com.example.eventify.models.others.Location;
import com.example.eventify.models.enums.PrivacyType;
import com.example.eventify.models.users.EventOrganizerDTO;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.Date;
import java.util.List;

public class EventCreationDTO {
    @SerializedName("name")
    @Expose
    private String name;
    
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
    private String eventStart;
    
    @SerializedName("eventEnd")
    @Expose
    private String eventEnd;
    
    @SerializedName("eventType")
    @Expose
    private EventType eventType;
    
    @SerializedName("location")
    @Expose
    private Location location;
    
    @SerializedName("price")
    @Expose
    private Double price;
    
    @SerializedName("organizer")
    @Expose
    private EventOrganizerDTO organizer;
    
    @SerializedName("activities")
    @Expose
    private List<Activity> activities;

    public EventCreationDTO() {
    }

    public EventCreationDTO(String name, String description, String image, int maxAttendees, 
                           PrivacyType privacyType, String eventStart, String eventEnd, 
                           EventType eventType, Location location, Double price, 
                           EventOrganizerDTO organizer, List<Activity> activities) {
        this.name = name;
        this.description = description;
        this.image = image;
        this.maxAttendees = maxAttendees;
        this.privacyType = privacyType;
        this.eventStart = eventStart;
        this.eventEnd = eventEnd;
        this.eventType = eventType;
        this.location = location;
        this.price = price;
        this.organizer = organizer;
        this.activities = activities;
    }

    // Getters and Setters
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

    public String getEventStart() {
        return eventStart;
    }

    public void setEventStart(String eventStart) {
        this.eventStart = eventStart;
    }

    public String getEventEnd() {
        return eventEnd;
    }

    public void setEventEnd(String eventEnd) {
        this.eventEnd = eventEnd;
    }

    public EventType getEventType() {
        return eventType;
    }

    public void setEventType(EventType eventType) {
        this.eventType = eventType;
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

    public EventOrganizerDTO getOrganizer() {
        return organizer;
    }

    public void setOrganizer(EventOrganizerDTO organizer) {
        this.organizer = organizer;
    }

    public List<Activity> getActivities() {
        return activities;
    }

    public void setActivities(List<Activity> activities) {
        this.activities = activities;
    }
}
