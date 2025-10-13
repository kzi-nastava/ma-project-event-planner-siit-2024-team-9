package com.example.eventify.models.filters;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class EventFilterStatistics implements Serializable {
    public int maxAttendees = 1000;
    public int maxAttendance = 1000;
    public Double maxPrice = null;
    public List<String> eventTypes = new ArrayList<>();
    public List<String> locations = new ArrayList<>();
}
