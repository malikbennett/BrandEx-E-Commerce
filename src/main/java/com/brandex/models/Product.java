package com.brandex.models;


public class Product extends Model {
    private String name, description, category, brand, imageUrl;
    private double price, rating;
    private int stock;

    public Product() {}

    public String getName() { return this.name; }
    public String getDescription() { return this.description; }
    public String getCategory() { return this.category; }
    public String getBrand() { return this.brand; }
    public String getImageUrl() { return this.imageUrl; }
    public double getPrice() { return this.price; }
    public double getRating() { return this.rating; }
    public int getStock() { return this.stock; }

    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setCategory(String category) { this.category = category; }
    public void setBrand(String brand) { this.brand = brand; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public void setPrice(double price) { this.price = price; }
    public void setRating(double rating) { this.rating = rating; }
    public void setStock(int stock) { this.stock = stock; }
}
