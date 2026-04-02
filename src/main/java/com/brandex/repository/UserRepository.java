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

    public User getUser(String condition, String value) throws SQLException {
        String sql = "SELECT * FROM users WHERE " + condition + " = ?";
        ResultSet rs = JDBC.query(sql, value);

        if (rs.next()) {
            User user = new User();
            user.setId(rs.getString("user_id"));
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
        return null;
    }

    public void createUser(User user) throws SQLException {
        String sql = "INSERT INTO users (username, email, first_name, last_name, password_hash, role, otp_hash) VALUES (?, ?, ?, ?, ?, ?, ?)";
        JDBC.execute(sql,
                user.getUsername(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getPasswordHash(),
                user.getRole(),
                user.getOtpHash());
    }

    public void updatePassword(String username, String newHash, String prev1, String prev2) throws SQLException {
        String sql = "UPDATE users SET password_hash = ?, prev_hash_1 = ?, prev_hash_2 = ?, force_pw_change = false WHERE username = ?";
        JDBC.execute(sql, newHash, prev1, prev2, username);
    }

    public void updateOtpUsed(String username) throws SQLException {
        String sql = "UPDATE users SET otp_used = true WHERE username = ?";
        JDBC.execute(sql, username);
    }
}
