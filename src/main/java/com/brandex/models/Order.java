package com.brandex.models;

public class Order extends Model {
    private String userId;
    private String orderNumber;
    private String status;
    private String shippingAddress;
    private String paymentMethod;
    private double total;

    public String getUserId() {
        return this.userId;
    }

    public String getOrderNumber() {
        return this.orderNumber;
    }

    public String getStatus() {
        return this.status;
    }

    public String getShippingAddress() {
        return this.shippingAddress;
    }

    public String getPaymentMethod() {
        return this.paymentMethod;
    }

    public double getTotal() {
        return this.total;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setShippingAddress(String shippingAddress) {
        this.shippingAddress = shippingAddress;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public void setTotal(double total) {
        this.total = total;
    }

}
