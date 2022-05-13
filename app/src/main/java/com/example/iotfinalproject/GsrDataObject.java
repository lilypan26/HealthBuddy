package com.example.iotfinalproject;

import com.google.gson.annotations.SerializedName;

import java.sql.Time;
import java.sql.Timestamp;

import GsrDataID.model.GsrData;

public class GsrDataObject {
    private Timestamp timestamp;
    private Integer gsrValue;

    public GsrDataObject(Timestamp timestamp, Integer gsrValue) {
        this.timestamp = timestamp;
        this.gsrValue = gsrValue;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public Timestamp getTimestamp() { return this.timestamp; }

    public Integer getGsr() {
        return this.gsrValue;
    }

    public void setGsr(Integer gsr) {
        this.gsrValue = gsr;
    }
}