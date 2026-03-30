package com.brandex.models;

import java.time.OffsetDateTime;

public class Product {
    private int id;
    private String name, description;
    private double price;
    private int stock;
    private OffsetDateTime created_at;

    Product() {}

    public int getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public double getPrice() { return price; }
    public int getStock() { return stock; }
    public OffsetDateTime getCreatedAt() { return created_at; }

    public void setId(int id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setPrice(double price) { this.price = price; }
    public void setStock(int stock) { this.stock = stock; }
    public void setCreatedAt(OffsetDateTime created_at) { this.created_at = created_at; }
}
