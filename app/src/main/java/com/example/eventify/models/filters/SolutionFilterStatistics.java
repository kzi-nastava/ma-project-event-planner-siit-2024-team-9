package com.example.eventify.models.filters;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class SolutionFilterStatistics implements Serializable {
    public int minPrice = 0;
    public int maxPrice = 2000;
    public int minDiscount = 0;
    public int maxDiscount = 20;
    public int minDuration = 0;
    public int maxDuration = 12;
    public int minEngagement = 0;
    public int maxEngagement = 500;

    public List<String> solutionCategories = new ArrayList<>();
    public List<String> eventTypes = new ArrayList<>();
}

