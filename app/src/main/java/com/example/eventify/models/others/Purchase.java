package com.example.eventify.models.others;

import com.example.eventify.models.events.Event;
import com.example.eventify.models.solutions.Solution;

public class Purchase {

    public Purchase() {
    }

    public Event[] getEvents() {
        return events;
    }

    public void setEvents(Event[] events) {
        this.events = events;
    }

    private Event[] events;

    public Solution getSolution() {
        return solution;
    }

    public void setSolution(Solution solution) {
        this.solution = solution;
    }

    private Solution solution;


    public Purchase(Event[] events, Solution solution) {
        this.events = events;
        this.solution = solution;
    }
}
