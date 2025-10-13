package com.example.eventify.models.filters;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class SolutionFilterOptions implements Serializable {
    public String search = "";
    public Boolean isServiceSelected = null;
    public List<String> solutionCategories = new ArrayList<>();
    public List<String> eventTypes = new ArrayList<>();
    public Integer minPrice = null;
    public Integer maxPrice = null;
    public Integer minDiscount = null;
    public Integer maxDiscount = null;
    public Boolean visibility = null;
    public Boolean availability = null;

    // service-specific
    public Integer minDuration = null;
    public Integer maxDuration = null;
    public Integer minEngagement = null;
    public Integer maxEngagement = null;
    public Integer reservationDeadline = null;
    public Integer cancellationDeadline = null;
    public List<String> reservationMethod = new ArrayList<>();
}
