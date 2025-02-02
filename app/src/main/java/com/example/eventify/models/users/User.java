package com.example.eventify.models.users;


import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.databinding.Bindable;
import androidx.databinding.Observable;

import com.example.eventify.models.enums.UserRole;
import com.example.eventify.models.events.Event;
import com.example.eventify.models.solutions.Solution;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.Set;

public class User implements Parcelable, Observable {
    private String id;
    @Bindable
    private String email;
    private String password;
    private Timestamp lastPasswordResetDate;
    @Bindable
    private String address;
    @Bindable
    private String phoneNumber;
    private String profileImage;
    private Role role;
    private boolean suspended;
    public Date suspensionEndDate;

    private boolean activated;
    private Set<Solution> favoriteSolutions;
    private Set<Event> attendingEvents;

    public User() {
    }

    public User(String email,
                String password,
                String address,
                String phoneNumber,
                String profileImage) {
        this.email = email;
        this.password = password;
        this.lastPasswordResetDate = new Timestamp(System.currentTimeMillis());
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.role = new Role(UserRole.AUTHENTICATED_USER);
        this.profileImage = profileImage;
        this.suspended = false;
        this.activated = false;
    }

    protected User(Parcel in) {
        id = in.readString();
        email = in.readString();
        password = in.readString();
        address = in.readString();
        phoneNumber = in.readString();
        profileImage = in.readString();
        suspended = in.readByte() != 0;
        activated = in.readByte() != 0;
        lastPasswordResetDate = (Timestamp) in.readSerializable();
        suspensionEndDate = (Date) in.readSerializable();
        role = in.readParcelable(Role.class.getClassLoader());
        favoriteSolutions = new java.util.HashSet<>(in.createTypedArrayList(Solution.CREATOR));
        attendingEvents = new java.util.HashSet<>(in.createTypedArrayList(Event.CREATOR));
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
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

    public Timestamp getLastPasswordResetDate() {
        return lastPasswordResetDate;
    }

    public void setLastPasswordResetDate(Timestamp lastPasswordResetDate) {
        this.lastPasswordResetDate = lastPasswordResetDate;
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

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public boolean isSuspended() {
        return suspended;
    }

    public void setSuspended(boolean suspended) {
        this.suspended = suspended;
    }

    public Date getSuspensionEndDate() {
        return suspensionEndDate;
    }

    public void setSuspensionEndDate(Date suspensionEndDate) {
        this.suspensionEndDate = suspensionEndDate;
    }

    public boolean isActivated() {
        return activated;
    }

    public void setActivated(boolean activated) {
        this.activated = activated;
    }

    public Set<Solution> getFavoriteSolutions() {
        return favoriteSolutions;
    }

    public void setFavoriteSolutions(Set<Solution> favoriteSolutions) {
        this.favoriteSolutions = favoriteSolutions;
    }

    public Set<Event> getAttendingEvents() {
        return attendingEvents;
    }

    public void setAttendingEvents(Set<Event> attendingEvents) {
        this.attendingEvents = attendingEvents;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int i) {
        parcel.writeString(id);
        parcel.writeString(email);
        parcel.writeString(password);
        parcel.writeString(address);
        parcel.writeString(phoneNumber);
        parcel.writeString(profileImage);
        parcel.writeByte((byte) (suspended ? 1 : 0)); // Convert boolean to byte (1 = true, 0 = false)
        parcel.writeByte((byte) (activated ? 1 : 0)); // Convert boolean to byte
        parcel.writeSerializable(lastPasswordResetDate); // Serialize Timestamp as it implements Serializable
        parcel.writeSerializable(suspensionEndDate); // Serialize Date as it implements Serializable
        parcel.writeParcelable(role, i); // Assuming Role implements Parcelable
        parcel.writeTypedList(favoriteSolutions != null ? new ArrayList<>(favoriteSolutions) : null); // Convert Set to List
        parcel.writeTypedList(attendingEvents != null ? new ArrayList<>(attendingEvents) : null); // Convert Set to List
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @NonNull
        @Override
        public User createFromParcel(@NonNull Parcel in) {
            return new User(in);
        }

        @NonNull
        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    @Override
    public void addOnPropertyChangedCallback(OnPropertyChangedCallback callback) {

    }

    @Override
    public void removeOnPropertyChangedCallback(OnPropertyChangedCallback callback) {

    }
}
