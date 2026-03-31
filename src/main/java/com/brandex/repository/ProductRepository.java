package com.brandex.repository;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.brandex.database.JDBC;
import com.brandex.datastructures.LinkedList;
import com.brandex.models.Product;

public class ProductRepository {
    private static ProductRepository instance;

    public static ProductRepository getInstance() {
        if (instance == null)
            instance = new ProductRepository();
        return instance;
    }

    public LinkedList<Product> listProducts() {
        LinkedList<Product> products = new LinkedList<>();
        String sql = "SELECT * FROM products";
        try {
            ResultSet rs = JDBC.query(sql);
            while (rs.next()) {
                Product product = new Product();
                product.setId(rs.getString("id"));
                product.setName(rs.getString("name"));
                product.setDescription(rs.getString("description"));
                product.setCategory(rs.getString("category"));
                product.setBrand(rs.getString("brand"));
                product.setImageUrl(rs.getString("image_url"));
                product.setPrice(rs.getDouble("price"));
                product.setRating(rs.getDouble("rating"));
                product.setStock(rs.getInt("stock"));

                products.insert(product);
            }
        } catch (SQLException e) {
            System.err.println("Database Error: Failed to fetch products. " + e.getMessage());
            e.printStackTrace();
        }
        return products;
    }
}
