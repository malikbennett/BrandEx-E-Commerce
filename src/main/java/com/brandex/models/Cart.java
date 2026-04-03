package com.brandex.models;

import java.time.OffsetDateTime;

public class Cart extends Model {
    private String userId;
    private double totalPrice;
    private OffsetDateTime updatedAt;

    public Cart() {
    }

    public String getUserId() {
        return this.userId;
    }

    public double getTotalPrice() {
        return this.totalPrice;
    }

    public OffsetDateTime getUpdatedAt() {
        return this.updatedAt;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setTotalPrice(double total) {
        this.totalPrice = total;
    }

    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
