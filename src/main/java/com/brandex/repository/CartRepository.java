package com.brandex.repository;

import com.brandex.models.Cart;
import com.brandex.database.JDBC;
import java.sql.*;
import java.time.OffsetDateTime;

public class CartRepository {

    private static CartRepository instance;

    private CartRepository() {
    }

    public static CartRepository getInstance() {
        if (instance == null) {
            instance = new CartRepository();
        }
        return instance;
    }

    public Cart getCart(String condition, String value) throws SQLException {
        String sql = "SELECT * FROM cart WHERE " + condition + " = ?";
        ResultSet rs = JDBC.query(sql, value);

        if (rs.next()) {
            Cart cart = new Cart();
            cart.setId(rs.getString("id"));
            cart.setUserId(rs.getString("user_id"));
            cart.setTotal(rs.getDouble("total_price"));
            cart.setUpdatedAt(rs.getObject("updated_at", OffsetDateTime.class));
            return cart;
        }
        return null;
    }

    public void createCart(Cart cart) throws SQLException {
        String sql = "INSERT INTO cart (user_id, total_price, updated_at) VALUES (?, ?, ?)";
        JDBC.execute(sql, cart.getUserId(), cart.getTotal(), cart.getUpdatedAt());
    }
}
