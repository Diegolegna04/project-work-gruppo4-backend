package com.example.rest.model;


import java.time.LocalDateTime;


public class OrderRequest {
    public LocalDateTime pickupDateTime;
    public String notes;


    public OrderRequest() {

    }
    public OrderRequest(LocalDateTime pickupDateTime, String notes) {
        this.pickupDateTime = pickupDateTime;
        this.notes = notes;
    }

    public LocalDateTime getPickupDateTime() {
        return pickupDateTime;
    }

    public void setPickupDateTime(LocalDateTime pickupDateTime) {
        this.pickupDateTime = pickupDateTime;
    }
}
