package com.example.parkingapp.Model;

import java.util.Date;
public class Spot {
    private Date duration;
    private String place;
    private String userId;
    private String sTime;
    public String getsTime() {
        return sTime;
    }
    public Date getDuration() {
        return duration;
    }
    public String getPlace() {
        return place;
    }
    public String getUserId() {
        return userId;
    }
}
