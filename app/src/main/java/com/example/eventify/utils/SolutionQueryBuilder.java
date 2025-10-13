package com.example.eventify.utils;

import com.example.eventify.models.filters.SolutionFilterOptions;

import java.util.HashMap;
import java.util.Map;

public final class SolutionQueryBuilder {
    private SolutionQueryBuilder(){}

    public static Map<String, String> fromFilters(SolutionFilterOptions f) {
        Map<String, String> q = new HashMap<>();
        if (f == null) return q;

        if (f.search != null && !f.search.isEmpty()) q.put("search", f.search);
        if (f.isServiceSelected != null) q.put("isService", String.valueOf(f.isServiceSelected));

        if (f.solutionCategories != null && !f.solutionCategories.isEmpty())
            q.put("solutionCategories", String.join(",", f.solutionCategories));
        if (f.eventTypes != null && !f.eventTypes.isEmpty())
            q.put("eventTypes", String.join(",", f.eventTypes));

        if (f.minPrice != null)     q.put("minPrice", String.valueOf(f.minPrice));
        if (f.maxPrice != null)     q.put("maxPrice", String.valueOf(f.maxPrice));
        if (f.minDiscount != null)  q.put("minDiscount", String.valueOf(f.minDiscount));
        if (f.maxDiscount != null)  q.put("maxDiscount", String.valueOf(f.maxDiscount));
        if (f.visibility != null)   q.put("visibility", String.valueOf(f.visibility));
        if (f.availability != null) q.put("availability", String.valueOf(f.availability));

        if (f.minDuration != null)  q.put("minDuration", String.valueOf(f.minDuration));
        if (f.maxDuration != null)  q.put("maxDuration", String.valueOf(f.maxDuration));
        if (f.minEngagement != null) q.put("minEngagement", String.valueOf(f.minEngagement));
        if (f.maxEngagement != null) q.put("maxEngagement", String.valueOf(f.maxEngagement));

        if (f.reservationDeadline != null)   q.put("reservationDeadline", String.valueOf(f.reservationDeadline));
        if (f.cancellationDeadline != null)  q.put("cancellationDeadline", String.valueOf(f.cancellationDeadline));
        if (f.reservationMethod != null && !f.reservationMethod.isEmpty())
            q.put("reservationMethods", String.join(",", f.reservationMethod));

        return q;
    }
}
