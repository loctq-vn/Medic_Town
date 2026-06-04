package com.example.medictown.data.models;

public class FakePaymentMethodRequest {
    public String order_id;
    public String method;

    public FakePaymentMethodRequest(String orderId, String method) {
        this.order_id = orderId;
        this.method = method;
    }
}
