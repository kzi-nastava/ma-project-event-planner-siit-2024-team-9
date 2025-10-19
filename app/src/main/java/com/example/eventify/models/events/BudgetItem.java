package com.example.eventify.models.events;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.example.eventify.models.solutions.Solution;
import com.example.eventify.models.solutions.SolutionCategory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class BudgetItem implements Parcelable {
    private String id;
    private SolutionCategory category;
    private double plannedValue;
    private Set<Solution> selectedSolutions;

    public BudgetItem() {
    }

    public BudgetItem(String id, SolutionCategory category, double plannedValue, Set<Solution> selectedSolutions) {
        this.id = id;
        this.category = category;
        this.plannedValue = plannedValue;
        this.selectedSolutions = selectedSolutions;
    }

    protected BudgetItem(Parcel in) {
        id = in.readString();
        category = (SolutionCategory) in.readSerializable();
        plannedValue = in.readDouble();
        selectedSolutions = new HashSet<>();
        in.readList(new ArrayList(selectedSolutions), Solution.class.getClassLoader());
    }

    public static final Creator<BudgetItem> CREATOR = new Creator<BudgetItem>() {
        @Override
        public BudgetItem createFromParcel(Parcel in) {
            return new BudgetItem(in);
        }

        @Override
        public BudgetItem[] newArray(int size) {
            return new BudgetItem[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeSerializable((Serializable) category);
        dest.writeDouble(plannedValue);
        dest.writeList(new ArrayList<>(selectedSolutions));
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

    public Set<Solution> getSelectedSolutions() {
        return selectedSolutions;
    }

    public void setSelectedSolutions(Set<Solution> selectedSolutions) {
        this.selectedSolutions = selectedSolutions;
    }
}