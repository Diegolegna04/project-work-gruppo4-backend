package com.example.rest.model;


import java.sql.Time;
import java.time.LocalDateTime;
import java.util.Date;


public class OrderDateRequest {
    public LocalDateTime pickupDateTime;


    public OrderDateRequest() {

    }
    public OrderDateRequest(LocalDateTime pickupDateTime) {
        this.pickupDateTime = pickupDateTime;
    }

    public LocalDateTime getPickupDateTime() {
        return pickupDateTime;
    }

    public void setPickupDateTime(LocalDateTime pickupDateTime) {
        this.pickupDateTime = pickupDateTime;
    }
}
