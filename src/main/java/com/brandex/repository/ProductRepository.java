package com.brandex.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;

import com.brandex.database.JDBC;
import com.brandex.datastructures.LinkedList;
import com.brandex.models.Product;
import com.brandex.database.DatabaseException;

// The repository class for managing products.
public class ProductRepository {
    private static ProductRepository instance;

    // Returns the instance of the ProductRepository
    public static ProductRepository getInstance() {
        if (instance == null)
            instance = new ProductRepository();
        return instance;
    }

    // Returns a list of all products
    public LinkedList<Product> listProducts() {
        LinkedList<Product> products = new LinkedList<>((a, b) -> 0);
        String sql = "SELECT * FROM products";
        try (ResultSet rs = JDBC.query(sql)) {
            while (rs.next()) {
                products.insert(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to list products", e);
        }
        return products;
    }

    // Returns a product based on the condition and value
    public Product findById(String id) {
        String sql = "SELECT * FROM products WHERE id = ?::uuid";
        try (ResultSet rs = JDBC.query(sql, id)) {
            if (rs.next()) {
                return mapRow(rs);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to find product by ID: " + id, e);
        }
        return null;
    }

    // Creates a new product
    public String createProduct(Product product) {
        String sql = "INSERT INTO products (name, description, category, brand, image_url, price, rating, stock) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?) RETURNING id";
        try (ResultSet rs = JDBC.query(sql,
                product.getName(),
                product.getDescription(),
                product.getCategory(),
                product.getBrand(),
                product.getImageUrl(),
                product.getPrice(),
                product.getRating(),
                product.getStock())) {
            if (rs.next())
                return rs.getString("id");
        } catch (SQLException e) {
            throw new DatabaseException("Failed to create product: " + product.getName(), e);
        }
        return null;
    }

    // Updates a product
    public boolean updateProduct(Product p) {
        String sql = "UPDATE products "
                + "SET name=?, description=?, category=?, brand=?, image_url=?, price=?, rating=?, stock=? "
                + "WHERE id=?::uuid";
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
    }

    // Deletes a product
    public boolean deleteProduct(String productId) {
        JDBC.execute("DELETE FROM cart_item WHERE product_id = ?::uuid", productId);
        JDBC.execute("DELETE FROM products  WHERE id = ?::uuid", productId);
        return true;
    }

    // Maps a result set row to a product
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
