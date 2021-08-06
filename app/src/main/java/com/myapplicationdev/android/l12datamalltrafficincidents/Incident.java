package com.myapplicationdev.android.l12datamalltrafficincidents;

import java.io.Serializable;
import java.util.Date;

public class Incident implements Serializable {

    private String type;
    private double latitude;
    private double longitude;
    private String message;
    private Date date;

    public Incident() {
    }

    public Incident(String type, double latitude, double longitude, String message, Date date) {
        this.type = type;
        this.latitude = latitude;
        this.longitude = longitude;
        this.message = message;
        this.date = date;
    }

    public String getType() {
        return type;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getMessage() {
        return message;
    }

    public Date getDate() {
        return date;
    }
}
