package com.example.eventify.models.users;


import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.databinding.Bindable;
import androidx.databinding.Observable;

import com.example.eventify.models.solutions.Solution;

import java.util.HashSet;
import java.util.Set;

public class BusinessOwner extends User implements Parcelable, Observable {

    @Bindable
    private String name;
    private String description;
    private Set<Solution> solutions;
    private Set<String> images;

    public BusinessOwner(String email, String password, String address, String phoneNumber, String profileImage, String name, String description, Set<String> images) {
        super(email, password, address, phoneNumber, profileImage);
        this.name = name;
        this.description = description;
        this.images = images;
    }

    public BusinessOwner() {
        super();
    }

    protected BusinessOwner(Parcel in) {
        super(in);
        name = in.readString();
        description = in.readString();
        solutions = new HashSet<>(in.createTypedArrayList(Solution.CREATOR));
        images = new HashSet<>(in.createStringArrayList());
    }

    public static final Creator<BusinessOwner> CREATOR = new Creator<BusinessOwner>() {
        @Override
        public BusinessOwner createFromParcel(Parcel in) {
            return new BusinessOwner(in);
        }

        @Override
        public BusinessOwner[] newArray(int size) {
            return new BusinessOwner[size];
        }
    };

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

    public Set<Solution> getSolutions() {
        return solutions;
    }

    public void setSolutions(Set<Solution> solutions) {
        this.solutions = solutions;
    }

    public Set<String> getImages() {
        return images;
    }

    public void setImages(Set<String> images) {
        this.images = images;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(name);
        dest.writeString(description);
        dest.writeTypedList(solutions != null ? new java.util.ArrayList<>(solutions) : null);
        dest.writeStringList(new java.util.ArrayList<>(images));
    }
}

