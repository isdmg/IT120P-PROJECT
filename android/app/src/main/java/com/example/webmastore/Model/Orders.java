package com.example.webmastore.Model;

import com.google.firebase.Timestamp;

public class Orders {

    private String OrderId, OrderStatus;
    private com.google.firebase.Timestamp Timestamp;

    public Orders() {
    }

    public Orders(String OrderId, String OrderStatus, com.google.firebase.Timestamp Timestamp) {
        this.OrderId = OrderId;
        this.OrderStatus = OrderStatus;
        this.Timestamp = Timestamp;
    }

    public String getOrderId() {
        return OrderId;
    }

    public void setOrderId(String orderId) {
        OrderId = orderId;
    }

    public String getOrderStatus() {
        return OrderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        OrderStatus = orderStatus;
    }

    public com.google.firebase.Timestamp getTimestamp() {
        return Timestamp;
    }

    public void setTimestamp(com.google.firebase.Timestamp timestamp) {
        Timestamp = timestamp;
    }
}
