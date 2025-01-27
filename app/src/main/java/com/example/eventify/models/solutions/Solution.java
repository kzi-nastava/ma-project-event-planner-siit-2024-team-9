package com.example.eventify.models.solutions;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.databinding.Bindable;
import androidx.databinding.Observable;

import com.example.eventify.BR;
import com.example.eventify.models.enums.SolutionType;
import com.example.eventify.models.enums.Status;
import com.example.eventify.models.events.EventType;
import com.example.eventify.models.users.BusinessOwner;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class Solution implements Parcelable, Observable {

    private UUID id;
    @Bindable
    private String name;
    @Bindable
    private SolutionCategory category;
    @Bindable
    private Set<EventType> type;

    private Status status;
    @Bindable
    private String description;
    @Bindable
    private double price;
    @Bindable
    private double discount;

    private ArrayList<String> images;
    @Bindable
    private boolean visibility;
    @Bindable
    private boolean availability;
    private BusinessOwner owner;

    private Set<Review> reviews;

    private SolutionType solutionType;

    private transient List<OnPropertyChangedCallback> propertyChangedCallbacks = new ArrayList<>();

    public Solution(
            String name,
            SolutionCategory category,
            Set<EventType> type,
            Status status,
            String description,
            double price,
            double discount,
            ArrayList<String> images,
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

    public Solution() {
    }

    protected Solution(Parcel in) {
        id = UUID.fromString(in.readString());
        name = in.readString();
        status = Status.valueOf(in.readString());
        description = in.readString();
        price = in.readDouble();
        discount = in.readDouble();
        visibility = in.readByte() != 0;
        availability = in.readByte() != 0;
        category = in.readParcelable(SolutionCategory.class.getClassLoader());
        type = new HashSet<>(in.createTypedArrayList(EventType.CREATOR));
        images = new ArrayList<>(in.createStringArrayList());
        owner = in.readParcelable(BusinessOwner.class.getClassLoader());
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        notifyPropertyChanged(BR.name);
    }

    public SolutionCategory getCategory() {
        return category;
    }

    public void setCategory(SolutionCategory category) {
        this.category = category;
        notifyPropertyChanged(BR.category);
    }

    public Set<EventType> getEventTypes() {
        return type;
    }

    public void setEventTypes(Set<EventType> eventTypes) {
        this.type = eventTypes;
        notifyPropertyChanged(BR.type);
    }

    public Set<Review> getReviews() {
        return this.reviews;
    }

    public void setReviews(Set<Review> reviews) {
        this.reviews = reviews;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
        notifyPropertyChanged(BR.description);
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
        notifyPropertyChanged(BR.price);
    }

    public double getDiscount() {
        return discount;
    }

    public void setDiscount(double discount) {
        this.discount = discount;
        notifyPropertyChanged(BR.discount);
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
        notifyPropertyChanged(BR.visibility);
    }

    public boolean isAvailability() {
        return availability;
    }

    public void setAvailability(boolean availability) {
        this.availability = availability;
        notifyPropertyChanged(BR.availability);
    }

    public BusinessOwner getOwner() {
        return owner;
    }

    public void setOwner(BusinessOwner owner) {
        this.owner = owner;
        notifyPropertyChanged(BR.owner);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(String.valueOf(id));
        dest.writeString(name);
        dest.writeString(status.name());
        dest.writeString(description);
        dest.writeDouble(price);
        dest.writeDouble(discount);
        dest.writeByte((byte) (visibility ? 1 : 0));
        dest.writeByte((byte) (availability ? 1 : 0));
        dest.writeParcelable(category, flags);
        dest.writeTypedList(new ArrayList<>(type));
        dest.writeStringList(new ArrayList<>(images));
        dest.writeParcelable(owner, flags);
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

    public boolean isService() {
        return SolutionType.SERVICE.equals(solutionType);
    }

    public boolean isProduct() {
        return SolutionType.PRODUCT.equals(solutionType);
    }
}
