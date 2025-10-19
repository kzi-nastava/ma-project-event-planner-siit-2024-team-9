package com.example.eventify.models.users;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.util.UUID;

public class EventOrganizer implements Parcelable {
    @SerializedName("id")
    private UUID id;
    
    @SerializedName("email")
    private String email;
    
    @SerializedName("password")
    private String password;
    
    @SerializedName("address")
    private String address;
    
    @SerializedName("phoneNumber")
    private String phoneNumber;
    
    @SerializedName("profileImage")
    private String profileImage;
    
    @SerializedName("firstName")
    private String firstName;
    
    @SerializedName("lastName")
    private String lastName;
    
    @SerializedName("role")
    private UserRole role;

    public EventOrganizer() {}

    public EventOrganizer(UUID id, String email, String password, String address, String phoneNumber, 
                         String profileImage, String firstName, String lastName, UserRole role) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.profileImage = profileImage;
        this.firstName = firstName;
        this.lastName = lastName;
        this.role = role;
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    // Parcelable implementation
    protected EventOrganizer(Parcel in) {
        id = (UUID) in.readSerializable();
        email = in.readString();
        password = in.readString();
        address = in.readString();
        phoneNumber = in.readString();
        profileImage = in.readString();
        firstName = in.readString();
        lastName = in.readString();
        role = in.readParcelable(UserRole.class.getClassLoader());
    }

    public static final Creator<EventOrganizer> CREATOR = new Creator<EventOrganizer>() {
        @Override
        public EventOrganizer createFromParcel(Parcel in) {
            return new EventOrganizer(in);
        }

        @Override
        public EventOrganizer[] newArray(int size) {
            return new EventOrganizer[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeSerializable(id);
        dest.writeString(email);
        dest.writeString(password);
        dest.writeString(address);
        dest.writeString(phoneNumber);
        dest.writeString(profileImage);
        dest.writeString(firstName);
        dest.writeString(lastName);
        dest.writeParcelable(role, flags);
    }
}
