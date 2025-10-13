package com.example.eventify.models.solutions.reservations;

import java.util.UUID;

public class CreateServiceReservationRequest {
    private UUID serviceId;
    private UUID providerId;
    private UUID customerId;
    private UUID eventId;
    private String startAt;
    private String endAt;
    private String note;

    public CreateServiceReservationRequest() {}

    public CreateServiceReservationRequest(UUID serviceId, UUID providerId, UUID customerId,
                                           UUID eventId, String startAt, String endAt, String note) {
        this.serviceId = serviceId;
        this.providerId = providerId;
        this.customerId = customerId;
        this.eventId = eventId;
        this.startAt = startAt;
        this.endAt = endAt;
        this.note = note;
    }

    public UUID getServiceId() { return serviceId; }
    public void setServiceId(UUID serviceId) { this.serviceId = serviceId; }
    public UUID getProviderId() { return providerId; }
    public void setProviderId(UUID providerId) { this.providerId = providerId; }
    public UUID getCustomerId() { return customerId; }
    public void setCustomerId(UUID customerId) { this.customerId = customerId; }
    public UUID getEventId() { return eventId; }
    public void setEventId(UUID eventId) { this.eventId = eventId; }
    public String getStartAt() { return startAt; }
    public void setStartAt(String startAt) { this.startAt = startAt; }
    public String getEndAt() { return endAt; }
    public void setEndAt(String endAt) { this.endAt = endAt; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}
