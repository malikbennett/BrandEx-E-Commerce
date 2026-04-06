package com.brandex.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;

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

    // ── READ ──────────────────────────────────────────────────────────────────

    public LinkedList<Product> listProducts() {
        LinkedList<Product> products = new LinkedList<>((a, b) -> 0);
        String sql = "SELECT * FROM products";
        try {
            ResultSet rs = JDBC.query(sql);
            while (rs.next()) {
                products.insert(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("DB Error - listProducts: " + e.getMessage());
        }
        return products;
    }

    // ── CREATE ────────────────────────────────────────────────────────────────

    /**
     * Inserts a new product and returns the DB-generated UUID string,
     * or null on failure.
     *
     * Assumes the DB column is: id UUID PRIMARY KEY DEFAULT gen_random_uuid()
     * We use RETURNING id to get the value immediately.
     */
    public String createProduct(Product product) {
        String sql = "INSERT INTO products (name, description, category, brand, image_url, price, rating, stock) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?) RETURNING id";
        try {
            ResultSet rs = JDBC.query(sql,
                    product.getName(),
                    product.getDescription(),
                    product.getCategory(),
                    product.getBrand(),
                    product.getImageUrl(),
                    product.getPrice(),
                    product.getRating(),
                    product.getStock());
            if (rs.next())
                return rs.getString("id");
        } catch (SQLException e) {
            System.err.println("DB Error - insertProduct: " + e.getMessage());
        }
        return null;
    }

    // ── UPDATE ────────────────────────────────────────────────────────────────

    /**
     * Updates every mutable field for the given product id.
     * Returns true if the execute succeeded without throwing.
     */
    public boolean updateProduct(Product p) {
        String sql = "UPDATE products "
                + "SET name=?, description=?, category=?, brand=?, image_url=?, price=?, rating=?, stock=? "
                + "WHERE id=?";
        try {
            JDBC.execute(sql,
                    p.getName(),
                    p.getDescription(),
                    p.getCategory(),
                    p.getBrand(),
                    p.getImageUrl(),
                    p.getPrice(),
                    p.getRating(),
                    p.getStock(),
                    p.getId());
            return true;
        } catch (SQLException e) {
            System.err.println("DB Error - updateProduct: " + e.getMessage());
            return false;
        }
    }

    // ── DELETE ────────────────────────────────────────────────────────────────

    /**
     * Deletes orphaned cart_items first (avoids FK violation), then deletes
     * the product row itself.
     * Returns true if both executes succeeded without throwing.
     */
    public boolean deleteProduct(String productId) {
        try {
            JDBC.execute("DELETE FROM cart_items WHERE product_id = ?", productId);
            JDBC.execute("DELETE FROM products   WHERE id = ?", productId);
            return true;
        } catch (SQLException e) {
            System.err.println("DB Error - deleteProduct: " + e.getMessage());
            return false;
        }
    }

    // ── HELPER ────────────────────────────────────────────────────────────────

    private Product mapRow(ResultSet rs) throws SQLException {
        Product p = new Product();
        p.setId(rs.getString("id"));
        p.setName(rs.getString("name"));
        p.setDescription(rs.getString("description"));
        p.setCategory(rs.getString("category"));
        p.setBrand(rs.getString("brand"));
        p.setImageUrl(rs.getString("image_url"));
        p.setPrice(rs.getDouble("price"));
        p.setRating(rs.getDouble("rating"));
        p.setStock(rs.getInt("stock"));
        p.setCreatedAt(rs.getObject("created_at", OffsetDateTime.class));
        return p;
    }
}
