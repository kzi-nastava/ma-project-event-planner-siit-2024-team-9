package com.example.eventify.models.auth;

import com.google.gson.annotations.SerializedName;

public class UserTokenState {

    @SerializedName("accessToken")
    private String accessToken;

    @SerializedName("expiresIn")
    private Long expiresIn;

    @SerializedName("suspended")
    private Boolean suspended;

    @SerializedName("remainingTime")
    private Long remainingTime;

    public UserTokenState() {
    }

    public UserTokenState(String accessToken, long expiresIn) {
        this.accessToken = accessToken;
        this.expiresIn = expiresIn;
    }

    public UserTokenState(String accessToken, Long expiresIn, Boolean suspended, Long remainingTime) {
        this.accessToken = accessToken;
        this.expiresIn = expiresIn;
        this.suspended = suspended;
        this.remainingTime = remainingTime;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public Long getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(Long expiresIn) {
        this.expiresIn = expiresIn;
    }

    public Boolean getSuspended() {
        return suspended;
    }

    public void setSuspended(Boolean suspended) {
        this.suspended = suspended;
    }

    public Long getRemainingTime() {
        return remainingTime;
    }

    public void setRemainingTime(Long remainingTime) {
        this.remainingTime = remainingTime;
    }

    public boolean isSuspended() {
        return Boolean.TRUE.equals(suspended);
    }

    public long getRemainingTimeOrZero() {
        return remainingTime != null ? remainingTime : 0L;
    }

    public boolean hasToken() {
        return accessToken != null && !accessToken.isEmpty();
    }

    @Override
    public String toString() {
        return "UserTokenState{" +
                "accessToken=" + (accessToken != null ? "***" : "null") +
                ", expiresIn=" + expiresIn +
                ", suspended=" + suspended +
                ", remainingTime=" + remainingTime +
                '}';
    }
}
