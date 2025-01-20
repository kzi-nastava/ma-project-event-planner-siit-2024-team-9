package com.example.eventify.models.events;

import java.util.Set;

public class Budget {
    private String id;
    private Set<BudgetItem> items;
    private double plannedValue;
    private double actualValue;
    private boolean isDeleted;

    public Budget() {
    }

    public Budget(String id, Set<BudgetItem> items, double plannedValue, double actualValue, boolean isDeleted) {
        this.id = id;
        this.items = items;
        this.plannedValue = plannedValue;
        this.actualValue = actualValue;
        this.isDeleted = isDeleted;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Set<BudgetItem> getItems() {
        return items;
    }

    public void setItems(Set<BudgetItem> items) {
        this.items = items;
    }

    public double getPlannedValue() {
        return plannedValue;
    }

    public void setPlannedValue(double plannedValue) {
        this.plannedValue = plannedValue;
    }

    public double getActualValue() {
        return actualValue;
    }

    public void setActualValue(double actualValue) {
        this.actualValue = actualValue;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }
}
