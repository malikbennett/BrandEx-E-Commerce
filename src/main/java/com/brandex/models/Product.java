package com.brandex.models;


public class Product extends Model {
    private String name, description;
    private double price;
    private int stock;

    Product() {}

    public String getName() { return this.name; }
    public String getDescription() { return this.description; }
    public double getPrice() { return this.price; }
    public int getStock() { return this.stock; }

    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setPrice(double price) { this.price = price; }
    public void setStock(int stock) { this.stock = stock; }
}
