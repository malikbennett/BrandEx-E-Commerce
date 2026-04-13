package com.brandex.models;

import com.brandex.models.enums.OrderStatus;
import com.brandex.models.enums.PaymentMethod;

// The ADT Order, which extends the ADT Model.
public class Order extends Model {
    private String userId;
    private String orderNumber;
    private OrderStatus status;
    private String shippingAddress;
    private PaymentMethod paymentMethod;
    private double total;

    public Order() {
    }

    public String getUserId() {
        return this.userId;
    }

    public String getOrderNumber() {
        return this.orderNumber;
    }

    public OrderStatus getStatus() {
        return this.status;
    }

    public String getShippingAddress() {
        return this.shippingAddress;
    }

    public PaymentMethod getPaymentMethod() {
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

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public void setShippingAddress(String shippingAddress) {
        this.shippingAddress = shippingAddress;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public void setTotal(double total) {
        this.total = total;
    }

}
