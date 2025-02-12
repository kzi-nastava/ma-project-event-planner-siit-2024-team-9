package com.example.eventify.models.events;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.util.Set;

public class Budget implements Parcelable {
    private String id;
    private Set<BudgetItem> items;
    private double plannedValue;
    private double actualValue;

    public Budget() {
    }

    public Budget(String id, Set<BudgetItem> items, double plannedValue, double actualValue, boolean isDeleted) {
        this.id = id;
        this.items = items;
        this.plannedValue = plannedValue;
        this.actualValue = actualValue;
    }

    protected Budget(Parcel in) {
        id = in.readString();
        plannedValue = in.readDouble();
        actualValue = in.readDouble();
    }

    public static final Creator<Budget> CREATOR = new Creator<Budget>() {
        @Override
        public Budget createFromParcel(Parcel in) {
            return new Budget(in);
        }

        @Override
        public Budget[] newArray(int size) {
            return new Budget[size];
        }
    };

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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeDouble(plannedValue);
        dest.writeDouble(actualValue);
    }
}
