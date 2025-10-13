package com.example.eventify.models.solutions.reservations;

import com.example.eventify.models.enums.Status;
import com.example.eventify.models.events.Event;
import com.example.eventify.models.solutions.Service;
import com.example.eventify.models.users.User;

import java.util.Date;
import java.util.UUID;

public class ServiceReservation {
    private UUID id;
    private Service service;
    private User provider;
    private User customer;
    private Event event;
    private Date startAt;
    private Date endAt;
    private String note;
    private Status status;

    // getters/setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public Service getService() { return service; }
    public void setService(Service service) { this.service = service; }
    public User getProvider() { return provider; }
    public void setProvider(User provider) { this.provider = provider; }
    public User getCustomer() { return customer; }
    public void setCustomer(User customer) { this.customer = customer; }
    public Event getEvent() { return event; }
    public void setEvent(Event event) { this.event = event; }
    public Date getStartAt() { return startAt; }
    public void setStartAt(Date startAt) { this.startAt = startAt; }
    public Date getEndAt() { return endAt; }
    public void setEndAt(Date endAt) { this.endAt = endAt; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
}
