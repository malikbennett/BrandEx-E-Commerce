package com.brandex.models;

import java.time.OffsetDateTime;

public class Cart extends Model {
    private String userId;
    private double total;
    private OffsetDateTime updatedAt;

    public Cart() {
    }

    public String getUserId() {
        return this.userId;
    }

    public double getTotal() {
        return this.total;
    }

    public OffsetDateTime getUpdatedAt() {
        return this.updatedAt;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
