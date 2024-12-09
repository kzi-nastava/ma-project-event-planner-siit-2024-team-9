package com.example.eventify.models;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.databinding.Bindable;
import androidx.databinding.Observable;

import com.example.eventify.BR;
import com.example.eventify.models.enums.Status;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class Solution implements Parcelable, Observable {
    @Bindable
    private int id;
    @Bindable
    private String name;
    @Bindable
    private SolutionCategory category;
    @Bindable
    private Set<EventType> type;
    @Bindable
    private Status status;
    @Bindable
    private String description;
    @Bindable
    private double price;
    @Bindable
    private double discount;
    @Bindable
    private Set<String> images;
    @Bindable
    private boolean visibility;
    @Bindable
    private boolean availability;

    private transient List<OnPropertyChangedCallback> propertyChangedCallbacks = new ArrayList<>();

    public Solution(
            String name,
            SolutionCategory category,
            Set<EventType> type,
            Status status,
            String description,
            double price,
            double discount,
            Set<String> images,
            boolean visibility,
            boolean availability
    ) {
        this.name = name;
        this.category = category;
        this.type = type;
        this.status = status;
        this.description = description;
        this.price = price;
        this.discount = discount;
        this.images = images;
        this.visibility = visibility;
        this.availability = availability;
    }

    public Solution() {}

    protected Solution(Parcel in) {
        id = in.readInt();
        status = Status.valueOf(in.readString());
        name = in.readString();
        description = in.readString();
        price = in.readDouble();
        discount = in.readDouble();
        images = new java.util.HashSet<>(in.createStringArrayList());
        visibility = in.readByte() != 0;
        availability = in.readByte() != 0;
        category = in.readParcelable(SolutionCategory.class.getClassLoader());
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
        notifyPropertyChanged(BR.id);
    }

    public Status getStatus() {
        return status;
    }

    public SolutionCategory getCategory() {
        return category;
    }

    public void setCategory(SolutionCategory category) {
        this.category = category;
        notifyPropertyChanged(BR.category);
    }

    public Set<EventType> getType() {
        return type;
    }

    public Set<String> getImages() {
        return images;
    }

    public void setImages(Set<String> images) {
        this.images = images;
        notifyPropertyChanged(BR.images);
    }

    public void setType(Set<EventType> type) {
        this.type = type;
        notifyPropertyChanged(BR.type);
    }

    public void setStatus(Status status) {
        this.status = status;
        notifyPropertyChanged(BR.status);
    }

    @Bindable
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        notifyPropertyChanged(BR.name);
    }

    @Bindable
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
        notifyPropertyChanged(BR.description);
    }

    @Bindable
    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
        notifyPropertyChanged(BR.price);
    }

    @Bindable
    public double getDiscount() {
        return discount;
    }

    public void setDiscount(double discount) {
        this.discount = discount;
        notifyPropertyChanged(BR.discount);
    }

    @Bindable
    public boolean isVisibility() {
        return visibility;
    }

    public void setVisibility(boolean visibility) {
        this.visibility = visibility;
        notifyPropertyChanged(BR.visibility);
    }

    @Bindable
    public boolean isAvailability() {
        return availability;
    }

    public void setAvailability(boolean availability) {
        this.availability = availability;
        notifyPropertyChanged(BR.availability);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(status.name());
        dest.writeString(name);
        dest.writeString(description);
        dest.writeDouble(price);
        dest.writeDouble(discount);
        dest.writeStringList(new ArrayList<>(images));
        dest.writeByte((byte) (visibility ? 1 : 0));
        dest.writeByte((byte) (availability ? 1 : 0));
        dest.writeParcelable(category, flags);
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

    @Override
    public void addOnPropertyChangedCallback(OnPropertyChangedCallback callback) {
        if (!propertyChangedCallbacks.contains(callback)) {
            propertyChangedCallbacks.add(callback);
        }
    }

    @Override
    public void removeOnPropertyChangedCallback(OnPropertyChangedCallback callback) {
        propertyChangedCallbacks.remove(callback);
    }

    protected void notifyPropertyChanged(int fieldId) {
        for (OnPropertyChangedCallback callback : propertyChangedCallbacks) {
            callback.onPropertyChanged(this, fieldId);
        }
    }
}
