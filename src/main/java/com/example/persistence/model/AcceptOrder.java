package com.example.persistence.model;

import org.bson.types.ObjectId;

public class AcceptOrder {
    public ObjectId orderId;
    public boolean accepted;


    public AcceptOrder() {

    }
    public AcceptOrder(ObjectId orderId, boolean accepted) {
        this.orderId = orderId;
        this.accepted = accepted;
    }


    public ObjectId getOrderId() {
        return orderId;
    }

    public void setOrderId(ObjectId orderId) {
        this.orderId = orderId;
    }

    public boolean isAccepted() {
        return accepted;
    }

    public void setAccepted(boolean accepted) {
        this.accepted = accepted;
    }
}
