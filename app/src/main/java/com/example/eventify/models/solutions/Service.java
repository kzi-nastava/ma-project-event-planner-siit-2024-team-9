package com.example.eventify.models.solutions;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.databinding.Bindable;
import androidx.databinding.Observable;

import com.example.eventify.BR;
import com.example.eventify.models.enums.ReservationMethod;
import com.example.eventify.models.enums.SolutionType;
import com.example.eventify.models.enums.Status;
import com.example.eventify.models.events.EventType;
import com.example.eventify.models.users.BusinessOwner;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class Service extends Solution implements Parcelable, Observable {
    @Bindable
    private String specifity;
    @Bindable
    private int duration;
    @Bindable
    private int minEngagement;
    @Bindable
    private int maxEngagement;
    @Bindable
    private int reservationDeadline;
    @Bindable
    private int cancellationDeadline;
    @Bindable
    private ReservationMethod reservationMethod;

    // Constructor
    public Service(
            String name,
            SolutionCategory serviceCategory,
            Set<EventType> eventType,
            Status status,
            String description,
            double price,
            double discount,
            ArrayList<String> images,
            boolean visibility,
            boolean availability,
            String specifity,
            int duration,
            int minEngagement,
            int maxEngagement,
            int reservationDeadline,
            int cancellationDeadline,
            ReservationMethod reservationMethod
    ) {
        super(name, serviceCategory, eventType, status, description, price, discount, images, visibility, availability);
        this.specifity = specifity;
        this.duration = duration;
        this.minEngagement = minEngagement;
        this.maxEngagement = maxEngagement;
        this.reservationDeadline = reservationDeadline;
        this.cancellationDeadline = cancellationDeadline;
        this.reservationMethod = reservationMethod;
    }

    public Service() {
            this.setId(null);
            this.setName("");
            this.setCategory(new SolutionCategory());
            this.setEventTypes(new HashSet<>());
            this.setStatus(Status.DENIED);
            this.setDescription("");
            this.setPrice(0.0);
            this.setDiscount(0.0);
            this.setImages(new ArrayList<>());
            this.setVisibility(true);
            this.setAvailability(true);
            this.setDeleted(false);
            this.setOwner(null);
            this.setReviews(new HashSet<>());
            this.setSpecifity("");
            this.setDuration(0);
            this.setMinEngagement(0);
            this.setMaxEngagement(0);
            this.setReservationDeadline(0);
            this.setCancellationDeadline(0);
            this.setReservationMethod(ReservationMethod.AUTOMATIC);
    }

    // Parcelable constructor
    protected Service(Parcel in) {
        super(in); // Call the parent class Parcelable constructor
        specifity = in.readString();
        duration = in.readInt();
        minEngagement = in.readInt();
        maxEngagement = in.readInt();
        reservationDeadline = in.readInt();
        cancellationDeadline = in.readInt();
        reservationMethod = ReservationMethod.valueOf(in.readString());
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        super.writeToParcel(dest, flags); // Write parent class fields
        dest.writeString(specifity);
        dest.writeInt(duration);
        dest.writeInt(minEngagement);
        dest.writeInt(maxEngagement);
        dest.writeInt(reservationDeadline);
        dest.writeInt(cancellationDeadline);
        dest.writeString(reservationMethod.name());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<Service> CREATOR = new Parcelable.Creator<Service>() {
        @Override
        public Service createFromParcel(Parcel in) {
            return new Service(in);
        }

        @Override
        public Service[] newArray(int size) {
            return new Service[size];
        }
    };

    // Getters and setters with Bindable annotations
    @Bindable
    public String getSpecifity() {
        return specifity;
    }

    public void setSpecifity(String specifity) {
        this.specifity = specifity;
        notifyPropertyChanged(BR.specifity); // Notify when the specifity changes
    }

    @Bindable
    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
        notifyPropertyChanged(BR.duration); // Notify when the duration changes
    }

    @Bindable
    public int getMinEngagement() {
        return minEngagement;
    }

    public void setMinEngagement(int minEngagement) {
        this.minEngagement = minEngagement;
        notifyPropertyChanged(BR.minEngagement); // Notify when minEngagement changes
    }

    @Bindable
    public int getMaxEngagement() {
        return maxEngagement;
    }

    public void setMaxEngagement(int maxEngagement) {
        this.maxEngagement = maxEngagement;
        notifyPropertyChanged(BR.maxEngagement); // Notify when maxEngagement changes
    }

    @Bindable
    public int getReservationDeadline() {
        return reservationDeadline;
    }

    public void setReservationDeadline(int reservationDeadline) {
        this.reservationDeadline = reservationDeadline;
        notifyPropertyChanged(BR.reservationDeadline); // Notify when reservationDeadline changes
    }

    @Bindable
    public int getCancellationDeadline() {
        return cancellationDeadline;
    }

    public void setCancellationDeadline(int cancellationDeadline) {
        this.cancellationDeadline = cancellationDeadline;
        notifyPropertyChanged(BR.cancellationDeadline); // Notify when cancellationDeadline changes
    }

    @Bindable
    public ReservationMethod getReservationMethod() {
        return reservationMethod;
    }

    public void setReservationMethod(ReservationMethod reservationMethod) {
        this.reservationMethod = reservationMethod;
        this.notifyPropertyChanged(BR.reservationMethod); // Notify when reservationMethod changes
    }

    @Override
    public String toString() {
        return "Service{" +
                "specifity='" + specifity + '\'' +
                ", duration=" + duration +
                ", minEngagement=" + minEngagement +
                ", maxEngagement=" + maxEngagement +
                ", reservationDeadline=" + reservationDeadline +
                ", cancellationDeadline=" + cancellationDeadline +
                ", reservationMethod=" + reservationMethod +
                "} " + super.toString();
    }

    public boolean isAutomaticReservation() {
        return ReservationMethod.AUTOMATIC.equals(reservationMethod);
    }

    public boolean isManualReservation() {
        return ReservationMethod.MANUAL.equals(reservationMethod);
    }


}
