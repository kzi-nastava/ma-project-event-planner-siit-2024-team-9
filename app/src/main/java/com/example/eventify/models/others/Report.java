package com.example.eventify.models.others;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.example.eventify.models.enums.Status;
import com.example.eventify.models.users.User;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Report implements Parcelable {

    @SerializedName("id")
    @Expose
    private String id;

    @SerializedName("reason")
    @Expose
    private String reason;

    @SerializedName("description")
    @Expose
    private String description;

    @SerializedName("reportedBy")
    @Expose
    private User reportedBy;

    @SerializedName("reportedUser")
    @Expose
    private User reportedUser;

    @SerializedName("reportStatus")
    @Expose
    private Status reportStatus;

    public Report() { }

    public Report(String id,
                  String reason,
                  String description,
                  User reportedBy,
                  User reportedUser,
                  Status reportStatus) {
        this.id = id;
        this.reason = reason;
        this.description = description;
        this.reportedBy = reportedBy;
        this.reportedUser = reportedUser;
        this.reportStatus = reportStatus;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public User getReportedBy() { return reportedBy; }
    public void setReportedBy(User reportedBy) { this.reportedBy = reportedBy; }

    public User getReportedUser() { return reportedUser; }
    public void setReportedUser(User reportedUser) { this.reportedUser = reportedUser; }

    public Status getReportStatus() { return reportStatus; }
    public void setReportStatus(Status reportStatus) { this.reportStatus = reportStatus; }

    // --- Parcelable ---
    protected Report(Parcel in) {
        id = in.readString();
        reason = in.readString();
        description = in.readString();
        reportedBy = in.readParcelable(User.class.getClassLoader());
        reportedUser = in.readParcelable(User.class.getClassLoader());
        String statusName = in.readString();
        reportStatus = statusName != null ? Status.valueOf(statusName) : null;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(reason);
        dest.writeString(description);
        dest.writeParcelable(reportedBy, flags);
        dest.writeParcelable(reportedUser, flags);
        dest.writeString(reportStatus != null ? reportStatus.name() : null);
    }

    @Override
    public int describeContents() { return 0; }

    public static final Creator<Report> CREATOR = new Creator<Report>() {
        @Override public Report createFromParcel(Parcel in) { return new Report(in); }
        @Override public Report[] newArray(int size) { return new Report[size]; }
    };
}
