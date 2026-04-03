package com.brandex.repository;

import com.brandex.models.User;
import com.brandex.database.JDBC;
import java.sql.*;
import java.time.OffsetDateTime;

// Interacts with the "users" table in the database to perform CRUD operations related to user accounts
public class UserRepository {

    private static UserRepository instance;

    public static UserRepository getInstance() {
        if (instance == null)
            instance = new UserRepository();
        return instance;
    }

    public User getUser(String condition, String value) {
        String cast = condition.equals("id") ? "::uuid" : "";
        String sql = "SELECT * FROM users WHERE " + condition + " = ?" + cast;
        try {
            ResultSet rs = JDBC.query(sql, value);
            if (rs.next()) {
                User user = new User();
                user.setId(rs.getString("id"));
                user.setUsername(rs.getString("username"));
                user.setEmail(rs.getString("email"));
                user.setFirstName(rs.getString("first_name"));
                user.setLastName(rs.getString("last_name"));
                user.setPhoneNumber(rs.getString("phone_number"));
                user.setShippingAddress(rs.getString("shipping_address"));
                user.setRole(rs.getString("role"));
                user.setPasswordHash(rs.getString("password_hash"));
                user.setPrevHash1(rs.getString("prev_hash_1"));
                user.setPrevHash2(rs.getString("prev_hash_2"));
                user.setOtpHash(rs.getString("otp_hash"));
                user.setOtpUsed(rs.getBoolean("otp_used"));
                user.setForcePwChange(rs.getBoolean("force_pw_change"));
                user.setProfileImgURL(rs.getString("profile_image_url"));
                user.setCreatedAt(rs.getObject("created_at", OffsetDateTime.class));
                return user;
            }
        } catch (Exception e) {
            System.err.println("Database Error: Failed to fetch user. " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public void createUser(User user) {
        String sql = "INSERT INTO users (username, email, first_name, last_name, password_hash, role, otp_hash) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try {
            JDBC.execute(sql,
                    user.getUsername(),
                    user.getEmail(),
                    user.getFirstName(),
                    user.getLastName(),
                    user.getPasswordHash(),
                    user.getRole(),
                    user.getOtpHash());
        } catch (Exception e) {
            System.err.println("Database Error: Failed to create user. " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void updatePassword(String username, String newHash, String prev1, String prev2) {
        String sql = "UPDATE users SET password_hash = ?, prev_hash_1 = ?, prev_hash_2 = ?, force_pw_change = false WHERE username = ?";
        try {
            JDBC.execute(sql, newHash, prev1, prev2, username);
        } catch (Exception e) {
            System.err.println("Database Error: Failed to update password. " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void updateOtpUsed(String username) {
        String sql = "UPDATE users SET otp_used = true WHERE username = ?";
        try {
            JDBC.execute(sql, username);
        } catch (Exception e) {
            System.err.println("Database Error: Failed to update OTP used status. " + e.getMessage());
            e.printStackTrace();
        }
    }
}
