package com.example.parkingapp.Model;

import java.util.Date;

public class User {
    private String Credits;
    private String Email;
    private String Status;
    private String Password;
    private Date duration;

    public Date getDuration() {
        return duration;
    }

    public String getPassword() {
        return Password;
    }

    public String getCredits() {
        return Credits;
    }

    public String getEmail() {
        return Email;
    }

    public String getStatus() {
        return Status;
    }
}
