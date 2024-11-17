package com.example.eventify.Model;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;

import com.example.eventify.Enums.Status;

import java.util.ArrayList;

public class Solution implements Parcelable {
    private String id;
    private Status status;
    private String name;
    private String description;
    private double price;
    private double discount;
    private ArrayList<String> images;
    private boolean visibility;
    private boolean availability;
    private SolutionCategory solutionCategory;

    public Solution(String id, Status status, String name, String description, double price, double discount,
                    ArrayList<String> images, boolean visibility, boolean availability, SolutionCategory solutionCategory) {
        this.id = id;
        this.status = status;
        this.name = name;
        this.description = description;
        this.price = price;
        this.discount = discount;
        this.images = images;
        this.visibility = visibility;
        this.availability = availability;
        this.solutionCategory = solutionCategory;
    }

    public Solution() {}

    protected Solution(Parcel in) {
        id = in.readString();
        status = Status.valueOf(in.readString());
        name = in.readString();
        description = in.readString();
        price = in.readDouble();
        discount = in.readDouble();
        images = in.createStringArrayList();
        visibility = in.readByte() != 0;
        availability = in.readByte() != 0;
        solutionCategory = in.readParcelable(SolutionCategory.class.getClassLoader());
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public double getDiscount() {
        return discount;
    }

    public void setDiscount(double discount) {
        this.discount = discount;
    }

    public ArrayList<String> getImages() {
        return images;
    }

    public void setImages(ArrayList<String> images) {
        this.images = images;
    }

    public boolean isVisibility() {
        return visibility;
    }

    public void setVisibility(boolean visibility) {
        this.visibility = visibility;
    }

    public boolean isAvailability() {
        return availability;
    }

    public void setAvailability(boolean availability) {
        this.availability = availability;
    }

    public SolutionCategory getSolutionCategory() {
        return solutionCategory;
    }

    public void setSolutionCategory(SolutionCategory solutionCategory) {
        this.solutionCategory = solutionCategory;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(status.name());
        dest.writeString(name);
        dest.writeString(description);
        dest.writeDouble(price);
        dest.writeDouble(discount);
        dest.writeStringList(images);
        dest.writeByte((byte) (visibility ? 1 : 0));
        dest.writeByte((byte) (availability ? 1 : 0));
        dest.writeParcelable(solutionCategory, flags);
    }

    public static final Creator<Solution> CREATOR = new Creator<Solution>() {
        @Override
        public Solution createFromParcel(Parcel in) {
            return new Solution(in);
        }

        @Override
        public Solution[] newArray(int size) {
            return new Solution[size];
        }
    };
}

