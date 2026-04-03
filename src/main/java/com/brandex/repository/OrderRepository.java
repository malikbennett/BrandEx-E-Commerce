package com.brandex.repository;

import com.brandex.database.JDBC;
import com.brandex.models.Order;
import com.brandex.models.enums.OrderStatus;
import com.brandex.models.enums.PaymentMethod;

import java.sql.ResultSet;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

public class OrderRepository {

    private static OrderRepository instance;

    public static OrderRepository getInstance() {
        if (instance == null) {
            instance = new OrderRepository();
        }
        return instance;
    }

    public void createOrder(Order order) {
        String sql = "INSERT INTO orders (user_id, order_number, status, shipping_address, payment_method, total) VALUES (?::uuid, ?, ?, ?, ?, ?)";
        try {
            JDBC.execute(sql,
                    order.getUserId(),
                    order.getOrderNumber(),
                    order.getStatus().name(), // Store enum as string
                    order.getShippingAddress(),
                    order.getPaymentMethod().name(), // Store enum as string
                    order.getTotal());
        } catch (Exception e) {
            System.err.println("Database Error: Failed to create order. " + e.getMessage());
            e.printStackTrace();
        }
    }

    public List<Order> getOrdersByUserId(String userId) {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT * FROM orders WHERE user_id = ?::uuid";
        try {
            ResultSet rs = JDBC.query(sql, userId);
            while (rs.next()) {
                Order order = new Order();
                order.setId(rs.getString("id"));
                order.setUserId(rs.getString("user_id"));
                order.setOrderNumber(rs.getString("order_number"));
                
                // Convert string back to Enum
                order.setStatus(OrderStatus.valueOf(rs.getString("status")));
                
                order.setShippingAddress(rs.getString("shipping_address"));
                
                // Convert string back to Enum
                order.setPaymentMethod(PaymentMethod.valueOf(rs.getString("payment_method")));
                
                order.setTotal(rs.getDouble("total"));
                order.setCreatedAt(rs.getObject("created_at", OffsetDateTime.class));
                orders.add(order);
            }
        } catch (Exception e) {
            System.err.println("Database Error: Failed to fetch orders. " + e.getMessage());
            e.printStackTrace();
        }
        return orders;
    }

    public void updateOrderStatus(String orderId, OrderStatus newStatus) {
        String sql = "UPDATE orders SET status = ? WHERE id = ?::uuid";
        try {
            JDBC.execute(sql, newStatus.name(), orderId);
        } catch (Exception e) {
            System.err.println("Database Error: Failed to update order status. " + e.getMessage());
            e.printStackTrace();
        }
    }
}
