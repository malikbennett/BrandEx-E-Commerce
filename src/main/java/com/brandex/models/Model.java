package com.brandex.models;

import java.time.OffsetDateTime;

public class Model {
    private String id;
    private OffsetDateTime created_at;

    public String getId() { return this.id;}
    public OffsetDateTime getCreated_at() { return this.created_at; }

    public void setId(String id) { this.id = id; }
    public void setCreated_at(OffsetDateTime created_at) { this.created_at = created_at; }
}
