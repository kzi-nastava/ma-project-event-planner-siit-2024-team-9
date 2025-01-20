package com.example.eventify.models.events;

import com.example.eventify.models.solutions.Solution;
import com.example.eventify.models.solutions.SolutionCategory;

import java.util.Set;

public class BudgetItem {
    private String id;
    private SolutionCategory category;
    private double plannedValue;
    private boolean isDeleted;
    private Set<Solution> selectedSolutions;

    public BudgetItem() {
    }

    public BudgetItem(String id, SolutionCategory category, double plannedValue, boolean isDeleted, Set<Solution> selectedSolutions) {
        this.id = id;
        this.category = category;
        this.plannedValue = plannedValue;
        this.isDeleted = isDeleted;
        this.selectedSolutions = selectedSolutions;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public SolutionCategory getCategory() {
        return category;
    }

    public void setCategory(SolutionCategory category) {
        this.category = category;
    }

    public double getPlannedValue() {
        return plannedValue;
    }

    public void setPlannedValue(double plannedValue) {
        this.plannedValue = plannedValue;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean isDeleted) {
        this.isDeleted = isDeleted;
    }

    public Set<Solution> getSelectedSolutions() {
        return selectedSolutions;
    }

    public void setSelectedSolutions(Set<Solution> selectedSolutions) {
        this.selectedSolutions = selectedSolutions;
    }
}

