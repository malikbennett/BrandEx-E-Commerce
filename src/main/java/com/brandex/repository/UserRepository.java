package com.brandex.repository;

import com.brandex.models.User;
import com.brandex.models.enums.UserStatus;
import com.brandex.database.JDBC;
import com.brandex.datastructures.LinkedList;

import java.sql.*;
import java.time.OffsetDateTime;

/**
 * Interacts with the "users" table in the database to perform CRUD operations related to user accounts.
 */
public class UserRepository {

    private static UserRepository instance;

    public static UserRepository getInstance() {
        if (instance == null)
            instance = new UserRepository();
        return instance;
    }

    public LinkedList<User> listUsers() {
        LinkedList<User> users = new LinkedList<>((a, b) -> 0);
        String sql = "SELECT * FROM users";
        try {
            ResultSet rs = JDBC.query(sql);
            while (rs.next()) {
                users.insert(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("DB Error - listUsers: " + e.getMessage());
        }
        return users;
    }

    public User findById(String id) {
        String sql = "SELECT * FROM users WHERE id = ?::uuid";
        try {
            ResultSet rs = JDBC.query(sql, id);
            if (rs.next()) {
                return mapRow(rs);
            }
        } catch (SQLException e) {
            System.err.println("DB Error - findById: " + e.getMessage());
        }
        return null;
    }

    public User findByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";
        try {
            ResultSet rs = JDBC.query(sql, username);
            if (rs.next()) {
                return mapRow(rs);
            }
        } catch (SQLException e) {
            System.err.println("DB Error - findByUsername: " + e.getMessage());
        }
        return null;
    }

    public User findByEmail(String email) {
        String sql = "SELECT * FROM users WHERE email = ?";
        try {
            ResultSet rs = JDBC.query(sql, email);
            if (rs.next()) {
                return mapRow(rs);
            }
        } catch (SQLException e) {
            System.err.println("DB Error - findByEmail: " + e.getMessage());
        }
        return null;
    }

    public String createUser(User user) {
        String sql = "INSERT INTO users (username, email, first_name, last_name, password_hash, role, otp_hash) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?) RETURNING id";
        try {
            ResultSet rs = JDBC.query(sql,
                    user.getUsername(),
                    user.getEmail(),
                    user.getFirstName(),
                    user.getLastName(),
                    user.getPasswordHash(),
                    user.getRole(),
                    user.getOtpHash());
            if (rs.next())
                return rs.getString("id");
        } catch (SQLException e) {
            System.err.println("DB Error - createUser: " + e.getMessage());
        }
        return null;
    }

    public boolean updateUser(User user) {
        String sql = "UPDATE users SET username=?, email=?, first_name=?, last_name=?, phone_number=?, shipping_address=?, status=?, role=?, password_hash=?, profile_image_url=? WHERE id=?::uuid";
        try {
            JDBC.execute(sql,
                    user.getUsername(),
                    user.getEmail(),
                    user.getFirstName(),
                    user.getLastName(),
                    user.getPhoneNumber(),
                    user.getShippingAddress(),
                    user.getStatus().name(),
                    user.getRole(),
                    user.getPasswordHash(),
                    user.getProfileImgURL(),
                    user.getId());
            return true;
        } catch (SQLException e) {
            System.err.println("DB Error - updateUser: " + e.getMessage());
            return false;
        }
    }

    public boolean updatePassword(String userId, String newHash, String prev1, String prev2) {
        String sql = "UPDATE users SET password_hash = ?, prev_hash_1 = ?, prev_hash_2 = ?, force_pw_change = false WHERE id = ?::uuid";
        try {
            JDBC.execute(sql, newHash, prev1, prev2, userId);
            return true;
        } catch (SQLException e) {
            System.err.println("DB Error - updatePassword: " + e.getMessage());
            return false;
        }
    }

    public boolean updateOtpUsed(String userId) {
        String sql = "UPDATE users SET otp_used = true WHERE id = ?::uuid";
        try {
            JDBC.execute(sql, userId);
            return true;
        } catch (SQLException e) {
            System.err.println("DB Error - updateOtpUsed: " + e.getMessage());
            return false;
        }
    }

    public boolean updateUserStatusAndRole(String userId, UserStatus status, String role) {
        String sql = "UPDATE users SET status = ?, role = ? WHERE id = ?::uuid";
        try {
            JDBC.execute(sql, status.name(), role, userId);
            return true;
        } catch (SQLException e) {
            System.err.println("DB Error - updateUserStatusAndRole: " + e.getMessage());
            return false;
        }
    }

    public boolean resetPassword(String userId, String tempPwHash) {
        String sql = "UPDATE users SET password_hash = ?, force_pw_change = true WHERE id = ?::uuid";
        try {
            JDBC.execute(sql, tempPwHash, userId);
            return true;
        } catch (SQLException e) {
            System.err.println("DB Error - resetPassword: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteUser(String userId) {
        String sql = "DELETE FROM users WHERE id = ?::uuid";
        try {
            JDBC.execute(sql, userId);
            return true;
        } catch (SQLException e) {
            System.err.println("DB Error - deleteUser: " + e.getMessage());
            return false;
        }
    }

    private User mapRow(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getString("id"));
        user.setUsername(rs.getString("username"));
        user.setEmail(rs.getString("email"));
        user.setFirstName(rs.getString("first_name"));
        user.setLastName(rs.getString("last_name"));
        user.setPhoneNumber(rs.getString("phone_number"));
        user.setShippingAddress(rs.getString("shipping_address"));
        user.setStatus(UserStatus.valueOf(rs.getString("status").toUpperCase()));
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
}
