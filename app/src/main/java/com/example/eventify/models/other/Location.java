package com.example.eventify.models.other;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

public class Location implements Parcelable {
    @SerializedName("name")
    private String name;
    
    @SerializedName("address")
    private String address;
    
    @SerializedName("city")
    private String city;
    
    @SerializedName("country")
    private String country;
    
    @SerializedName("longitude")
    private double longitude;
    
    @SerializedName("latitude")
    private double latitude;

    public Location() {}

    public Location(String name, String address, String city, String country, double longitude, double latitude) {
        this.name = name;
        this.address = address;
        this.city = city;
        this.country = country;
        this.longitude = longitude;
        this.latitude = latitude;
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    // Parcelable implementation
    protected Location(Parcel in) {
        name = in.readString();
        address = in.readString();
        city = in.readString();
        country = in.readString();
        longitude = in.readDouble();
        latitude = in.readDouble();
    }

    public static final Creator<Location> CREATOR = new Creator<Location>() {
        @Override
        public Location createFromParcel(Parcel in) {
            return new Location(in);
        }

        @Override
        public Location[] newArray(int size) {
            return new Location[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(address);
        dest.writeString(city);
        dest.writeString(country);
        dest.writeDouble(longitude);
        dest.writeDouble(latitude);
    }
}
