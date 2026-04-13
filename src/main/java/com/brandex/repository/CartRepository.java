package com.brandex.repository;

import com.brandex.models.Cart;
import com.brandex.models.CartItem;
import com.brandex.database.JDBC;
import com.brandex.datastructures.LinkedList;
import com.brandex.database.DatabaseException;

import java.sql.*;
import java.time.OffsetDateTime;

// The repository class for managing carts.
public class CartRepository {

    private static CartRepository instance;

    // Returns the instance of the CartRepository
    private CartRepository() {
    }

    // Returns the instance of the CartRepository
    public static CartRepository getInstance() {
        if (instance == null) {
            instance = new CartRepository();
        }
        return instance;
    }

    // Creates a new cart
    public void createCart(Cart cart) {
        String sql = "INSERT INTO carts (user_id) VALUES (?::uuid)";
        JDBC.execute(sql, cart.getUserId());
    }

    // Returns a cart based on the condition and value
    public Cart getCart(String condition, String value) {
        String cast = (condition.equals("id") || condition.equals("user_id")) ? "::uuid" : "";
        String sql = "SELECT * FROM carts WHERE " + condition + " = ?" + cast;
        try (ResultSet rs = JDBC.query(sql, value)) {
            if (rs.next()) {
                Cart cart = new Cart();
                cart.setId(rs.getString("id"));
                cart.setUserId(rs.getString("user_id"));
                cart.setTotalPrice(rs.getDouble("total_price"));
                cart.setUpdatedAt(rs.getObject("updated_at", OffsetDateTime.class));
                return cart;
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to fetch cart. condition=" + condition + ", value=" + value, e);
        }
        return null;
    }

    // Returns a list of cart items for a given cart ID
    public LinkedList<CartItem> listCartItems(String cartId) {
        LinkedList<CartItem> items = new LinkedList<>((a, b) -> 0);
        String sql = "SELECT * FROM cart_item WHERE cart_id = ?::uuid";
        try (ResultSet rs = JDBC.query(sql, cartId)) {
            while (rs.next()) {
                CartItem item = new CartItem();
                item.setId(rs.getString("id"));
                item.setCartId(rs.getString("cart_id"));
                item.setProductId(rs.getString("product_id"));
                item.setQuantity(rs.getInt("quantity"));
                item.setTotalPrice(rs.getDouble("total_price"));
                item.setCreatedAt(rs.getObject("created_at", OffsetDateTime.class));
                items.insert(item);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to fetch cart items for cartId: " + cartId, e);
        }
        return items;
    }

    // Returns a cart item based on the condition and value
    public CartItem getCartItem(String condition, String value) {
        String cast = (condition.equals("id") || condition.equals("cart_id") || condition.equals("product_id"))
                ? "::uuid"
                : "";
        String sql = "SELECT * FROM cart_item WHERE " + condition + " = ?" + cast;
        try (ResultSet rs = JDBC.query(sql, value)) {
            if (rs.next()) {
                CartItem item = new CartItem();
                item.setId(rs.getString("id"));
                item.setCartId(rs.getString("cart_id"));
                item.setProductId(rs.getString("product_id"));
                item.setQuantity(rs.getInt("quantity"));
                item.setTotalPrice(rs.getDouble("total_price"));
                item.setCreatedAt(rs.getObject("created_at", OffsetDateTime.class));
                return item;
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to fetch cart item. condition=" + condition + ", value=" + value, e);
        }
        return null;
    }

    // Returns a cart item by product
    public CartItem getCartItemByProduct(String cartId, String productId) {
        String sql = "SELECT * FROM cart_item WHERE cart_id = ?::uuid AND product_id = ?::uuid";
        try (ResultSet rs = JDBC.query(sql, cartId, productId)) {
            if (rs.next()) {
                CartItem item = new CartItem();
                item.setId(rs.getString("id"));
                item.setCartId(rs.getString("cart_id"));
                item.setProductId(rs.getString("product_id"));
                item.setQuantity(rs.getInt("quantity"));
                item.setTotalPrice(rs.getDouble("total_price"));
                item.setCreatedAt(rs.getObject("created_at", OffsetDateTime.class));
                return item;
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to fetch cart item for cartId: " + cartId + ", productId: " + productId, e);
        }
        return null;
    }

    // Creates a new cart item
    public void createCartItem(String cartId, String productId, int quantity, double totalPrice) {
        String sql = "INSERT INTO cart_item (cart_id, product_id, quantity, total_price) VALUES (?::uuid, ?::uuid, ?, ?)";
        JDBC.execute(sql, cartId, productId, quantity, totalPrice);
    }

    // Updates a cart item
    public void updateCartItem(String cartItemId, String condition, Object value) {
        String cast = condition.equals("quantity") ? "::integer" : "";
        String sql = "UPDATE cart_item SET " + condition + " = ?" + cast + " WHERE id = ?::uuid";
        JDBC.execute(sql, value, cartItemId);
    }

    // Deletes a cart item
    public void deleteCartItem(String cartItemId) {
        String sql = "DELETE FROM cart_item WHERE id = ?::uuid";
        JDBC.execute(sql, cartItemId);
    }

    // Updates the total price of a cart
    public void updateCartTotalPrice(String cartId, double totalPrice) {
        String sql = "UPDATE carts SET total_price = ? WHERE id = ?::uuid";
        JDBC.execute(sql, totalPrice, cartId);
    }

    // Clears all cart items from a cart
    public void clearCartItems(String cartId) {
        String sql = "DELETE FROM cart_item WHERE cart_id = ?::uuid";
        JDBC.execute(sql, cartId);
    }
}
