package com.brandex.repository;

import com.brandex.models.Cart;
import com.brandex.models.CartItem;
import com.brandex.database.JDBC;
import com.brandex.datastructures.LinkedList;

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

    public void createCart(Cart cart) {
        String sql = "INSERT INTO cart (user_id) VALUES (?::uuid)";
        try {
            JDBC.execute(sql, cart.getUserId());
        } catch (Exception e) {
            System.err.println("Database Error: Failed to create cart. " + e.getMessage());
            e.printStackTrace();
        }
    }

    public Cart getCart(String condition, String value) {
        String cast = (condition.equals("id") || condition.equals("user_id")) ? "::uuid" : "";
        String sql = "SELECT * FROM cart WHERE " + condition + " = ?" + cast;
        try {
            ResultSet rs = JDBC.query(sql, value);
            if (rs.next()) {
                Cart cart = new Cart();
                cart.setId(rs.getString("id"));
                cart.setUserId(rs.getString("user_id"));
                cart.setTotal(rs.getDouble("total_price"));
                cart.setUpdatedAt(rs.getObject("updated_at", OffsetDateTime.class));
                return cart;
            }
        } catch (Exception e) {
            System.err.println("Database Error: Failed to fetch cart. " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public LinkedList<CartItem> listCartItems(String cartId) {
        LinkedList<CartItem> items = new LinkedList<>((a, b) -> 0);
        String sql = "SELECT * FROM cart_item WHERE cart_id = ?::uuid";
        try {
            ResultSet rs = JDBC.query(sql, cartId);
            while (rs.next()) {
                CartItem item = new CartItem();
                item.setId(rs.getString("id"));
                item.setCartId(rs.getString("cart_id"));
                item.setProductId(rs.getString("product_id"));
                item.setQuantity(rs.getInt("quantity"));
                item.setCreatedAt(rs.getObject("created_at", OffsetDateTime.class));
                items.insert(item);
            }
        } catch (Exception e) {
            System.err.println("Database Error: Failed to fetch cart items. " + e.getMessage());
            e.printStackTrace();
        }
        return items;
    }

    public CartItem getCartItem(String condition, String value) {
        String cast = (condition.equals("id") || condition.equals("cart_id") || condition.equals("product_id"))
                ? "::uuid"
                : "";
        String sql = "SELECT * FROM cart_item WHERE " + condition + " = ?" + cast;
        try {
            ResultSet rs = JDBC.query(sql, value);
            if (rs.next()) {
                CartItem item = new CartItem();
                item.setId(rs.getString("id"));
                item.setCartId(rs.getString("cart_id"));
                item.setProductId(rs.getString("product_id"));
                item.setQuantity(rs.getInt("quantity"));
                item.setCreatedAt(rs.getObject("created_at", OffsetDateTime.class));
                return item;
            }
        } catch (Exception e) {
            System.err.println("Database Error: Failed to fetch cart item. " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public void createCartItem(String cartId, String productId, int quantity) {
        String sql = "INSERT INTO cart_item (cart_id, product_id, quantity) VALUES (?::uuid, ?::uuid, ?)";
        try {
            JDBC.execute(sql, cartId, productId, quantity);
        } catch (Exception e) {
            System.err.println("Database Error: Failed to create cart item. " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void updateCartItem(String cartItemId, String condition, String value) {
        String sql = "UPDATE cart_item SET " + condition + " = ? WHERE id = ?::uuid";
        try {
            JDBC.execute(sql, value, cartItemId);
        } catch (Exception e) {
            System.err.println("Database Error: Failed to update cart item. " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void deleteCartItem(String cartItemId) {
        String sql = "DELETE FROM cart_item WHERE id = ?::uuid";
        try {
            JDBC.execute(sql, cartItemId);
        } catch (Exception e) {
            System.err.println("Database Error: Failed to delete cart item. " + e.getMessage());
            e.printStackTrace();
        }
    }
}
