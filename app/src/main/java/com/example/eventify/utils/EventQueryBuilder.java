package com.example.eventify.utils;

import com.example.eventify.models.filters.EventFilterOptions;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public final class EventQueryBuilder {
    private static final SimpleDateFormat ISO_DATE = new SimpleDateFormat("yyyy-MM-dd");
    private EventQueryBuilder() {}

    public static Map<String, String> fromFilters(EventFilterOptions f) {
        Map<String, String> q = new HashMap<>();
        if (f == null) return q;

        if (f.search != null && !f.search.isEmpty()) q.put("search", f.search);
        if (f.startDate != null) q.put("startDate", toIsoDate(addDays(f.startDate, 1)));
        if (f.endDate != null)   q.put("endDate",   toIsoDate(addDays(f.endDate, 1)));
        if (f.maxAttendees != null && f.maxAttendees > 0) q.put("maxAttendees", String.valueOf(f.maxAttendees));
        if (f.attendance != null && f.attendance > 0)     q.put("attendance",   String.valueOf(f.attendance));
        if (f.eventTypes != null && !f.eventTypes.isEmpty()) q.put("eventTypes", String.join(",", f.eventTypes));
        if (f.locations != null && !f.locations.isEmpty())   q.put("locations",  String.join(",", f.locations));
        if (f.maxPrice != null && f.maxPrice > 0) q.put("maxPrice", String.valueOf(f.maxPrice));

        return q;
    }

    private static String toIsoDate(Date d) { return ISO_DATE.format(d); }
    private static Date addDays(Date date, int days) { return new Date(date.getTime() + days * 24L*60*60*1000); }
}
