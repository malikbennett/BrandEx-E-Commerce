package com.brandex.models;

import java.time.OffsetDateTime;

public class Model {
    private String id;
    private OffsetDateTime createdAt;

    public String getId() {
        return this.id;
    }

    public OffsetDateTime getCreatedAt() {
        return this.createdAt;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setCreatedAt(OffsetDateTime created_at) {
        this.createdAt = created_at;
    }
}
