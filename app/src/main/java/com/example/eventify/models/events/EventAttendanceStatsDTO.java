package com.example.eventify.models.events;

import androidx.annotation.Nullable;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * DTO mirroring backend EventAttendanceStatsDTO response.
 */
public class EventAttendanceStatsDTO {

    private String eventId;
    private String eventName;
    private int attendance;
    private int maxAttendees;
    private double attendancePercentage;
    private Map<Integer, Long> gradeDistribution;
    private long totalReviews;
    private double averageRating;
    private int mostCommonGrade;

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public int getAttendance() {
        return attendance;
    }

    public void setAttendance(int attendance) {
        this.attendance = attendance;
    }

    public int getMaxAttendees() {
        return maxAttendees;
    }

    public void setMaxAttendees(int maxAttendees) {
        this.maxAttendees = maxAttendees;
    }

    public double getAttendancePercentage() {
        return attendancePercentage;
    }

    public void setAttendancePercentage(double attendancePercentage) {
        this.attendancePercentage = attendancePercentage;
    }

    public Map<Integer, Long> getGradeDistribution() {
        if (gradeDistribution == null) return Collections.emptyMap();
        return new LinkedHashMap<>(gradeDistribution);
    }

    public void setGradeDistribution(@Nullable Map<Integer, Long> gradeDistribution) {
        this.gradeDistribution = gradeDistribution;
    }

    public long getTotalReviews() {
        return totalReviews;
    }

    public void setTotalReviews(long totalReviews) {
        this.totalReviews = totalReviews;
    }

    public double getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(double averageRating) {
        this.averageRating = averageRating;
    }

    public int getMostCommonGrade() {
        return mostCommonGrade;
    }

    public void setMostCommonGrade(int mostCommonGrade) {
        this.mostCommonGrade = mostCommonGrade;
    }
}
