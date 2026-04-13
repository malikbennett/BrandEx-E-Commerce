package com.brandex.repository;

import com.brandex.database.JDBC;
import com.brandex.datastructures.LinkedList;
import com.brandex.models.Order;
import com.brandex.models.OrderItem;
import com.brandex.models.enums.OrderStatus;
import com.brandex.database.DatabaseException;

import java.sql.ResultSet;
import java.sql.SQLException;

// The repository class for managing orders.
public class OrderRepository {

    private static OrderRepository instance;

    // Returns the instance of the OrderRepository
    public static OrderRepository getInstance() {
        if (instance == null) {
            instance = new OrderRepository();
        }
        return instance;
    }

    // Creates a new order
    public void createOrder(Order order) {
        String sql = "INSERT INTO orders (user_id, order_number, status, shipping_address, payment_method, total_price) VALUES (?::uuid, ?, ?, ?, ?, ?) RETURNING id";
        try (ResultSet rs = JDBC.query(sql,
                order.getUserId(),
                order.getOrderNumber(),
                order.getStatus().name(),
                order.getShippingAddress(),
                order.getPaymentMethod().name(),
                order.getTotal())) {
            if (rs.next()) {
                order.setId(rs.getString("id"));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to create order: " + order.getOrderNumber(), e);
        }
    }

    // Creates a new order item
    public void createOrderItem(OrderItem item) {
        String sql = "INSERT INTO order_item (order_id, product_id, quantity, total_price) VALUES (?::uuid, ?::uuid, ?, ?)";
        JDBC.execute(sql,
                item.getOrderId(),
                item.getProductId(),
                item.getQuantity(),
                item.getTotalPrice());
    }

    // Returns a list of orders for a given user ID
    public LinkedList<Order> getOrdersByUserId(String userId) {
        LinkedList<Order> orders = new LinkedList<>((a, b) -> 0);
        String sql = "SELECT * FROM orders WHERE user_id = ?::uuid ORDER BY created_at DESC";
        try (ResultSet rs = JDBC.query(sql, userId)) {
            while (rs.next()) {
                orders.insert(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to fetch orders for userId: " + userId, e);
        }
        return orders;
    }

    // Updates the status of an order
    public void updateOrderStatus(String orderId, OrderStatus newStatus) {
        String sql = "UPDATE orders SET status = ? WHERE id = ?::uuid";
        JDBC.execute(sql, newStatus.name(), orderId);
    }

    // Returns a list of all orders
    public LinkedList<Order> getAllOrders() {
        LinkedList<Order> orders = new LinkedList<>((a, b) -> 0);
        String sql = "SELECT * FROM orders ORDER BY created_at ASC";
        try (ResultSet rs = JDBC.query(sql)) {
            while (rs.next()) {
                orders.insert(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to fetch all orders", e);
        }
        return orders;
    }

    // Returns a list of orders based on status
    public LinkedList<Order> getOrdersByStatus(OrderStatus status) {
        LinkedList<Order> orders = new LinkedList<>((a, b) -> 0);
        String sql = "SELECT * FROM orders WHERE status = ? ORDER BY created_at ASC";
        try (ResultSet rs = JDBC.query(sql, status.name())) {
            while (rs.next()) {
                orders.insert(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to fetch orders by status: " + status, e);
        }
        return orders;
    }

    // Maps a result set row to an order
    private Order mapRow(ResultSet rs) throws SQLException {
        Order order = new Order();
        order.setId(rs.getString("id"));
        order.setUserId(rs.getString("user_id"));
        order.setOrderNumber(rs.getString("order_number"));
        order.setStatus(com.brandex.models.enums.OrderStatus.valueOf(rs.getString("status")));
        order.setShippingAddress(rs.getString("shipping_address"));
        order.setPaymentMethod(com.brandex.models.enums.PaymentMethod.valueOf(rs.getString("payment_method")));
        order.setTotal(rs.getDouble("total_price"));
        order.setCreatedAt(rs.getObject("created_at", java.time.OffsetDateTime.class));
        return order;
    }
}
