package com.example.eventify.models.filters;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class EventFilterOptions implements Serializable {
    public String search = "";
    public Date startDate = null;
    public Date endDate = null;
    public Integer maxAttendees = 0;
    public Integer attendance = 0;
    public List<String> eventTypes = new ArrayList<>();
    public List<String> locations = new ArrayList<>();
    public Double maxPrice = 0.0;
}
